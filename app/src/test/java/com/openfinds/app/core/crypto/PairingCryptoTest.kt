package com.openfinds.app.core.crypto

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PairingCryptoTest {
    @Test
    fun `both sides derive the same session key from a shared ECDH secret`() {
        val (aPrivate, aPublic) = PairingCrypto.generateEphemeralKeyPair()
        val (bPrivate, bPublic) = PairingCrypto.generateEphemeralKeyPair()

        val secretFromA = PairingCrypto.computeSharedSecret(aPrivate, bPublic)
        val secretFromB = PairingCrypto.computeSharedSecret(bPrivate, aPublic)
        assertThat(secretFromA).isEqualTo(secretFromB)

        val transcript = aPublic + bPublic
        val keyFromA = PairingCrypto.deriveSessionKey(secretFromA, transcript, pin = "123456")
        val keyFromB = PairingCrypto.deriveSessionKey(secretFromB, transcript, pin = "123456")

        assertThat(keyFromA).isEqualTo(keyFromB)
        assertThat(keyFromA).hasLength(32)
    }

    @Test
    fun `a different PIN produces a different session key`() {
        val (aPrivate, aPublic) = PairingCrypto.generateEphemeralKeyPair()
        val (bPrivate, bPublic) = PairingCrypto.generateEphemeralKeyPair()
        val secret = PairingCrypto.computeSharedSecret(aPrivate, bPublic)
        val transcript = aPublic + bPublic

        val keyWithCorrectPin = PairingCrypto.deriveSessionKey(secret, transcript, pin = "111111")
        val keyWithWrongPin = PairingCrypto.deriveSessionKey(secret, transcript, pin = "222222")

        assertThat(keyWithCorrectPin).isNotEqualTo(keyWithWrongPin)
    }

    @Test
    fun `session cipher round-trips plaintext and rejects tampering`() {
        val key =
            PairingCrypto.deriveSessionKey(
                sharedSecret = ByteArray(32) { it.toByte() },
                transcript = ByteArray(16) { 1 },
            )
        val cipher = SessionCipher(key)
        val plaintext = "ring".toByteArray()

        val ciphertext = cipher.encrypt(plaintext)
        assertThat(cipher.decrypt(ciphertext)).isEqualTo(plaintext)

        val tampered = ciphertext.copyOf().also { it[0] = it[0].inc() }
        org.junit.Assert.assertThrows(Exception::class.java) { cipher.decrypt(tampered) }
    }

    @Test
    fun `generated PINs are always six digits`() {
        repeat(50) {
            val pin = PairingCrypto.generatePin()
            assertThat(pin).hasLength(6)
            assertThat(pin.toIntOrNull()).isNotNull()
        }
    }
}
