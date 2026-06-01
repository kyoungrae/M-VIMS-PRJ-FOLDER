#!/bin/bash
# vims-inspection 패키징 스크립트

echo "=== vims-inspection Clean & Package 시작 ==="

# Maven 명령어 찾기
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
    )
    for PATH_CHECK in "${POSSIBLE_PATHS[@]}"; do
        if [ -f "$PATH_CHECK" ]; then
            MVN_CMD="$PATH_CHECK"
            FOUND_MVN=true
            echo "IntelliJ 내장 Maven 사용: $MVN_CMD"
            break
        fi
    done
fi

if [ "$FOUND_MVN" = false ]; then
    echo "❌ 'mvn' 명령어를 찾을 수 없습니다."
    exit 1
fi

cd vims-inspection
echo "vims-inspection 빌드 실행..."
"$MVN_CMD" clean package -DskipTests -Dmaven.javadoc.skip=true
if [ $? -ne 0 ]; then
    echo "❌ vims-inspection 빌드 실패"
    exit 1
fi
echo "✅ vims-inspection 빌드 성공!"
ls -lh target/*.jar
cd ..
