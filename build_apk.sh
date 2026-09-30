#!/bin/sh
set -eu
cd "$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
./gradlew --no-daemon assembleDebug
cp app/build/outputs/apk/debug/app-debug.apk HWT_Maker_GT6.apk
echo "APK hazır: HWT_Maker_GT6.apk"
