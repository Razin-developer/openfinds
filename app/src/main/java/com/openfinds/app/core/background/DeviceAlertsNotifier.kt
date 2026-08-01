package com.openfinds.app.core.background

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.openfinds.app.MainActivity
import com.openfinds.app.R
import com.openfinds.app.core.domain.model.TrustedDevice
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Posts low-battery and connection-change notifications for trusted devices, grouped under one summary. */
@Singleton
class DeviceAlertsNotifier
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private val notifiedLowBatteryFor = mutableSetOf<String>()

        fun notifyLowBattery(
            device: TrustedDevice,
            batteryPercent: Int,
        ) {
            if (batteryPercent > LOW_BATTERY_THRESHOLD) {
                notifiedLowBatteryFor.remove(device.id)
                return
            }
            if (!notifiedLowBatteryFor.add(device.id)) return // already notified since it dropped below the threshold

            post(
                id = ("battery-" + device.id).hashCode(),
                title = context.getString(R.string.notification_low_battery_title, device.name),
                text = context.getString(R.string.notification_low_battery_text, batteryPercent),
            )
        }

        fun notifyBeingFound(fromDeviceName: String) {
            val contentIntent =
                PendingIntent.getActivity(
                    context,
                    FIND_ALERT_NOTIFICATION_ID,
                    Intent(context, MainActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                )
            val notification =
                NotificationCompat.Builder(context, NotificationChannels.FIND_ALERTS)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(context.getString(R.string.notification_find_alert_title))
                    .setContentText(context.getString(R.string.notification_find_alert_text, fromDeviceName))
                    .setContentIntent(contentIntent)
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .build()
            context.notifySafely(FIND_ALERT_NOTIFICATION_ID, notification)
        }

        fun notifyConnectionChange(
            device: TrustedDevice,
            isNowOnline: Boolean,
        ) {
            post(
                id = ("connection-" + device.id).hashCode(),
                title = device.name,
                text =
                    if (isNowOnline) {
                        context.getString(R.string.notification_device_online)
                    } else {
                        context.getString(R.string.notification_device_offline)
                    },
            )
        }

        private fun post(
            id: Int,
            title: String,
            text: String,
        ) {
            val contentIntent =
                PendingIntent.getActivity(
                    context,
                    id,
                    Intent(context, MainActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                )
            val notification =
                NotificationCompat.Builder(context, NotificationChannels.DEVICE_STATUS)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setContentIntent(contentIntent)
                    .setAutoCancel(true)
                    .setGroup(DEVICE_STATUS_GROUP_KEY)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .build()

            val summary =
                NotificationCompat.Builder(context, NotificationChannels.DEVICE_STATUS)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(context.getString(R.string.notification_device_status_summary_title))
                    .setStyle(NotificationCompat.InboxStyle())
                    .setGroup(DEVICE_STATUS_GROUP_KEY)
                    .setGroupSummary(true)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .build()

            context.notifySafely(id, notification)
            context.notifySafely(DEVICE_STATUS_GROUP_KEY.hashCode(), summary)
        }

        private companion object {
            const val LOW_BATTERY_THRESHOLD = 15
            const val DEVICE_STATUS_GROUP_KEY = "com.openfinds.app.DEVICE_STATUS_GROUP"
            const val FIND_ALERT_NOTIFICATION_ID = 2001
        }
    }
