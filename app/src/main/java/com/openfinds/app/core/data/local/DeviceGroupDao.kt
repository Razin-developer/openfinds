package com.openfinds.app.core.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceGroupDao {
    @Query("SELECT * FROM device_groups ORDER BY name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<DeviceGroupEntity>>

    @Query("SELECT groupId, COUNT(*) as count FROM trusted_devices WHERE groupId IS NOT NULL GROUP BY groupId")
    fun observeDeviceCounts(): Flow<List<GroupDeviceCount>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: DeviceGroupEntity)

    @Update
    suspend fun update(entity: DeviceGroupEntity)

    @Query("UPDATE device_groups SET name = :name WHERE id = :id")
    suspend fun rename(
        id: String,
        name: String,
    )

    @Query("DELETE FROM device_groups WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM device_groups")
    suspend fun deleteAll()

    @Query("UPDATE trusted_devices SET groupId = :groupId WHERE id = :deviceId")
    suspend fun assignDeviceToGroup(
        deviceId: String,
        groupId: String?,
    )
}

data class GroupDeviceCount(val groupId: String, val count: Int)
