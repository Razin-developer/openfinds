package com.openfinds.app.core.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.openfinds.app.core.domain.model.HistoryEvent
import com.openfinds.app.core.domain.model.HistoryEventType

@Entity(tableName = "device_history_events")
data class DeviceHistoryEventEntity(
    @PrimaryKey val id: String,
    val deviceId: String,
    val deviceName: String,
    val eventType: String,
    val timestampEpochMillis: Long,
    val detail: String? = null,
)

fun DeviceHistoryEventEntity.toDomain(): HistoryEvent =
    HistoryEvent(
        id = id,
        deviceId = deviceId,
        deviceName = deviceName,
        type = runCatching { HistoryEventType.valueOf(eventType) }.getOrDefault(HistoryEventType.OTHER),
        timestampEpochMillis = timestampEpochMillis,
        detail = detail,
    )
