package com.openfinds.app.core.crypto

import com.google.crypto.tink.subtle.AesGcmJce

/**
 * Encrypts/decrypts every message on an established P2P session using
 * Tink's audited AES-256-GCM implementation, keyed by the session key
 * produced by [PairingCrypto.deriveSessionKey].
 */
class SessionCipher(sessionKey: ByteArray) {

    private val aead = AesGcmJce(sessionKey)

    fun encrypt(plaintext: ByteArray, associatedData: ByteArray = ByteArray(0)): ByteArray =
        aead.encrypt(plaintext, associatedData)

    fun decrypt(ciphertext: ByteArray, associatedData: ByteArray = ByteArray(0)): ByteArray =
        aead.decrypt(ciphertext, associatedData)
}
