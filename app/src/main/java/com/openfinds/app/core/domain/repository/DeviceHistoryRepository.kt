package com.openfinds.app.core.domain.repository

import com.openfinds.app.core.data.local.DeviceHistoryDao
import com.openfinds.app.core.data.local.DeviceHistoryEventEntity
import com.openfinds.app.core.data.local.toDomain
import com.openfinds.app.core.domain.model.HistoryEvent
import com.openfinds.app.core.domain.model.HistoryEventType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

interface DeviceHistoryRepository {
    fun observeRecent(): Flow<List<HistoryEvent>>

    fun observeForDevice(deviceId: String): Flow<List<HistoryEvent>>

    suspend fun record(
        deviceId: String,
        deviceName: String,
        type: HistoryEventType,
        detail: String? = null,
    )

    suspend fun clearAll()
}

@Singleton
class DeviceHistoryRepositoryImpl
    @Inject
    constructor(
        private val dao: DeviceHistoryDao,
    ) : DeviceHistoryRepository {
        override fun observeRecent(): Flow<List<HistoryEvent>> = dao.observeRecent().map { list -> list.map { it.toDomain() } }

        override fun observeForDevice(deviceId: String): Flow<List<HistoryEvent>> =
            dao.observeForDevice(deviceId).map { list ->
                list.map {
                    it.toDomain()
                }
            }

        override suspend fun record(
            deviceId: String,
            deviceName: String,
            type: HistoryEventType,
            detail: String?,
        ) {
            dao.insert(
                DeviceHistoryEventEntity(
                    id = UUID.randomUUID().toString(),
                    deviceId = deviceId,
                    deviceName = deviceName,
                    eventType = type.name,
                    timestampEpochMillis = System.currentTimeMillis(),
                    detail = detail,
                ),
            )
        }

        override suspend fun clearAll() {
            dao.clearAll()
        }
    }
