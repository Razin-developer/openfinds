package com.openfinds.app.feature.pairing

import com.openfinds.app.core.network.json.OpenFindJson
import kotlinx.serialization.Serializable

/** What this device's pairing QR code encodes: enough for a scanner to connect and authenticate it. */
@Serializable
data class QrPairingPayload(
    val deviceId: String,
    val deviceName: String,
    val host: String,
    val port: Int,
    val identityPublicKeyB64: String,
) {
    fun encode(): String = OpenFindJson.encodeToString(serializer(), this)

    companion object {
        fun decode(raw: String): QrPairingPayload? =
            runCatching {
                OpenFindJson.decodeFromString(serializer(), raw)
            }.getOrNull()
    }
}
