package com.openfinds.app.core.background

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

/** Below API 33, posting a notification never required a runtime permission. */
fun Context.hasNotificationPermission(): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

/**
 * Posts [notification] only if the user has actually granted notification permission.
 * Lint can't trace the [hasNotificationPermission] guard across the function boundary, hence the suppress.
 */
@SuppressLint("MissingPermission")
fun Context.notifySafely(
    id: Int,
    notification: Notification,
) {
    if (!hasNotificationPermission()) return
    NotificationManagerCompat.from(this).notify(id, notification)
}
