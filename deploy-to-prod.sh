#!/bin/bash
# =============================================================================
# deploy-to-prod.sh
# 각 모듈의 빌드 산출물(.jar)을 운영 서버의 대응 컨테이너에 배포하고 재기동한다.
#
# 동작 개요 (설계서: 배포-스크립트_설계서.md 참고)
#   1. (옵션) deploy-core-lib.sh 로 빌드
#   2. deploy-output 의 *-podman-ROOT.jar 선택
#   3. scp 로 운영 서버 스테이징 디렉터리에 전송
#   4. ssh -> podman cp 로 컨테이너 내부 /app/app.jar 주입
#   5. 컨테이너 내부 java 프로세스 종료 후 nohup java -jar 재기동
#   6. 호스트 포트 기준 헬스체크
#   7. 결과 요약 출력
#
# 사용 예시
#   ./deploy-to-prod.sh                      # 전체 모듈 배포
#   ./deploy-to-prod.sh login gateway        # 특정 모듈만 배포
#   ./deploy-to-prod.sh --build              # 빌드부터 수행 후 배포
#   ./deploy-to-prod.sh --source target      # target/*-ROOT.jar 배포(로컬 프로파일)
#   ./deploy-to-prod.sh --no-restart         # JAR 복사만, 재기동 생략
# =============================================================================

set -uo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

# ─────────────────────────────────────────────────────────────────────────────
# 1. 설정 (운영 환경에 맞게 수정)
# ─────────────────────────────────────────────────────────────────────────────
REMOTE_HOST="${REMOTE_HOST:-192.168.0.141}"              # 운영 서버 IP/호스트 (필수)
REMOTE_USER="${REMOTE_USER:-vims}"              # SSH 계정 (필수)
REMOTE_SSH_PORT="${REMOTE_SSH_PORT:-22222}"    # SSH 포트
ENGINE="${ENGINE:-podman}"                  # 컨테이너 엔진: podman | docker

REMOTE_STAGING_DIR="${REMOTE_STAGING_DIR:-~/vims-deploy}"  # JAR 임시 저장 경로(원격)
CONTAINER_APP_DIR="${CONTAINER_APP_DIR:-/app}"             # 컨테이너 내부 JAR 경로
CONTAINER_JAR_NAME="${CONTAINER_JAR_NAME:-app.jar}"        # 컨테이너 내부 통일 JAR 이름

JAR_SOURCE="${JAR_SOURCE:-deploy-output}"   # deploy-output | target
HEALTH_TIMEOUT="${HEALTH_TIMEOUT:-60}"      # 헬스체크 최대 대기(초)

DEPLOY_OUTPUT_DIR="$SCRIPT_DIR/deploy-output"

# ─────────────────────────────────────────────────────────────────────────────
# 2. 모듈 정의 테이블
#    형식: "표시명|로컬디렉터리|컨테이너명|host포트|내부포트|deploy-output JAR|target JAR"
#    ※ inspection / register 는 운영 컨테이너 추가 시 이 테이블에 한 줄만 추가하면 됨
# ─────────────────────────────────────────────────────────────────────────────
MODULES=(
  "Login|vims-login|vims-login|9081|8081|vims-login-podman-ROOT.jar|vims-login-ROOT.jar"
  "Management|vims-management-system|vims-management|9082|8082|vims-management-podman-ROOT.jar|vims-management-ROOT.jar"
  "FMS|FMS|vims-fms|9083|8083|FMS-podman-ROOT.jar|FMS-ROOT.jar"
  "WebApp|vims-web-app|vims-web-app|9084|8084|vims-web-app-podman-ROOT.jar|vims-web-app-ROOT.jar"
  "Gateway|vims-gateway|vims-gateway|9000|8080|vims-gateway-podman-ROOT.jar|vims-gateway-ROOT.jar"
)

# ─────────────────────────────────────────────────────────────────────────────
# 3. 옵션 파싱
# ─────────────────────────────────────────────────────────────────────────────
DO_BUILD=false
DO_RESTART=true
SELECTED=()

print_help() {
    cat <<'EOF'
사용법: ./deploy-to-prod.sh [옵션] [모듈명 ...]

옵션:
  --build              배포 전에 deploy-core-lib.sh 로 전체 빌드 수행
  --source <소스>      JAR 소스 지정: deploy-output(기본) | target
  --no-restart         JAR 복사만 하고 컨테이너 내부 java 재기동은 생략
  -h, --help           도움말 출력

모듈명(대소문자 무시, 표시명/디렉터리/컨테이너명 중 아무거나):
  login, management, fms, webapp, gateway
  (생략 시 전체 모듈 배포)

예시:
  ./deploy-to-prod.sh
  ./deploy-to-prod.sh login gateway
  ./deploy-to-prod.sh --build
  ./deploy-to-prod.sh --source target webapp
EOF
}

while [ $# -gt 0 ]; do
    case "$1" in
        --build)      DO_BUILD=true; shift ;;
        --no-restart) DO_RESTART=false; shift ;;
        --source)     JAR_SOURCE="${2:-}"; shift 2 ;;
        --source=*)   JAR_SOURCE="${1#*=}"; shift ;;
        -h|--help)    print_help; exit 0 ;;
        -*)           echo "❌ 알 수 없는 옵션: $1"; print_help; exit 1 ;;
        *)            SELECTED+=("$(echo "$1" | tr '[:upper:]' '[:lower:]')"); shift ;;
    esac
done

if [ "$JAR_SOURCE" != "deploy-output" ] && [ "$JAR_SOURCE" != "target" ]; then
    echo "❌ --source 값이 올바르지 않습니다: $JAR_SOURCE (deploy-output | target)"
    exit 1
fi

# ─────────────────────────────────────────────────────────────────────────────
# 4. 사전 점검
# ─────────────────────────────────────────────────────────────────────────────
if [ -z "$REMOTE_HOST" ] || [ -z "$REMOTE_USER" ]; then
    echo "❌ 운영 서버 접속 정보가 설정되지 않았습니다."
    echo "   스크립트 상단의 REMOTE_HOST / REMOTE_USER 를 채우거나 환경변수로 전달하세요."
    echo "   예: REMOTE_HOST=10.0.0.5 REMOTE_USER=deploy ./deploy-to-prod.sh"
    exit 1
fi

# SSH 연결 다중화(ControlMaster): 최초 1회만 인증하고 이후 scp/ssh 가 연결을 재사용
# → 모듈마다 비밀번호를 반복 입력하지 않아도 됨 (SSH 키 미사용 환경에서 특히 유용)
SSH_CTL="$HOME/.ssh/cm-vims-%r@%h:%p"
SSH_MUX_OPTS=(-o ControlMaster=auto -o "ControlPath=$SSH_CTL" -o ControlPersist=120)
SSH_OPTS=(-p "$REMOTE_SSH_PORT" -o ConnectTimeout=10 "${SSH_MUX_OPTS[@]}")
SCP_OPTS=(-P "$REMOTE_SSH_PORT" -o ConnectTimeout=10 "${SSH_MUX_OPTS[@]}")
REMOTE="$REMOTE_USER@$REMOTE_HOST"

# 종료 시 다중화 마스터 연결 정리
cleanup_mux() {
    ssh -O exit -o "ControlPath=$SSH_CTL" -p "$REMOTE_SSH_PORT" "$REMOTE" 2>/dev/null || true
}
trap cleanup_mux EXIT

echo "=============================================="
echo " VIMS 운영 배포 시작"
echo "=============================================="
echo "  대상 서버   : $REMOTE (port $REMOTE_SSH_PORT)"
echo "  컨테이너엔진 : $ENGINE"
echo "  JAR 소스    : $JAR_SOURCE"
echo "  빌드 수행   : $DO_BUILD / 재기동: $DO_RESTART"
echo "----------------------------------------------"

# SSH 연결 확인
if ! ssh "${SSH_OPTS[@]}" "$REMOTE" "command -v $ENGINE >/dev/null 2>&1"; then
    echo "❌ SSH 연결 실패 또는 원격 서버에 '$ENGINE' 명령이 없습니다."
    echo "   - SSH 키 기반 접속 설정 및 $ENGINE 설치/권한을 확인하세요."
    exit 1
fi
echo "✅ SSH 연결 및 $ENGINE 확인 완료"

# 스테이징 디렉터리 보장
ssh "${SSH_OPTS[@]}" "$REMOTE" "mkdir -p $REMOTE_STAGING_DIR"

# ─────────────────────────────────────────────────────────────────────────────
# 5. (옵션) 빌드
# ─────────────────────────────────────────────────────────────────────────────
if [ "$DO_BUILD" = true ]; then
    echo ""
    echo "=== 🏗️  전체 빌드 수행 (deploy-core-lib.sh) ==="
    if [ ! -x "$SCRIPT_DIR/deploy-core-lib.sh" ]; then
        echo "❌ deploy-core-lib.sh 를 찾을 수 없거나 실행 권한이 없습니다."
        exit 1
    fi
    if ! "$SCRIPT_DIR/deploy-core-lib.sh"; then
        echo "❌ 빌드 실패 — 배포를 중단합니다."
        exit 1
    fi
fi

# ─────────────────────────────────────────────────────────────────────────────
# 6. 모듈 선택 매칭
# ─────────────────────────────────────────────────────────────────────────────
is_selected() {
    # 인자: 표시명 디렉터리 컨테이너명 (모두 소문자 비교)
    [ ${#SELECTED[@]} -eq 0 ] && return 0
    local name="$1" dir="$2" cont="$3"
    local s
    for s in "${SELECTED[@]}"; do
        if [ "$s" = "$name" ] || [ "$s" = "$dir" ] || [ "$s" = "$cont" ]; then
            return 0
        fi
    done
    return 1
}

# ─────────────────────────────────────────────────────────────────────────────
# 7. 모듈별 배포
# ─────────────────────────────────────────────────────────────────────────────
RESULT_LINES=()

deploy_module() {
    local NAME="$1" DIR="$2" CONTAINER="$3" HOSTPORT="$4" INPORT="$5" PJAR="$6" TJAR="$7"

    echo ""
    echo "──────────────────────────────────────────────"
    echo "▶ [$NAME] 배포 시작 (컨테이너: $CONTAINER, 포트: $HOSTPORT)"

    # 7-1. JAR 경로 결정
    local JAR_PATH
    if [ "$JAR_SOURCE" = "deploy-output" ]; then
        JAR_PATH="$DEPLOY_OUTPUT_DIR/$PJAR"
    else
        JAR_PATH="$SCRIPT_DIR/$DIR/target/$TJAR"
    fi

    if [ ! -f "$JAR_PATH" ]; then
        echo "  ⚠️  JAR 없음: $JAR_PATH → 건너뜀"
        RESULT_LINES+=("SKIP|$NAME|JAR 없음 ($JAR_SOURCE)")
        return
    fi
    echo "  📦 JAR: $JAR_PATH ($(du -h "$JAR_PATH" | cut -f1))"

    # 7-2. scp 전송
    local STAGED="$REMOTE_STAGING_DIR/$CONTAINER.jar"
    echo "  ⬆️  전송 중 → $REMOTE:$STAGED"
    if ! scp "${SCP_OPTS[@]}" "$JAR_PATH" "$REMOTE:$STAGED"; then
        echo "  ❌ scp 전송 실패"
        RESULT_LINES+=("FAIL|$NAME|scp 전송 실패")
        return
    fi

    # 7-3. 원격 배포(컨테이너 주입 + 재기동 + 헬스체크)
    echo "  🚀 컨테이너 주입/재기동 중..."
    if ssh "${SSH_OPTS[@]}" "$REMOTE" \
        ENGINE="$ENGINE" CONTAINER="$CONTAINER" STAGED="$STAGED" \
        APP_DIR="$CONTAINER_APP_DIR" JAR="$CONTAINER_JAR_NAME" \
        HOSTPORT="$HOSTPORT" INPORT="$INPORT" RESTART="$DO_RESTART" TIMEOUT="$HEALTH_TIMEOUT" \
        'bash -s' <<'REMOTE_SCRIPT'
set -uo pipefail

# 컨테이너 존재 확인
if ! $ENGINE inspect "$CONTAINER" >/dev/null 2>&1; then
    echo "    ❌ 컨테이너를 찾을 수 없음: $CONTAINER"
    exit 10
fi

# 앱 디렉터리 보장
$ENGINE exec "$CONTAINER" sh -c "mkdir -p $APP_DIR" || { echo "    ❌ $APP_DIR 생성 실패"; exit 11; }

# 기존 JAR 백업
$ENGINE exec "$CONTAINER" sh -c "[ -f $APP_DIR/$JAR ] && cp $APP_DIR/$JAR $APP_DIR/$JAR.bak || true"

# 신규 JAR 복사
if ! $ENGINE cp "$STAGED" "$CONTAINER:$APP_DIR/$JAR"; then
    echo "    ❌ $ENGINE cp 실패"
    exit 12
fi
echo "    ✅ JAR 복사 완료: $CONTAINER:$APP_DIR/$JAR"

if [ "$RESTART" != "true" ]; then
    echo "    ℹ️  --no-restart: 재기동 생략"
    exit 0
fi

# 기존 java 프로세스 종료
$ENGINE exec "$CONTAINER" sh -c "pkill -f 'java -jar $JAR' 2>/dev/null || pkill -f 'java -jar $APP_DIR/$JAR' 2>/dev/null || true"
sleep 2

# 신규 기동 (백그라운드)
$ENGINE exec -d "$CONTAINER" sh -c "cd $APP_DIR && nohup java -jar $JAR > $APP_DIR/app.log 2>&1 &"
echo "    ⏳ 기동 대기 및 헬스체크 (최대 ${TIMEOUT}초, 컨테이너 내부 포트 $INPORT)..."

# 헬스체크: "컨테이너 내부"에서 앱이 실제로 포트를 LISTEN 하는지 확인한다.
#   - 호스트 포트(예: 9081)는 podman 이 컨테이너 기동만으로도 항상 listen 하므로
#     java 기동 여부를 보장하지 못한다. 따라서 내부 포트(예: 8081)로 직접 확인.
#   - eclipse-temurin:17-jdk(ubuntu 기반)에는 bash 가 있어 /dev/tcp 사용 가능.
ok=false
waited=0
while [ "$waited" -lt "$TIMEOUT" ]; do
    # java 프로세스가 떠 있고, 내부 앱 포트가 LISTEN 중인지 확인
    if $ENGINE exec "$CONTAINER" bash -c "pgrep -f 'java -jar $JAR' >/dev/null 2>&1 && (exec 3<>/dev/tcp/127.0.0.1/$INPORT) 2>/dev/null"; then
        ok=true
        break
    fi
    # java 프로세스 자체가 사라졌으면(즉시 죽음) 더 기다릴 필요 없음
    if ! $ENGINE exec "$CONTAINER" sh -c "pgrep -f 'java -jar $JAR' >/dev/null 2>&1"; then
        if [ "$waited" -ge 6 ]; then
            echo "    ❌ java 프로세스가 기동 직후 종료됨 — app.log 마지막 40줄:"
            $ENGINE exec "$CONTAINER" sh -c "tail -n 40 $APP_DIR/app.log" 2>/dev/null || true
            exit 21
        fi
    fi
    sleep 3
    waited=$((waited + 3))
done

if [ "$ok" = true ]; then
    echo "    ✅ 헬스체크 성공 (컨테이너 내부 포트 $INPORT LISTEN, java 프로세스 정상)"
    exit 0
else
    echo "    ❌ 헬스체크 실패 (${TIMEOUT}초 내 내부 포트 $INPORT 미응답) — app.log 마지막 40줄:"
    $ENGINE exec "$CONTAINER" sh -c "tail -n 40 $APP_DIR/app.log" 2>/dev/null || true
    exit 20
fi
REMOTE_SCRIPT
    then
        if [ "$DO_RESTART" = true ]; then
            echo "  🎉 [$NAME] 배포 + 재기동 완료"
            RESULT_LINES+=("OK|$NAME|배포+재기동 완료")
        else
            echo "  🎉 [$NAME] JAR 복사 완료 (재기동 생략)"
            RESULT_LINES+=("OK|$NAME|JAR 복사 완료(미재기동)")
        fi
    else
        echo "  ❌ [$NAME] 배포 실패"
        RESULT_LINES+=("FAIL|$NAME|원격 배포/재기동 실패")
    fi
}

MATCHED=0
for ENTRY in "${MODULES[@]}"; do
    IFS='|' read -r NAME DIR CONTAINER HOSTPORT INPORT PJAR TJAR <<< "$ENTRY"
    name_lc="$(echo "$NAME" | tr '[:upper:]' '[:lower:]')"
    if is_selected "$name_lc" "$DIR" "$CONTAINER"; then
        MATCHED=$((MATCHED + 1))
        deploy_module "$NAME" "$DIR" "$CONTAINER" "$HOSTPORT" "$INPORT" "$PJAR" "$TJAR"
    fi
done

if [ "$MATCHED" -eq 0 ]; then
    echo "❌ 선택한 모듈과 일치하는 항목이 없습니다: ${SELECTED[*]:-}"
    echo "   사용 가능: login, management, fms, webapp, gateway"
    exit 1
fi

# ─────────────────────────────────────────────────────────────────────────────
# 8. 결과 요약
# ─────────────────────────────────────────────────────────────────────────────
echo ""
echo "=============================================="
echo " 배포 결과 요약"
echo "=============================================="
FAIL_COUNT=0
for LINE in "${RESULT_LINES[@]}"; do
    IFS='|' read -r STATUS NAME MSG <<< "$LINE"
    case "$STATUS" in
        OK)   echo "  ✅ $NAME : $MSG" ;;
        SKIP) echo "  ⏭️  $NAME : $MSG" ;;
        FAIL) echo "  ❌ $NAME : $MSG"; FAIL_COUNT=$((FAIL_COUNT + 1)) ;;
    esac
done
echo "----------------------------------------------"
echo "  로그 확인(원격): $ENGINE exec <컨테이너> tail -f $CONTAINER_APP_DIR/app.log"
echo "=============================================="

[ "$FAIL_COUNT" -gt 0 ] && exit 1 || exit 0
