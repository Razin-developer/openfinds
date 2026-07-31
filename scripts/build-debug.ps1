Set-Location (Join-Path $PSScriptRoot "..")
& .\gradlew.bat :app:assembleDebug
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
Write-Host ""
Write-Host "Debug APK: app\build\outputs\apk\debug\app-debug.apk"
