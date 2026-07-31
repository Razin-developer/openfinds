package com.openfinds.app.core.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.openfinds.app.core.domain.model.TrustedDevice

@Entity(tableName = "trusted_devices")
data class TrustedDeviceEntity(
    @PrimaryKey val id: String,
    val displayName: String,
    val nickname: String?,
    val avatarColorArgb: Int,
    val publicKeyBase64: String,
    val lastKnownHost: String?,
    val lastKnownPort: Int?,
    val pairedAtEpochMillis: Long,
    val lastSeenEpochMillis: Long?,
)

fun TrustedDeviceEntity.toDomain(isOnline: Boolean, isConnecting: Boolean): TrustedDevice = TrustedDevice(
    id = id,
    displayName = displayName,
    nickname = nickname,
    avatarColorArgb = avatarColorArgb,
    publicKeyBase64 = publicKeyBase64,
    lastKnownHost = lastKnownHost,
    lastKnownPort = lastKnownPort,
    pairedAtEpochMillis = pairedAtEpochMillis,
    lastSeenEpochMillis = lastSeenEpochMillis,
    connectionState = when {
        isConnecting -> com.openfinds.app.core.domain.model.ConnectionState.CONNECTING
        isOnline -> com.openfinds.app.core.domain.model.ConnectionState.ONLINE
        else -> com.openfinds.app.core.domain.model.ConnectionState.OFFLINE
    },
)
