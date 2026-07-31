Set-Location (Join-Path $PSScriptRoot "..")
& .\gradlew.bat :app:installDebug
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
adb shell am start -n com.openfinds.app.debug/com.openfinds.app.MainActivity
