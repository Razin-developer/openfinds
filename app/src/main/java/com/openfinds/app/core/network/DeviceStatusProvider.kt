package com.openfinds.app.core.network

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Environment
import android.os.StatFs
import android.os.SystemClock
import com.openfinds.app.core.domain.model.DeviceSnapshot
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Reads this device's real, current hardware status for the details dashboard and status replies. */
@Singleton
class DeviceStatusProvider
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        fun currentSnapshot(): DeviceSnapshot {
            val batteryStatus = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val batteryPercent = if (level >= 0 && scale > 0) (level * 100) / scale else 0
            val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

            val statFs = StatFs(Environment.getDataDirectory().path)
            val storageTotal = statFs.blockCountLong * statFs.blockSizeLong
            val storageFree = statFs.availableBlocksLong * statFs.blockSizeLong
            val storageUsed = storageTotal - storageFree

            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memoryInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memoryInfo)
            val ramTotal = memoryInfo.totalMem
            val ramUsed = ramTotal - memoryInfo.availMem

            return DeviceSnapshot(
                batteryPercent = batteryPercent,
                isCharging = isCharging,
                storageUsedBytes = storageUsed,
                storageTotalBytes = storageTotal,
                ramUsedBytes = ramUsed,
                ramTotalBytes = ramTotal,
                uptimeMillis = SystemClock.elapsedRealtime(),
                capturedAtEpochMillis = System.currentTimeMillis(),
            )
        }
    }
