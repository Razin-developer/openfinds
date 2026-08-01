package com.openfinds.app.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [TrustedDeviceEntity::class, DeviceGroupEntity::class, DeviceHistoryEventEntity::class],
    version = 2,
    exportSchema = true,
)
abstract class OpenFindDatabase : RoomDatabase() {
    abstract fun trustedDeviceDao(): TrustedDeviceDao

    abstract fun deviceGroupDao(): DeviceGroupDao

    abstract fun deviceHistoryDao(): DeviceHistoryDao
}

/** v1 shipped only `trusted_devices`; v2 adds device groups, history events, and avatar/group columns. */
val MIGRATION_1_2 =
    object : androidx.room.migration.Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `device_groups` (
                    `id` TEXT NOT NULL,
                    `name` TEXT NOT NULL,
                    `colorArgb` INTEGER NOT NULL,
                    `createdAtEpochMillis` INTEGER NOT NULL,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent(),
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `device_history_events` (
                    `id` TEXT NOT NULL,
                    `deviceId` TEXT NOT NULL,
                    `deviceName` TEXT NOT NULL,
                    `eventType` TEXT NOT NULL,
                    `timestampEpochMillis` INTEGER NOT NULL,
                    `detail` TEXT,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent(),
            )
            db.execSQL("ALTER TABLE `trusted_devices` ADD COLUMN `avatarImageUri` TEXT")
            db.execSQL("ALTER TABLE `trusted_devices` ADD COLUMN `groupId` TEXT")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_trusted_devices_groupId` ON `trusted_devices` (`groupId`)")
        }
    }
