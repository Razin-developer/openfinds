# Roadmap

OpenFind's first release (`v0.1.0`) covers the core vertical slice: discovery, pairing, dashboard, and find mode. Planned next:

## Near-term
- Device groups (organize trusted devices into named groups)
- Device history log (connection/pairing events, exportable)
- Diagnostics screen + on-device log export
- Developer options screen (protocol/log verbosity toggles)
- Richer notification channels: grouped notifications, inline pairing accept/reject actions, low-battery alerts for trusted devices

## Hardening
- Replace PIN-mixed-HKDF pairing with a proper PAKE (SPAKE2) — see [SECURITY.md](SECURITY.md)
- Optional out-of-band fingerprint verification for QR pairing

## Engineering
- Split into Gradle feature modules (currently a single `:app` module organized by package, to keep the initial build simple)
- Full instrumented UI test suite (Compose UI tests) and CI matrix across API levels
- Architecture Decision Records (ADRs) for major design choices
