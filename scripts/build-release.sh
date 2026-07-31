#!/usr/bin/env bash
# Builds the release APK and AAB. Signed if keystore.properties exists at the
# repo root (see keystore.properties.example), otherwise built unsigned.
set -euo pipefail
cd "$(dirname "$0")/.."

if [ ! -f keystore.properties ]; then
  echo "Warning: keystore.properties not found — building an UNSIGNED release." >&2
  echo "Copy keystore.properties.example to keystore.properties to sign it." >&2
fi

./gradlew :app:assembleRelease :app:bundleRelease
echo
echo "Release APK: app/build/outputs/apk/release/app-release.apk"
echo "Release AAB: app/build/outputs/bundle/release/app-release.aab"
