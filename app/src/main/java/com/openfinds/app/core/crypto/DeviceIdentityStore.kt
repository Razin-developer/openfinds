package com.openfinds.app.core.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.crypto.tink.subtle.X25519
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.security.KeyStore
import java.util.Base64
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persists this device's X25519 identity keypair, encrypting the private key
 * at rest with an AES-256-GCM key that lives only inside the Android
 * Keystore (hardware-backed where available) and never leaves it. Tink's
 * [X25519] supplies the actual key-agreement primitive used by the pairing
 * protocol; Keystore only protects storage of the resulting private key.
 */
@Singleton
class DeviceIdentityStore
    @Inject
    constructor(
        private val dataStore: DataStore<Preferences>,
    ) {
        private object Keys {
            val DEVICE_ID = stringPreferencesKey("identity_device_id")
            val PUBLIC_KEY = stringPreferencesKey("identity_public_key")
            val WRAPPED_PRIVATE_KEY = stringPreferencesKey("identity_wrapped_private_key")
            val WRAP_IV = stringPreferencesKey("identity_wrap_iv")
        }

        private val mutex = Mutex()

        @Volatile
        private var cached: DeviceIdentity? = null

        suspend fun getOrCreate(): DeviceIdentity =
            mutex.withLock {
                cached?.let { return it }

                val prefs = dataStore.data.first()
                val existing = readIdentity(prefs)
                val identity = existing ?: createAndPersistIdentity()
                cached = identity
                identity
            }

        private fun readIdentity(prefs: Preferences): DeviceIdentity? {
            val deviceId = prefs[Keys.DEVICE_ID] ?: return null
            val publicKeyB64 = prefs[Keys.PUBLIC_KEY] ?: return null
            val wrappedB64 = prefs[Keys.WRAPPED_PRIVATE_KEY] ?: return null
            val ivB64 = prefs[Keys.WRAP_IV] ?: return null

            val privateKey =
                unwrapPrivateKey(
                    wrapped = Base64.getDecoder().decode(wrappedB64),
                    iv = Base64.getDecoder().decode(ivB64),
                )
            return DeviceIdentity(
                deviceId = deviceId,
                publicKey = Base64.getDecoder().decode(publicKeyB64),
                privateKey = privateKey,
            )
        }

        private suspend fun createAndPersistIdentity(): DeviceIdentity {
            val privateKey = X25519.generatePrivateKey()
            val publicKey = X25519.publicFromPrivate(privateKey)
            val deviceId = UUID.randomUUID().toString()

            val (wrapped, iv) = wrapPrivateKey(privateKey)

            dataStore.edit { prefs ->
                prefs[Keys.DEVICE_ID] = deviceId
                prefs[Keys.PUBLIC_KEY] = Base64.getEncoder().encodeToString(publicKey)
                prefs[Keys.WRAPPED_PRIVATE_KEY] = Base64.getEncoder().encodeToString(wrapped)
                prefs[Keys.WRAP_IV] = Base64.getEncoder().encodeToString(iv)
            }

            return DeviceIdentity(deviceId, publicKey, privateKey)
        }

        private fun wrapPrivateKey(privateKey: ByteArray): Pair<ByteArray, ByteArray> {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateWrappingKey())
            val ciphertext = cipher.doFinal(privateKey)
            return ciphertext to cipher.iv
        }

        private fun unwrapPrivateKey(
            wrapped: ByteArray,
            iv: ByteArray,
        ): ByteArray {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateWrappingKey(), GCMParameterSpec(GCM_TAG_BITS, iv))
            return cipher.doFinal(wrapped)
        }

        private fun getOrCreateWrappingKey(): SecretKey {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE_PROVIDER).apply { load(null) }
            (keyStore.getKey(WRAPPING_KEY_ALIAS, null) as? SecretKey)?.let { return it }

            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE_PROVIDER)
            val spec =
                KeyGenParameterSpec.Builder(
                    WRAPPING_KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()
            keyGenerator.init(spec)
            return keyGenerator.generateKey()
        }

        private companion object {
            const val ANDROID_KEYSTORE_PROVIDER = "AndroidKeyStore"
            const val WRAPPING_KEY_ALIAS = "openfind_identity_wrap_key"
            const val TRANSFORMATION = "AES/GCM/NoPadding"
            const val GCM_TAG_BITS = 128
        }
    }
