#!/usr/bin/env bash

















set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
ADMIN_EMAIL="${ADMIN_EMAIL:-}"
ADMIN_PASSWORD="${ADMIN_PASSWORD:-}"

if [ -z "$ADMIN_EMAIL" ] || [ -z "$ADMIN_PASSWORD" ]; then
    echo "ADMIN_EMAIL / ADMIN_PASSWORD 환경변수를 설정해주세요." >&2
    exit 2
fi

if ! command -v jq >/dev/null 2>&1; then
    echo "jq not found in PATH" >&2
    exit 1
fi

ROOT="$(cd "$(dirname "$0")" && pwd)"

echo "==> login as $ADMIN_EMAIL"
login_body=$(jq -n --arg e "$ADMIN_EMAIL" --arg p "$ADMIN_PASSWORD" '{email:$e, password:$p}')
login_resp=$(curl -sS -X POST "$BASE_URL/api/auth/login" \
    -H 'Content-Type: application/json' \
    -d "$login_body")
TOKEN=$(echo "$login_resp" | jq -r '.data.accessToken // empty')
if [ -z "$TOKEN" ]; then
    echo "login failed: $login_resp" >&2
    exit 3
fi


role=$(echo "$TOKEN" | awk -F. '{print $2}' | base64 -d 2>/dev/null | jq -r '.role // empty' || true)
if [ "$role" != "ADMIN" ]; then
    echo "warning: token role is '$role', not ADMIN — POST may fail with 403" >&2
fi

CREATED=0
SKIPPED=0
FAILED=0

for dir in $(find "$ROOT" -mindepth 1 -maxdepth 1 -type d | sort); do
    slug=$(basename "$dir")
    [ -f "$dir/meta.json" ] || { echo "skip $slug (no meta.json)"; continue; }


    language=$(jq -r '.language // "VERILOG"' "$dir/meta.json")
    case "$language" in
        VERILOG) ext="v" ;;
        VHDL)    ext="vhdl" ;;
        *)       echo "skip $slug (unsupported language: $language)"; continue ;;
    esac
    [ -f "$dir/skeleton.$ext" ] || { echo "skip $slug (no skeleton.$ext)"; continue; }
    [ -f "$dir/testbench.$ext" ] || { echo "skip $slug (no testbench.$ext)"; continue; }

    echo "==> seeding: $slug ($language)"
    skeleton=$(cat "$dir/skeleton.$ext")
    testbench=$(cat "$dir/testbench.$ext")

    payload=$(jq -n \
        --slurpfile meta "$dir/meta.json" \
        --arg skeleton "$skeleton" \
        --arg testbench "$testbench" \
        '$meta[0] + {skeleton: $skeleton, hiddenTestbench: $testbench}')

    http_code=$(curl -sS -o /tmp/seed.body -w "%{http_code}" \
        -X POST "$BASE_URL/api/challenges" \
        -H 'Content-Type: application/json' \
        -H "Authorization: Bearer $TOKEN" \
        -d "$payload")
    case "$http_code" in
        201)
            id=$(jq -r .data.id /tmp/seed.body)
            echo "    created id=$id"
            CREATED=$((CREATED+1))
            ;;
        409)
            code=$(jq -r .error.code /tmp/seed.body)
            echo "    skipped (already exists, $code)"
            SKIPPED=$((SKIPPED+1))
            ;;
        *)
            echo "    FAILED http=$http_code body=$(cat /tmp/seed.body)"
            FAILED=$((FAILED+1))
            ;;
    esac
done

echo
echo "===================="
echo "created: $CREATED"
echo "skipped: $SKIPPED"
echo "failed:  $FAILED"
[ $FAILED -eq 0 ]
