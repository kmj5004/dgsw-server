#!/usr/bin/env bash













set -euo pipefail

if ! command -v docker >/dev/null 2>&1; then
    if [ -x "/Applications/Docker.app/Contents/Resources/bin/docker" ]; then
        export PATH="/Applications/Docker.app/Contents/Resources/bin:$PATH"
    else
        echo "docker not found in PATH" >&2
        exit 1
    fi
fi

if ! command -v jq >/dev/null 2>&1; then
    echo "jq not found in PATH" >&2
    exit 1
fi

ROOT="$(cd "$(dirname "$0")" && pwd)"
TARGET="${1:-}"

challenges=()
if [ -n "$TARGET" ]; then
    [ -d "$ROOT/$TARGET" ] || { echo "no such challenge: $TARGET" >&2; exit 2; }
    challenges=("$TARGET")
else
    while IFS= read -r d; do
        challenges+=("$(basename "$d")")
    done < <(find "$ROOT" -mindepth 1 -maxdepth 1 -type d | sort)
fi

PASS=0
FAIL=0
FAILED_NAMES=()

for slug in "${challenges[@]}"; do
    dir="$ROOT/$slug"
    [ -f "$dir/meta.json" ] || { echo "skip $slug (no meta.json)"; continue; }

    language=$(jq -r '.language // "VERILOG"' "$dir/meta.json")

    case "$language" in
        VERILOG)
            ref="$dir/reference.v"; tb="$dir/testbench.v"
            user_dst="user.v";      tb_dst="testbench.v"
            image="${WORKER_IMAGE:-hdl-judge/worker-verilog:latest}"
            cmd="iverilog -o /tmp/sim user.v testbench.v && vvp /tmp/sim"
            tmpfs="rw,size=64m"
            ;;
        VHDL)
            ref="$dir/reference.vhdl"; tb="$dir/testbench.vhdl"
            user_dst="user.vhdl";      tb_dst="testbench.vhdl"
            image="${VHDL_WORKER_IMAGE:-hdl-judge/worker-vhdl:latest}"

            cmd="cd /tmp && cp /work/*.vhdl . && ghdl -a user.vhdl && ghdl -a testbench.vhdl && ghdl -e tb && ghdl -r tb --stop-time=1ms"
            tmpfs="rw,exec,size=64m"
            ;;
        *)
            echo "skip $slug (unknown language: $language)"; continue
            ;;
    esac

    [ -f "$ref" ] || { echo "skip $slug (no $(basename "$ref"))"; continue; }
    [ -f "$tb" ]  || { echo "skip $slug (no $(basename "$tb"))"; continue; }

    echo
    echo "==> verify: $slug ($language)"

    work=$(mktemp -d)
    cp "$ref" "$work/$user_dst"
    cp "$tb"  "$work/$tb_dst"

    set +e
    stdout=$(docker run --rm \
        --network=none --read-only --memory=256m --pids-limit=64 \
        --security-opt no-new-privileges:true \
        -v "$work:/work:ro" --workdir /work --tmpfs "/tmp:$tmpfs" \
        "$image" \
        "$cmd" 2>/dev/null)
    rc=$?
    set -e
    rm -rf "$work"

    if [ $rc -ne 0 ]; then
        echo "    FAIL: simulation exited with code $rc"
        FAIL=$((FAIL+1))
        FAILED_NAMES+=("$slug")
        continue
    fi

    bad=0
    while IFS= read -r line; do
        [[ "$line" =~ ^::HDLJUDGE_VEC::ordering=([0-9]+)::output=(.+)$ ]] || continue
        ord="${BASH_REMATCH[1]}"
        actual="${BASH_REMATCH[2]}"
        expected=$(jq -r --argjson o "$ord" '.testVectors[] | select(.ordering == $o) | .expectedJson' "$dir/meta.json")
        if [ -z "$expected" ] || [ "$expected" = "null" ]; then
            echo "    FAIL ord=$ord: no expected_json in meta.json"
            bad=$((bad+1))
            continue
        fi
        norm_actual=$(echo "$actual"   | jq -cS .)
        norm_expected=$(echo "$expected" | jq -cS .)
        if [ "$norm_actual" = "$norm_expected" ]; then
            echo "    pass ord=$ord  $norm_actual"
        else
            echo "    FAIL ord=$ord  actual=$norm_actual  expected=$norm_expected"
            bad=$((bad+1))
        fi
    done <<< "$stdout"

    if [ $bad -eq 0 ]; then
        PASS=$((PASS+1))
    else
        FAIL=$((FAIL+1))
        FAILED_NAMES+=("$slug")
    fi
done

echo
echo "===================="
echo "passed: $PASS"
echo "failed: $FAIL"
if [ $FAIL -gt 0 ]; then
    echo "failed challenges: ${FAILED_NAMES[*]}"
    exit 1
fi
