package com.openfinds.app.core.domain.model

/** A device this phone has securely paired with over the local network. */
data class TrustedDevice(
    val id: String,
    val displayName: String,
    val nickname: String?,
    val avatarColorArgb: Int,
    val publicKeyBase64: String,
    val lastKnownHost: String?,
    val lastKnownPort: Int?,
    val pairedAtEpochMillis: Long,
    val lastSeenEpochMillis: Long?,
    val connectionState: ConnectionState,
) {
    val name: String get() = nickname?.takeIf { it.isNotBlank() } ?: displayName
}

enum class ConnectionState { ONLINE, OFFLINE, CONNECTING }

/** A device discovered on the network but not yet trusted/paired. */
data class DiscoveredDevice(
    val serviceName: String,
    val host: String,
    val port: Int,
    val publicKeyBase64: String?,
)

/** Live hardware status pulled from a paired device over the encrypted channel. */
data class DeviceSnapshot(
    val batteryPercent: Int,
    val isCharging: Boolean,
    val storageUsedBytes: Long,
    val storageTotalBytes: Long,
    val ramUsedBytes: Long,
    val ramTotalBytes: Long,
    val uptimeMillis: Long,
    val capturedAtEpochMillis: Long,
)
