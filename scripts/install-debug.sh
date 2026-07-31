#!/usr/bin/env bash
# Builds and installs the debug build on a connected device/emulator via adb.
set -euo pipefail
cd "$(dirname "$0")/.."
./gradlew :app:installDebug
adb shell am start -n com.openfinds.app.debug/com.openfinds.app.MainActivity
