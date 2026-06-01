#!/bin/bash
# VIMS 애플리케이션 시작 스크립트 (mvn spring-boot:run 방식 — JAR 빌드 불필요)
# 포트 점유 시 kill -9 후 재시작

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "=== VIMS 애플리케이션 시작 ==="

# ─────────────────────────────────────────
# Maven 명령어 탐색
# ─────────────────────────────────────────
MVN_CMD="mvn"
FOUND_MVN=false

if command -v mvn &> /dev/null; then
    FOUND_MVN=true
else
    POSSIBLE_PATHS=(
        "/Applications/IntelliJ IDEA.app/Contents/plugins/maven/lib/maven3/bin/mvn"
        "/Applications/IntelliJ IDEA CE.app/Contents/plugins/maven/lib/maven3/bin/mvn"
        "$HOME/Applications/IntelliJ IDEA.app/Contents/plugins/maven/lib/maven3/bin/mvn"
        "$HOME/Applications/IntelliJ IDEA CE.app/Contents/plugins/maven/lib/maven3/bin/mvn"
        "/usr/local/bin/mvn"
        "/opt/homebrew/bin/mvn"
    )
    for PATH_CHECK in "${POSSIBLE_PATHS[@]}"; do
        if [ -f "$PATH_CHECK" ]; then
            MVN_CMD="$PATH_CHECK"
            FOUND_MVN=true
            echo "Maven 경로: $MVN_CMD"
            break
        fi
    done
fi

if [ "$FOUND_MVN" = false ]; then
    echo "❌ mvn 명령어를 찾을 수 없습니다. Maven을 설치하거나 경로를 확인해주세요."
    exit 1
fi

# ─────────────────────────────────────────
# 서비스 정의: "이름|프로젝트 디렉터리|포트|메인 클래스"
# ─────────────────────────────────────────
SERVICES=(
    "Gateway|vims-gateway|8080|com.VimsGatewayApplication"
    "FMS|FMS|8082|com.FileManagementSystemApplication"
    "Login|vims-login|8081|com.VimsLoginApplication"
    "Management|vims-management-system|8083|com.VimsManagementApplication"
    "Register|vims-register|8085|com.VimsRegisterApplication"
    "Inspection|vims-inspection|8086|com.VimsInspectionApplication"
)

LOG_DIR="$SCRIPT_DIR/logs"
mkdir -p "$LOG_DIR"

# ─────────────────────────────────────────
# 포트 점유 프로세스 kill -9
# ─────────────────────────────────────────
kill_port() {
    local PORT=$1
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
    local DIR="$SCRIPT_DIR/$2"
    local PORT=$3
    local MAIN_CLASS=$4
    local LOG_FILE="$LOG_DIR/$(echo "$NAME" | tr '[:upper:]' '[:lower:]').log"

    echo ""
    echo "--- [$NAME] 시작 중 (포트: $PORT) ---"

    if [ ! -f "$DIR/pom.xml" ]; then
        echo "  ❌ pom.xml 없음: $DIR"
        return 1
    fi

    # 포트 점유 해제
    kill_port "$PORT"

    # mvn spring-boot:run 백그라운드 실행
    nohup "$MVN_CMD" -f "$DIR/pom.xml" spring-boot:run \
        -Dspring-boot.run.mainClass="$MAIN_CLASS" \
        > "$LOG_FILE" 2>&1 &
    local MVN_PID=$!

    # 기동 확인 (최대 60초 대기)
    local TIMEOUT=60
    local COUNT=0
    echo -n "  ⏳ 기동 대기 중"
    while [ $COUNT -lt $TIMEOUT ]; do
        sleep 1
        COUNT=$((COUNT + 1))
        echo -n "."
        if lsof -ti tcp:"$PORT" > /dev/null 2>&1; then
            echo ""
            echo "  ✅ $NAME 기동 완료 (MVN PID: $MVN_PID, 포트: $PORT)"
            echo "  📜 로그: $LOG_FILE"
            return 0
        fi
    done

    echo ""
    echo "  ⚠️  $NAME 포트 응답 없음 (${TIMEOUT}초 초과) — 로그 확인: $LOG_FILE"
}

# ─────────────────────────────────────────
# 순서대로 실행
# ─────────────────────────────────────────
for SERVICE in "${SERVICES[@]}"; do
    IFS='|' read -r NAME DIR PORT MAIN_CLASS <<< "$SERVICE"
    start_app "$NAME" "$DIR" "$PORT" "$MAIN_CLASS"
done

echo ""
echo "=== 모든 서비스 시작 완료 ==="
echo "상태 확인 : ps -ef | grep spring-boot"
echo "로그 확인 : tail -f $LOG_DIR/[서비스명].log"
