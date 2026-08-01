package com.openfinds.app.core.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceHistoryDao {
    @Query("SELECT * FROM device_history_events ORDER BY timestampEpochMillis DESC LIMIT 500")
    fun observeRecent(): Flow<List<DeviceHistoryEventEntity>>

    @Query("SELECT * FROM device_history_events WHERE deviceId = :deviceId ORDER BY timestampEpochMillis DESC")
    fun observeForDevice(deviceId: String): Flow<List<DeviceHistoryEventEntity>>

    @Insert
    suspend fun insert(event: DeviceHistoryEventEntity)

    @Query("DELETE FROM device_history_events WHERE deviceId = :deviceId")
    suspend fun deleteForDevice(deviceId: String)

    @Query("DELETE FROM device_history_events")
    suspend fun clearAll()
}
