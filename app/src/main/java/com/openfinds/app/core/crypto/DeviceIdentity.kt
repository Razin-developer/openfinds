package com.openfinds.app.core.crypto

/**
 * This device's long-term X25519 identity keypair. [privateKey] must never
 * leave [DeviceIdentityStore] in plaintext form — it is always read back
 * still wrapped by the Android Keystore and only unwrapped in memory.
 */
data class DeviceIdentity(
    val deviceId: String,
    val publicKey: ByteArray,
    val privateKey: ByteArray,
) {
    override fun equals(other: Any?): Boolean =
        other is DeviceIdentity && deviceId == other.deviceId && publicKey.contentEquals(other.publicKey)

    override fun hashCode(): Int = deviceId.hashCode() * 31 + publicKey.contentHashCode()
}
