#!/bin/bash
# 전체 프로젝트 Clean & Package 스크립트

echo "=== 🧹 전체 프로젝트 Clean & Package 시작 ==="

# Maven 명령어 찾기
MVN_CMD="mvn"
FOUND_MVN=false

if command -v mvn &> /dev/null; then
    FOUND_MVN=true
else
    # 흔한 IntelliJ Maven 경로 탐색 (Mac OS 기준)
    POSSIBLE_PATHS=(
        "/Applications/IntelliJ IDEA.app/Contents/plugins/maven/lib/maven3/bin/mvn"
        "/Applications/IntelliJ IDEA CE.app/Contents/plugins/maven/lib/maven3/bin/mvn"
        "$HOME/Applications/IntelliJ IDEA.app/Contents/plugins/maven/lib/maven3/bin/mvn"
        "$HOME/Applications/IntelliJ IDEA CE.app/Contents/plugins/maven/lib/maven3/bin/mvn"
    )
    
    for PATH_CHECK in "${POSSIBLE_PATHS[@]}"; do
        if [ -f "$PATH_CHECK" ]; then
            MVN_CMD="$PATH_CHECK"
            FOUND_MVN=true
            echo "ℹ️ IntelliJ 내장 Maven을 찾았습니다: $MVN_CMD"
            break
        fi
    done
fi

if [ "$FOUND_MVN" = false ]; then
    echo "❌ 'mvn' 명령어를 찾을 수 없습니다. Maven을 설치하거나 경로를 확인해주세요."
    exit 1
fi

echo "✅ Maven 명령어 사용: $MVN_CMD"
echo ""

# 1. Core-lib 빌드 (Clean & Install)
echo "=== 1️⃣ Core-lib 빌드 (Libraries) ==="
cd Core-lib
echo "🚀 Core-lib 정리 및 빌드 중..."
"$MVN_CMD" clean install -DskipTests -Dmaven.javadoc.skip=true
if [ $? -ne 0 ]; then
    echo "❌ Core-lib 빌드 실패"
    exit 1
fi
echo "✅ Core-lib 빌드 성공!"
cd ..

# Core-lib JAR 파일 경로
CORE_LIB_JAR="Core-lib/core-lib/target/core-lib-1.0.jar"

if [ ! -f "$CORE_LIB_JAR" ]; then
    echo "❌ Core-lib JAR 파일을 찾을 수 없습니다: $CORE_LIB_JAR"
    exit 1
fi

echo ""
echo "=== 2️⃣ Core-lib 라이브러리 배포 ==="
# 각 프로젝트의 lib 폴더에 복사
PROJECTS_NEEDING_LIB=(
    "vims-management-system"
    "vims-login"
    "FMS"
    "vims-web-app"
    "vims-register"
    "vims-inspection"
)

for PROJ in "${PROJECTS_NEEDING_LIB[@]}"; do
    mkdir -p "$PROJ/src/lib"
    cp "$CORE_LIB_JAR" "$PROJ/src/lib/"
    echo "📦 $PROJ 에 라이브러리 복사 완료"
done

echo ""
echo "=== 3️⃣ 각 애플리케이션 Clean & Package ==="

# 빌드할 애플리케이션 목록
APPS=(
    "vims-login"
    "vims-management-system"
    "vims-gateway"
    "FMS"
    "vims-web-app"
    "vims-register"
    "vims-inspection"
)

for APP in "${APPS[@]}"; do
    echo "------------------------------------------------"
    echo "🏗️  $APP 빌드 중..."
    cd "$APP"
    
    # Clean & Package 실행
    "$MVN_CMD" clean package -DskipTests -Dmaven.javadoc.skip=true
    
    if [ $? -ne 0 ]; then
        echo "❌ $APP 빌드 실패!"
        exit 1
    fi
    
    echo "✅ $APP 빌드 성공!"
    cd ..
done

echo ""
echo "=== 🎉 모든 프로젝트 Clean & Package 완료! ==="
