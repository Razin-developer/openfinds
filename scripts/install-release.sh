#!/usr/bin/env bash
# Builds and installs the release build on a connected device/emulator via adb.
# Requires a signed build (see keystore.properties.example) — most devices
# refuse to install an unsigned or debug-signed release APK update over itself.
set -euo pipefail
cd "$(dirname "$0")/.."
./gradlew :app:assembleRelease
adb install -r app/build/outputs/apk/release/app-release.apk
adb shell am start -n com.openfinds.app/com.openfinds.app.MainActivity
