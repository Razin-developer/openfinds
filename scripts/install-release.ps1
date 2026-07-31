Set-Location (Join-Path $PSScriptRoot "..")
& .\gradlew.bat :app:assembleRelease
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
adb install -r app\build\outputs\apk\release\app-release.apk
adb shell am start -n com.openfinds.app/com.openfinds.app.MainActivity
