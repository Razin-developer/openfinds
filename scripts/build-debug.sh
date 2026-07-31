#!/usr/bin/env bash
# Builds the debug APK. Output: app/build/outputs/apk/debug/app-debug.apk
set -euo pipefail
cd "$(dirname "$0")/.."
./gradlew :app:assembleDebug
echo
echo "Debug APK: app/build/outputs/apk/debug/app-debug.apk"
