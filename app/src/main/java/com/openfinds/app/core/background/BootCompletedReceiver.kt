package com.openfinds.app.core.background

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.openfinds.app.core.data.datastore.UserPreferencesRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class BootCompletedReceiver : BroadcastReceiver() {

    @Inject lateinit var preferencesRepository: UserPreferencesRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pendingResult = goAsync()
        val enabled = runBlocking { preferencesRepository.preferences.first().backgroundMonitoringEnabled }
        if (enabled) {
            ContextCompat.startForegroundService(context, Intent(context, DeviceMonitorService::class.java))
        }
        pendingResult.finish()
    }
}
