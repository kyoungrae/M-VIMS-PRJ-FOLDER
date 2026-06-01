#!/bin/bash
# VIMS 애플리케이션 시작 스크립트
# 포트 점유 시 kill -9 후 재시작

set -e

echo "=== VIMS 애플리케이션 시작 ==="

# ─────────────────────────────────────────
# 서비스 정의: "이름|JAR경로|포트|클래스명"
# 실행 순서 준수 (Gateway는 마지막)
# ─────────────────────────────────────────
SERVICES=(
    "Gateway|vims-gateway/target/vims-gateway-ROOT.jar|8080|VimsGatewayApplication"
    "FMS|FMS/target/FMS-ROOT.jar|8082|FileManagementSystemApplication"
    "Login|vims-login/target/vims-login-ROOT.jar|8081|VimsLoginApplication"
    "Management|vims-management-system/target/vims-management-ROOT.jar|8083|VimsManagementApplication"
    "Register|vims-register/target/vims-register-ROOT.jar|8085|VimsRegisterApplication"
)

LOG_DIR="logs"
mkdir -p "$LOG_DIR"

# ─────────────────────────────────────────
# 포트 점유 프로세스 kill -9
# ─────────────────────────────────────────
kill_port() {
    local PORT=$1
    local NAME=$2
    local PIDS

    PIDS=$(lsof -ti tcp:"$PORT" 2>/dev/null || true)

    if [ -n "$PIDS" ]; then
        echo "  ⚠️  포트 $PORT 점유 중 (PID: $PIDS) → kill -9"
        echo "$PIDS" | xargs kill -9 2>/dev/null || true
        sleep 1
        echo "  ✅ 포트 $PORT 해제 완료"
    fi
}

# ─────────────────────────────────────────
# 서비스 시작 함수
# ─────────────────────────────────────────
start_app() {
    local NAME=$1
    local JAR=$2
    local PORT=$3
    local CLASS=$4
    local LOG_FILE="$LOG_DIR/$(echo "$NAME" | tr '[:upper:]' '[:lower:]').log"

    echo ""
    echo "--- [$NAME] 시작 중 (포트: $PORT) ---"

    # JAR 존재 확인
    if [ ! -f "$JAR" ]; then
        echo "  ❌ JAR 없음: $JAR"
        echo "  👉 먼저 빌드를 실행해주세요."
        return 1
    fi

    # 포트 점유 해제
    kill_port "$PORT" "$NAME"

    # 백그라운드 실행
    nohup java -jar "$JAR" > "$LOG_FILE" 2>&1 &
    local NEW_PID=$!

    # 기동 확인 (최대 20초 대기)
    local TIMEOUT=20
    local COUNT=0
    echo -n "  ⏳ 기동 대기 중"
    while [ $COUNT -lt $TIMEOUT ]; do
        sleep 1
        COUNT=$((COUNT + 1))
        echo -n "."
        if lsof -ti tcp:"$PORT" > /dev/null 2>&1; then
            echo ""
            echo "  ✅ $NAME 기동 완료 (PID: $NEW_PID, 포트: $PORT)"
            echo "  📜 로그: $LOG_FILE"
            return 0
        fi
    done

    echo ""
    echo "  ⚠️  $NAME 포트 응답 없음 (${TIMEOUT}초 초과) — 로그를 확인하세요: $LOG_FILE"
}

# ─────────────────────────────────────────
# 순서대로 실행
# ─────────────────────────────────────────
for SERVICE in "${SERVICES[@]}"; do
    IFS='|' read -r NAME JAR PORT CLASS <<< "$SERVICE"
    start_app "$NAME" "$JAR" "$PORT" "$CLASS"
done

echo ""
echo "=== 모든 서비스 시작 완료 ==="
echo "상태 확인: ps -ef | grep java"
echo "로그 확인: tail -f logs/[서비스명].log"
