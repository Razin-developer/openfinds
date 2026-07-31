Set-Location (Join-Path $PSScriptRoot "..")

if (-not (Test-Path "keystore.properties")) {
    Write-Warning "keystore.properties not found -- building an UNSIGNED release."
    Write-Warning "Copy keystore.properties.example to keystore.properties to sign it."
}

& .\gradlew.bat :app:assembleRelease :app:bundleRelease
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
Write-Host ""
Write-Host "Release APK: app\build\outputs\apk\release\app-release.apk"
Write-Host "Release AAB: app\build\outputs\bundle\release\app-release.aab"
