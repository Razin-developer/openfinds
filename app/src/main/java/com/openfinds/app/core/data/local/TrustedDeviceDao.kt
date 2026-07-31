package com.openfinds.app.core.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TrustedDeviceDao {

    @Query("SELECT * FROM trusted_devices ORDER BY displayName COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<TrustedDeviceEntity>>

    @Query("SELECT * FROM trusted_devices WHERE id = :id")
    suspend fun getById(id: String): TrustedDeviceEntity?

    @Query("SELECT * FROM trusted_devices WHERE id = :id")
    fun observeById(id: String): Flow<TrustedDeviceEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: TrustedDeviceEntity)

    @Update
    suspend fun update(entity: TrustedDeviceEntity)

    @Query("UPDATE trusted_devices SET lastSeenEpochMillis = :timestamp, lastKnownHost = :host, lastKnownPort = :port WHERE id = :id")
    suspend fun updateLastSeen(id: String, timestamp: Long, host: String, port: Int)

    @Query("UPDATE trusted_devices SET nickname = :nickname WHERE id = :id")
    suspend fun renameDevice(id: String, nickname: String?)

    @Delete
    suspend fun delete(entity: TrustedDeviceEntity)

    @Query("DELETE FROM trusted_devices WHERE id = :id")
    suspend fun deleteById(id: String)
}
