#!/bin/bash

echo "🚀 UI 테스트 자동 실행 시작..."

# 에뮬레이터 상태 확인
echo "📱 에뮬레이터 상태 확인 중..."
adb devices | grep -q "emulator"
if [ $? -ne 0 ]; then
    echo "❌ 에뮬레이터가 실행되지 않았습니다. 에뮬레이터를 먼저 실행해주세요."
    exit 1
fi

# UI 테스트 실행
echo "🧪 UI 테스트 실행 중..."
./gradlew connectedDebugAndroidTest

# 결과 확인
if [ $? -eq 0 ]; then
    echo "✅ 모든 UI 테스트가 성공했습니다!"
    echo "📊 테스트 결과: app/build/reports/androidTests/connected/debug/index.html"
else
    echo "❌ 일부 UI 테스트가 실패했습니다."
    echo "📊 실패 상세: app/build/reports/androidTests/connected/debug/index.html"
    exit 1
fi

echo "🎉 UI 테스트 자동 실행 완료!" 