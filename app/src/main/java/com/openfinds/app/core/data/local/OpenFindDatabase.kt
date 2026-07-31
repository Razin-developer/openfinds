package com.openfinds.app.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [TrustedDeviceEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class OpenFindDatabase : RoomDatabase() {
    abstract fun trustedDeviceDao(): TrustedDeviceDao
}
