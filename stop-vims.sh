#!/bin/bash
# VIMS 애플리케이션 중지 스크립트
# start-vims.sh(mvn spring-boot:run) 및 java -jar 실행 모두 포트 기준으로 종료

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "=== 🛑 VIMS 애플리케이션 중지 시작 ==="

# start-vims.sh 와 동일한 서비스/포트 정의
SERVICES=(
    "Gateway|8080"
    "FMS|8082"
    "Login|8081"
    "Management|8083"
    "Register|8085"
    "Inspection|8086"
)

kill_port() {
    local PORT=$1
    local PIDS
    PIDS=$(lsof -ti tcp:"$PORT" 2>/dev/null || true)
    if [ -n "$PIDS" ]; then
        echo "$PIDS" | xargs kill -15 2>/dev/null || true
        sleep 2
        PIDS=$(lsof -ti tcp:"$PORT" 2>/dev/null || true)
        if [ -n "$PIDS" ]; then
            echo "$PIDS" | xargs kill -9 2>/dev/null || true
        fi
        return 0
    fi
    return 1
}

stop_mvn_run() {
    local DIR_NAME=$1
    local PIDS
    PIDS=$(pgrep -f "spring-boot:run.*${DIR_NAME}/pom.xml" 2>/dev/null || true)
    if [ -n "$PIDS" ]; then
        echo "$PIDS" | xargs kill -15 2>/dev/null || true
        sleep 2
        PIDS=$(pgrep -f "spring-boot:run.*${DIR_NAME}/pom.xml" 2>/dev/null || true)
        if [ -n "$PIDS" ]; then
            echo "$PIDS" | xargs kill -9 2>/dev/null || true
        fi
        return 0
    fi
    return 1
}

# JAR 방식(deploy-output) 실행 프로세스도 함께 종료
stop_jar() {
    local JAR_NAME=$1
    local PIDS
    PIDS=$(pgrep -f "$JAR_NAME" 2>/dev/null || true)
    if [ -n "$PIDS" ]; then
        echo "$PIDS" | xargs kill -15 2>/dev/null || true
        sleep 2
        PIDS=$(pgrep -f "$JAR_NAME" 2>/dev/null || true)
        if [ -n "$PIDS" ]; then
            echo "$PIDS" | xargs kill -9 2>/dev/null || true
        fi
        return 0
    fi
    return 1
}

JAR_APPS=(
    "vims-gateway-ROOT.jar|Gateway"
    "FMS-ROOT.jar|FMS"
    "vims-login-ROOT.jar|Login"
    "vims-management-ROOT.jar|Management"
    "vims-register-ROOT.jar|Register"
    "vims-inspection-ROOT.jar|Inspection"
    "vims-web-app-ROOT.jar|WebApp"
)

MVN_DIRS=(
    "vims-gateway|Gateway"
    "FMS|FMS"
    "vims-login|Login"
    "vims-management-system|Management"
    "vims-register|Register"
    "vims-inspection|Inspection"
)

for ENTRY in "${SERVICES[@]}"; do
    IFS='|' read -r NAME PORT <<< "$ENTRY"
    echo "🔍 $NAME (포트: $PORT) 확인 중..."

    if kill_port "$PORT"; then
        echo "✅ $NAME 중지 완료 (포트 $PORT)"
    else
        echo "ℹ️  $NAME 포트 $PORT 에서 실행 중인 프로세스 없음"
    fi
    echo "--------------------------------------"
done

for ENTRY in "${MVN_DIRS[@]}"; do
    IFS='|' read -r DIR NAME <<< "$ENTRY"
    if stop_mvn_run "$DIR"; then
        echo "✅ $NAME Maven(spring-boot:run) 프로세스 정리 완료"
    fi
done

for ENTRY in "${JAR_APPS[@]}"; do
    IFS='|' read -r JAR NAME <<< "$ENTRY"
    if stop_jar "$JAR"; then
        echo "✅ $NAME JAR 프로세스 정리 완료"
    fi
done

echo "=== 🎉 모든 애플리케이션 중지 완료! ==="
echo "상태 확인 : lsof -i tcp:8080-8086"
