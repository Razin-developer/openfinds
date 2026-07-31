package com.openfinds.app.core.background

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint

/**
 * Wi-Fi/network changes (join a new network, toggle Wi-Fi) invalidate NSD
 * advertising and any cached peer addresses, so we simply restart the
 * monitor service — it re-advertises and lets discovery repopulate.
 */
@AndroidEntryPoint
class NetworkChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != "android.net.conn.CONNECTIVITY_CHANGE") return
        ContextCompat.startForegroundService(context, Intent(context, DeviceMonitorService::class.java))
    }
}
