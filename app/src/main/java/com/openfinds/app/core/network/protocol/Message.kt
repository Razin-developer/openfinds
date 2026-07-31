package com.openfinds.app.core.network.protocol

import kotlinx.serialization.Serializable

/** Wire format for every frame exchanged over an OpenFind P2P TCP session. */
@Serializable
sealed interface P2pMessage {

    /** Sent by the connecting device to start (or resume) a session. */
    @Serializable
    data class HandshakeInit(
        val fromDeviceId: String,
        val fromDeviceName: String,
        val ephemeralPublicKeyB64: String,
        val pairingMode: PairingMode,
    ) : P2pMessage

    /** Sent by the accepting device with its own ephemeral key. */
    @Serializable
    data class HandshakeResponse(
        val fromDeviceId: String,
        val fromDeviceName: String,
        val ephemeralPublicKeyB64: String,
        val identityPublicKeyB64: String,
    ) : P2pMessage

    /** First message encrypted under the newly derived session key, proving both sides agree on it. */
    @Serializable
    data class HandshakeConfirm(val identityPublicKeyB64: String) : P2pMessage

    @Serializable
    data class Command(val action: DeviceAction) : P2pMessage

    @Serializable
    data class StatusResponse(
        val batteryPercent: Int,
        val isCharging: Boolean,
        val storageUsedBytes: Long,
        val storageTotalBytes: Long,
        val ramUsedBytes: Long,
        val ramTotalBytes: Long,
        val uptimeMillis: Long,
    ) : P2pMessage

    @Serializable
    data class Ack(val forAction: DeviceAction? = null) : P2pMessage

    @Serializable
    data class Error(val reason: String) : P2pMessage
}

@Serializable
enum class PairingMode { QR, PIN, RECONNECT }

@Serializable
enum class DeviceAction { RING, VIBRATE, FLASH, STOP_FIND, STATUS_REQUEST }
