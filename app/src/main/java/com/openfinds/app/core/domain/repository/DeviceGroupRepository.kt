package com.openfinds.app.core.domain.repository

import com.openfinds.app.core.data.local.DeviceGroupDao
import com.openfinds.app.core.data.local.DeviceGroupEntity
import com.openfinds.app.core.data.local.toDomain
import com.openfinds.app.core.domain.model.DeviceGroup
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

interface DeviceGroupRepository {
    fun observeGroups(): Flow<List<DeviceGroup>>

    suspend fun createGroup(
        name: String,
        colorArgb: Int,
    ): String

    suspend fun renameGroup(
        groupId: String,
        name: String,
    )

    suspend fun deleteGroup(groupId: String)

    suspend fun assignDevice(
        deviceId: String,
        groupId: String?,
    )
}

@Singleton
class DeviceGroupRepositoryImpl
    @Inject
    constructor(
        private val dao: DeviceGroupDao,
    ) : DeviceGroupRepository {
        override fun observeGroups(): Flow<List<DeviceGroup>> =
            combine(dao.observeAll(), dao.observeDeviceCounts()) { groups, counts ->
                val countByGroup = counts.associate { it.groupId to it.count }
                groups.map { it.toDomain(deviceCount = countByGroup[it.id] ?: 0) }
            }

        override suspend fun createGroup(
            name: String,
            colorArgb: Int,
        ): String {
            val id = UUID.randomUUID().toString()
            dao.upsert(
                DeviceGroupEntity(
                    id = id,
                    name = name.trim(),
                    colorArgb = colorArgb,
                    createdAtEpochMillis = System.currentTimeMillis(),
                ),
            )
            return id
        }

        override suspend fun renameGroup(
            groupId: String,
            name: String,
        ) {
            val trimmed = name.trim()
            if (trimmed.isEmpty()) return
            dao.rename(groupId, trimmed)
        }

        override suspend fun deleteGroup(groupId: String) {
            dao.deleteById(groupId)
        }

        override suspend fun assignDevice(
            deviceId: String,
            groupId: String?,
        ) {
            dao.assignDeviceToGroup(deviceId, groupId)
        }
    }
