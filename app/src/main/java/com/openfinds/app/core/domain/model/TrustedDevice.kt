package com.openfinds.app.core.domain.model

/** A device this phone has securely paired with over the local network. */
data class TrustedDevice(
    val id: String,
    val displayName: String,
    val nickname: String?,
    val avatarColorArgb: Int,
    val avatarImageUri: String? = null,
    val publicKeyBase64: String,
    val lastKnownHost: String?,
    val lastKnownPort: Int?,
    val pairedAtEpochMillis: Long,
    val lastSeenEpochMillis: Long?,
    val groupId: String? = null,
    val connectionState: ConnectionState,
) {
    val name: String get() = nickname?.takeIf { it.isNotBlank() } ?: displayName
}

enum class ConnectionState { ONLINE, OFFLINE, CONNECTING }

/** A device discovered on the network but not yet trusted/paired. */
data class DiscoveredDevice(
    val deviceId: String,
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

/** A user-created label for organizing trusted devices (e.g. "Family", "Work"). */
data class DeviceGroup(
    val id: String,
    val name: String,
    val colorArgb: Int,
    val createdAtEpochMillis: Long,
    val deviceCount: Int,
)

enum class HistoryEventType { PAIRED, FORGOTTEN, CONNECTED, DISCONNECTED, FIND_TRIGGERED, RENAMED, OTHER }

/** A single logged event for the device history feed. */
data class HistoryEvent(
    val id: String,
    val deviceId: String,
    val deviceName: String,
    val type: HistoryEventType,
    val timestampEpochMillis: Long,
    val detail: String? = null,
)
