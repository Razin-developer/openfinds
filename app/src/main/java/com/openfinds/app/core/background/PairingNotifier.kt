package com.openfinds.app.core.background

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.openfinds.app.MainActivity
import com.openfinds.app.R
import com.openfinds.app.core.network.PairingRequest

/** Posts (and lets the user act on) the "someone wants to pair" notification. */
object PairingNotifier {

    fun notifyIncomingRequest(context: Context, request: PairingRequest) {
        val contentIntent = PendingIntent.getActivity(
            context,
            request.fromDeviceId.hashCode(),
            Intent(context, MainActivity::class.java).apply {
                action = ACTION_OPEN_PAIRING
                putExtra(EXTRA_DEVICE_NAME, request.fromDeviceName)
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val notification = NotificationCompat.Builder(context, NotificationChannels.PAIRING)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.notification_pairing_title))
            .setContentText(context.getString(R.string.notification_pairing_text, request.fromDeviceName))
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        NotificationManagerCompat.from(context).notify(request.fromDeviceId.hashCode(), notification)
    }

    const val ACTION_OPEN_PAIRING = "com.openfinds.app.action.OPEN_PAIRING"
    const val EXTRA_DEVICE_NAME = "extra_device_name"
}
