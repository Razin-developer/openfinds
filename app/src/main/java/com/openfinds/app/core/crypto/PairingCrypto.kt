package com.openfinds.app.core.crypto

import com.google.crypto.tink.subtle.Hkdf
import com.google.crypto.tink.subtle.X25519
import java.security.SecureRandom

/**
 * Key-agreement and session-key derivation for the pairing handshake.
 *
 * Both QR and PIN pairing run the same X25519 ECDH + HKDF derivation; PIN
 * pairing additionally mixes the human-verified PIN into the HKDF salt, so a
 * completed handshake proves both devices agree on the PIN and not just that
 * they can reach each other on the LAN. This is a lightweight, documented
 * trade-off versus a full PAKE (e.g. SPAKE2) — see SECURITY.md.
 */
object PairingCrypto {

    fun generateEphemeralKeyPair(): Pair<ByteArray, ByteArray> {
        val privateKey = X25519.generatePrivateKey()
        return privateKey to X25519.publicFromPrivate(privateKey)
    }

    fun generatePin(): String {
        val random = SecureRandom()
        return (100000 + random.nextInt(900000)).toString()
    }

    /**
     * Derives the 32-byte AES-256-GCM session key shared by both peers.
     *
     * @param sharedSecret raw X25519 ECDH output
     * @param transcript both public keys concatenated in a fixed, agreed order
     *   (binds the derived key to this exact handshake, preventing replay
     *   across sessions)
     * @param pin optional human-verified PIN; when present its UTF-8 bytes are
     *   folded into the HKDF salt
     */
    fun deriveSessionKey(sharedSecret: ByteArray, transcript: ByteArray, pin: String? = null): ByteArray {
        val salt = if (pin != null) transcript + pin.toByteArray(Charsets.UTF_8) else transcript
        return Hkdf.computeHkdf(
            "HMACSHA256",
            sharedSecret,
            salt,
            SESSION_KEY_INFO,
            32,
        )
    }

    fun computeSharedSecret(privateKey: ByteArray, peerPublicKey: ByteArray): ByteArray =
        X25519.computeSharedSecret(privateKey, peerPublicKey)

    private val SESSION_KEY_INFO = "OpenFind-Session-v1".toByteArray(Charsets.UTF_8)
}
