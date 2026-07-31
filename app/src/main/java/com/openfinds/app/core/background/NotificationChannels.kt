package com.openfinds.app.core.background

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.content.getSystemService

object NotificationChannels {
    const val MONITORING = "monitoring"
    const val PAIRING = "pairing"
    const val FIND_ALERTS = "find_alerts"
    const val DEVICE_STATUS = "device_status"

    fun ensureCreated(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService<NotificationManager>() ?: return

        manager.createNotificationChannel(
            NotificationChannel(MONITORING, "Background monitoring", NotificationManager.IMPORTANCE_MIN).apply {
                description = "Persistent status while OpenFind watches for your trusted devices"
                setShowBadge(false)
            },
        )
        manager.createNotificationChannel(
            NotificationChannel(PAIRING, "Pairing requests", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "A nearby device wants to pair with this phone"
            },
        )
        manager.createNotificationChannel(
            NotificationChannel(FIND_ALERTS, "Find device alerts", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "This phone is being located by a trusted device"
            },
        )
        manager.createNotificationChannel(
            NotificationChannel(DEVICE_STATUS, "Device connection changes", NotificationManager.IMPORTANCE_LOW).apply {
                description = "A trusted device came online, went offline, or its battery is low"
            },
        )
    }
}
