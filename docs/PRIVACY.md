# Privacy

This is the plain-English source for the in-app Privacy Policy screen (`feature/settings/StaticContentScreens.kt`).

## No servers, no accounts

OpenFind has no backend. There is nothing to sign up for, and nothing remote that could be breached, because there is no remote system at all.

## Data never leaves your Wi-Fi

Device discovery, pairing, and every command (ring, vibrate, flash, status) travel directly between your devices over your local network, encrypted end-to-end. None of it is sent to the internet.

## What's stored on this device

Trusted device records (name, nickname, public key, last-seen time) are stored locally in a Room database. You can forget any device at any time from its details screen, which deletes its record immediately. Settings (theme, background monitoring toggle, this device's display name) are stored in DataStore, also local-only.

## No analytics or crash reporting

OpenFind does not collect usage analytics or send crash reports anywhere. Diagnostic logs stay on-device and are only ever shared if you explicitly export them (planned — see [ROADMAP.md](../ROADMAP.md)).
