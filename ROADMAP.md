# Roadmap

OpenFind's first release covers discovery, pairing, dashboard, find mode, groups, history, diagnostics, developer options, rich notifications, and BLE-assisted discovery. Planned next:

## Near-term
- History log filtering by device/event type, and per-device history export
- Inline accept/reject actions directly on the pairing-request notification (currently opens the app)
- Notification-level toggles for individual alert types beyond the OS channel settings screen

## Hardening
- Replace PIN-mixed-HKDF pairing with a proper PAKE (SPAKE2) — see [SECURITY.md](SECURITY.md)
- Optional out-of-band fingerprint verification for QR pairing

## Engineering
- Split into Gradle feature modules (see [ADR 0006](docs/adr/0006-split-into-gradle-feature-modules.md) — proposed but deliberately not yet executed, to avoid destabilizing a verified, working build without on-device re-testing available in this environment)
- CI matrix across multiple API levels for instrumented tests
- Detekt custom rules specific to the crypto/network layers (e.g. flag any new cleartext socket usage)
