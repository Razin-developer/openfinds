# ADR 0002: X25519 + HKDF pairing instead of a full PAKE

## Status
Accepted, with a documented upgrade path.

## Context
OpenFind needs to establish an authenticated, encrypted channel between two devices that have never communicated before, using either a scanned QR code or a human-read 6-digit PIN as the trust anchor. The gold-standard approach for PIN-based pairing is a Password-Authenticated Key Exchange (PAKE) such as SPAKE2, which guarantees that an attacker who doesn't know the PIN learns nothing from observing the handshake, even offline.

## Decision
For v0.1.0, OpenFind runs a standard X25519 Diffie-Hellman key exchange between ephemeral keypairs, then derives the session key with HKDF-HMAC-SHA256, using the PIN's UTF-8 bytes folded into the HKDF salt for PIN-based pairing. A handshake only completes if both sides derive the same session key, which requires knowing the same PIN. For QR-based pairing, the scanned QR code carries the responder's real public key, so no PIN is needed — the initiator refuses to complete the handshake if the peer that answers doesn't hold the matching private key.

## Consequences
- Much simpler to implement and audit than a full PAKE, using only primitives already provided by Tink (`X25519`, `Hkdf`, `AesGcmJce`).
- An attacker who can observe the handshake AND guess PINs offline could, in principle, mount an offline dictionary attack against the PIN if they also captured a completed handshake transcript — a full PAKE would prevent this. Given PINs are short-lived (single pairing session, user-visible for ~60 seconds) and OpenFind's threat model is "attacker on the same Wi-Fi," this is an accepted, documented trade-off (see SECURITY.md).
- Upgrade path: replace the PIN-mixed-HKDF step with SPAKE2 without changing the surrounding protocol (handshake message shapes stay the same) — tracked in ROADMAP.md.
