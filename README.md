# OpenFind

**A privacy-first, local-network Android device finder.** OpenFind discovers your other trusted Android devices on the same Wi-Fi, pairs with them securely (QR code or PIN), and lets you locate, ring, vibrate, flash, and inspect them — with zero servers, zero accounts, and zero data ever leaving your network.

## Why OpenFind

Commercial "find my device" apps route everything through a company's cloud. OpenFind doesn't have one. Every feature — discovery, pairing, ringing a lost phone, checking its battery — happens directly between your devices over your local Wi-Fi, encrypted end-to-end with keys that never leave the Android Keystore.

## Features (current release)

- Secure onboarding with up-front, per-feature permission education
- Local-network device discovery (mDNS/NSD with a UDP broadcast fallback for routers that block multicast)
- Encrypted pairing via QR code or 6-digit PIN (X25519 ECDH + HKDF + AES-256-GCM, Android Keystore-protected identity keys)
- Trusted device dashboard: battery, charging state, storage, RAM, uptime, last seen
- Ring, vibrate, and flashlight "Find my device" with a full-screen locate mode
- Background monitoring via a foreground service, with auto-reconnect (WorkManager), boot-start, and network-change handling
- Device nicknames, search/filter/sort
- Settings, About, Privacy Policy, and open-source licenses screens
- Material 3 UI with full dark/light theming

See [ROADMAP.md](ROADMAP.md) for what's planned next (device groups, history log, diagnostics/log export, developer options, richer notification channels).

## Tech stack

Kotlin, Jetpack Compose + Material 3, type-safe Compose Navigation, MVVM + Clean Architecture, Hilt, Room, DataStore, Kotlin Coroutines/Flow, Kotlin Serialization, Android Keystore + Google Tink, Timber, WorkManager, Foreground Services, Network Service Discovery + UDP, CameraX + ZXing (QR), Coil.

## Architecture

OpenFind is a single Gradle module organized by feature and layer (see [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for the full breakdown and diagrams):

```
app/src/main/java/com/openfinds/app/
├── core/
│   ├── crypto/        # Keystore-backed identity, X25519/HKDF/AEAD pairing crypto
│   ├── network/       # NSD/UDP discovery, TCP P2P protocol, device status/actions
│   ├── data/          # Room database, DataStore preferences
│   ├── domain/        # Domain models + repository interfaces
│   ├── background/    # Foreground service, WorkManager, boot/network receivers
│   ├── navigation/     # Type-safe nav graph
│   ├── ui/            # Theme, shared composables, formatting helpers
│   └── di/            # Hilt modules
└── feature/
    ├── onboarding/     # Welcome, permissions
    ├── pairing/        # QR + PIN pairing
    ├── home/           # Dashboard
    ├── devices/        # Device list + details
    ├── find/           # Full-screen find mode
    └── settings/       # Settings, about, privacy, licenses
```

## Building

Requirements: JDK 17+, Android SDK (compileSdk 35, minSdk 26).

```bash
./gradlew :app:assembleDebug     # -> app/build/outputs/apk/debug/app-debug.apk
./gradlew :app:assembleRelease   # -> app/build/outputs/apk/release/app-release.apk
./gradlew :app:bundleRelease     # -> app/build/outputs/bundle/release/app-release.aab
```

Convenience scripts live in [scripts/](scripts/) (bash and PowerShell): `build-debug`, `build-release`, `install-debug`, `install-release`. A release build is signed only if `keystore.properties` exists at the repo root — copy [keystore.properties.example](keystore.properties.example) and fill it in.

## Testing

```bash
./gradlew test               # unit tests
./gradlew connectedAndroidTest # instrumented tests (requires a device/emulator)
```

## Security & privacy

See [SECURITY.md](SECURITY.md) for the threat model, cryptographic design, and how to report a vulnerability. See [docs/PRIVACY.md](docs/PRIVACY.md) for a plain-English summary (also shown in-app under Settings → Privacy policy).

## Contributing

Contributions are welcome — see [CONTRIBUTING.md](CONTRIBUTING.md). Please also read the [Code of Conduct](CODE_OF_CONDUCT.md).

## License

OpenFind is licensed under the [Apache License 2.0](LICENSE).
