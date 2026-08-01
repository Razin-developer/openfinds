package com.openfinds.app.core.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.openfinds.app.core.domain.model.DeviceGroup

@Entity(tableName = "device_groups")
data class DeviceGroupEntity(
    @PrimaryKey val id: String,
    val name: String,
    val colorArgb: Int,
    val createdAtEpochMillis: Long,
)

fun DeviceGroupEntity.toDomain(deviceCount: Int): DeviceGroup =
    DeviceGroup(
        id = id,
        name = name,
        colorArgb = colorArgb,
        createdAtEpochMillis = createdAtEpochMillis,
        deviceCount = deviceCount,
    )
