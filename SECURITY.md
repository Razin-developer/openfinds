# Security Policy

## Reporting a vulnerability

Please **do not** open a public GitHub issue for security vulnerabilities. Instead, use GitHub's [private vulnerability reporting](https://docs.github.com/en/code-security/security-advisories/guidance-on-reporting-and-writing/privately-reporting-a-security-vulnerability) on this repository (Security tab → "Report a vulnerability"). You should receive an acknowledgement within a few days.

Please include:
- A description of the vulnerability and its impact
- Steps to reproduce (a minimal repro is ideal)
- The OpenFind version / commit and Android version(s) affected

## Threat model

OpenFind's design goal is: **a passive or active attacker on the same Wi-Fi network cannot read, forge, or replay commands between two paired OpenFind devices, and cannot impersonate a trusted device.**

What OpenFind protects against:
- **Network eavesdropping** — all P2P traffic after pairing is encrypted with AES-256-GCM under a key derived via X25519 ECDH + HKDF; nothing is sent in plaintext.
- **Device impersonation after pairing** — every message is authenticated under the session key, and reconnecting to a "trusted" device verifies its long-term public key matches what was recorded at pairing time.
- **At-rest key theft** — this device's long-term identity private key is encrypted with an AES-256-GCM key that is generated inside, and never leaves, the Android Keystore (hardware-backed on supported devices).

What OpenFind does **not** fully protect against (known trade-offs):
- **QR pairing** trusts whatever public key is embedded in the scanned QR code. If an attacker can substitute the QR code itself (e.g., print a sticker over it), pairing would trust the attacker. Scan QR codes you can visually verify came from the device you intend to pair.
- **PIN pairing** mixes the PIN into the session key derivation rather than running a full password-authenticated key exchange (PAKE, e.g. SPAKE2). This is a lightweight, practical trade-off for v0.1 — see the "Planned hardening" note below.
- OpenFind assumes the Android OS and Keystore implementation on both devices are not themselves compromised.

## Cryptographic design

- **Identity keys**: X25519 keypair per device, generated with Tink's `X25519` primitive. The private key is wrapped with an AES-256-GCM key generated inside the Android Keystore (`AndroidKeyStore` provider) before being persisted, so the raw private key is never written to disk unencrypted, and the wrapping key never leaves secure hardware.
- **Handshake**: an ephemeral X25519 keypair is generated per pairing/reconnect attempt. Both sides compute a shared secret via ECDH, then derive a 32-byte session key with HKDF-HMAC-SHA256, using the concatenated ephemeral public keys (and, for PIN pairing, the PIN itself) as salt. This binds the session key to that specific handshake transcript.
- **Session encryption**: all post-handshake protocol messages (ring/vibrate/flash commands, status responses) are encrypted with Tink's `AesGcmJce` (AES-256-GCM) under the derived session key.
- **Framing**: length-prefixed frames over TCP prevent message-boundary confusion attacks.

## Planned hardening

Tracked for a future release (see [ROADMAP.md](ROADMAP.md)):
- Replace PIN-mixed-into-HKDF with a proper PAKE (SPAKE2) so an attacker who only observes the handshake (without knowing the PIN) gains zero information, even offline.
- Optional certificate pinning / out-of-band fingerprint verification for QR pairing.
