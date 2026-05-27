#!/usr/bin/env bash























set -euo pipefail

ADMIN_EMAIL="${ADMIN_EMAIL:-admin@hdljudge.local}"
ADMIN_PASSWORD="${ADMIN_PASSWORD:-hunter2hunter2}"
BASE_URL="${BASE_URL:-http://localhost:8080}"
LOG="${HDLJUDGE_LOG:-/tmp/hdljudge-bootstrap.log}"

ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT"

step() { printf "\n\033[1;36m→\033[0m %s\n" "$*"; }
ok()   { printf "  \033[1;32m✓\033[0m %s\n" "$*"; }
warn() { printf "  \033[1;33m!\033[0m %s\n" "$*"; }
die()  { printf "\n\033[1;31m✗\033[0m %s\n" "$*" >&2; exit 1; }



if ! command -v docker >/dev/null 2>&1; then
    if [ -x /Applications/Docker.app/Contents/Resources/bin/docker ]; then
        export PATH="/Applications/Docker.app/Contents/Resources/bin:$PATH"
    else
        die "docker CLI 가 PATH 에 없음. Docker Desktop 을 먼저 실행하세요."
    fi
fi
docker info >/dev/null 2>&1 || die "Docker daemon 이 응답하지 않음. Docker Desktop 을 시작하세요."
command -v jq >/dev/null 2>&1 || die "jq 가 필요합니다 (brew install jq)."
[ -x ./gradlew ] || die "./gradlew 실행 권한 없음 — chmod +x gradlew 하세요."



step "MySQL 컨테이너"
if docker ps --filter name=hdljudge-mysql --filter status=running --format '{{.Names}}' | grep -q hdljudge-mysql; then
    MYSQL_HOST_PORT=$(docker port hdljudge-mysql 3306 2>/dev/null | head -1 | awk -F: '{print $NF}')
    ok "이미 실행 중 (host port $MYSQL_HOST_PORT)"
else
    HOST_3306=$(lsof -nP -iTCP:3306 -sTCP:LISTEN 2>/dev/null | awk 'NR==2{print $1":"$2}')
    if [ -n "$HOST_3306" ]; then
        warn "호스트 3306 사용 중 ($HOST_3306) → 컨테이너를 3307 로 매핑"
        MYSQL_HOST_PORT=3307
    else
        MYSQL_HOST_PORT=3306
    fi
    docker rm -f hdljudge-mysql >/dev/null 2>&1 || true
    docker run -d --name hdljudge-mysql \
        -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=hdljudge \
        -e MYSQL_USER=hdljudge -e MYSQL_PASSWORD=hdljudge \
        -e TZ=Asia/Seoul \
        -p "${MYSQL_HOST_PORT}:3306" \
        --health-cmd "mysqladmin ping -h localhost -u root -proot" \
        --health-interval 5s --health-timeout 5s --health-retries 30 \
        mysql:8.0 \
        --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci >/dev/null
    until [ "$(docker inspect -f '{{.State.Health.Status}}' hdljudge-mysql 2>/dev/null)" = "healthy" ]; do sleep 2; done
    ok "started, healthy (host port $MYSQL_HOST_PORT)"
fi

step "Redis 컨테이너"
if docker ps --filter name=hdljudge-redis --filter status=running --format '{{.Names}}' | grep -q hdljudge-redis; then
    ok "이미 실행 중"
else
    docker rm -f hdljudge-redis >/dev/null 2>&1 || true
    docker run -d --name hdljudge-redis -p 6379:6379 redis:7-alpine >/dev/null
    ok "started"
fi



step "iverilog/yosys/netlistsvg 워커 이미지"
if docker image inspect hdl-judge/worker-verilog:latest >/dev/null 2>&1; then
    ok "이미 존재"
else
    warn "최초 빌드 (3~5 분)"
    docker build -t hdl-judge/worker-verilog:latest docker/worker-verilog >/dev/null
    ok "built"
fi

step "GHDL VHDL 워커 이미지"
if docker image inspect hdl-judge/worker-vhdl:latest >/dev/null 2>&1; then
    ok "이미 존재"
else
    warn "최초 빌드"
    docker build -t hdl-judge/worker-vhdl:latest docker/worker-vhdl >/dev/null
    ok "built"
fi



step "Spring Boot 백엔드"
if curl -sS -o /dev/null -w "%{http_code}" "$BASE_URL/actuator/health" 2>/dev/null | grep -q 200; then
    ok "이미 응답함 ($BASE_URL)"
else
    warn "백그라운드 시작, 로그: $LOG"
    nohup ./gradlew bootRun --no-daemon \
        --args="--spring.profiles.active=local --spring.datasource.url=jdbc:mysql://localhost:${MYSQL_HOST_PORT}/hdljudge?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8" \
        > "$LOG" 2>&1 &
    BOOT_PID=$!
    READY=0
    for i in $(seq 1 90); do
        sleep 2
        if curl -sS -o /dev/null -w "%{http_code}" "$BASE_URL/actuator/health" 2>/dev/null | grep -q 200; then
            READY=1; break
        fi
        if grep -qE "APPLICATION FAILED TO START" "$LOG" 2>/dev/null; then
            tail -30 "$LOG" >&2
            die "Spring Boot 시작 실패 (PID=$BOOT_PID) — $LOG 확인"
        fi
    done
    [ "$READY" = "1" ] || die "백엔드가 3분 안에 ready 상태 못 됨 — $LOG 확인"
    ok "healthy (PID=$BOOT_PID, log=$LOG)"
fi



step "admin 계정"
if curl -sS -X POST "$BASE_URL/api/auth/login" \
        -H 'Content-Type: application/json' \
        -d "{\"email\":\"$ADMIN_EMAIL\",\"password\":\"$ADMIN_PASSWORD\"}" 2>/dev/null \
        | jq -e '.data.accessToken' >/dev/null 2>&1; then

    TOKEN=$(curl -sS -X POST "$BASE_URL/api/auth/login" -H 'Content-Type: application/json' \
        -d "{\"email\":\"$ADMIN_EMAIL\",\"password\":\"$ADMIN_PASSWORD\"}" | jq -r .data.accessToken)
    ROLE=$(echo "$TOKEN" | awk -F. '{print $2}' | base64 -d 2>/dev/null | jq -r .role 2>/dev/null || echo "?")
    if [ "$ROLE" = "ADMIN" ]; then
        ok "$ADMIN_EMAIL 이미 ADMIN"
    else
        warn "$ADMIN_EMAIL 가입돼 있으나 role=$ROLE — DB 로 ADMIN 승격"
        docker exec hdljudge-mysql mysql -u hdljudge -phdljudge hdljudge \
            -e "UPDATE users SET role='ADMIN' WHERE email='$ADMIN_EMAIL';" >/dev/null 2>&1
        ok "ADMIN 으로 갱신"
    fi
else
    warn "$ADMIN_EMAIL 미가입 — 가입 + ADMIN 승격"
    curl -sS -o /dev/null -X POST "$BASE_URL/api/auth/signup" \
        -H 'Content-Type: application/json' \
        -d "{\"email\":\"$ADMIN_EMAIL\",\"password\":\"$ADMIN_PASSWORD\"}"
    docker exec hdljudge-mysql mysql -u hdljudge -phdljudge hdljudge \
        -e "UPDATE users SET role='ADMIN' WHERE email='$ADMIN_EMAIL';" >/dev/null 2>&1
    ok "준비 완료"
fi



step "챌린지 시드"
ADMIN_EMAIL="$ADMIN_EMAIL" ADMIN_PASSWORD="$ADMIN_PASSWORD" BASE_URL="$BASE_URL" \
    ./challenges/seed.sh 2>&1 | tail -20



CHALLENGE_COUNT=$(curl -sS "$BASE_URL/api/challenges?size=200" | jq -r .data.totalElements)
cat <<EOF

============================================================
 ✓ HDL Judge 백엔드 준비 완료
============================================================
   API base       : $BASE_URL
   Health         : $BASE_URL/actuator/health
   Swagger UI     : $BASE_URL/swagger-ui/index.html
   OpenAPI spec   : $BASE_URL/v3/api-docs
   Challenges     : $BASE_URL/api/challenges  (총 $CHALLENGE_COUNT 개 등록)
   admin login    : $ADMIN_EMAIL / $ADMIN_PASSWORD
   백엔드 로그    : $LOG
============================================================
EOF
