package com.openfinds.app.core.background

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.openfinds.app.MainActivity
import com.openfinds.app.R
import com.openfinds.app.core.crypto.DeviceIdentityStore
import com.openfinds.app.core.data.datastore.UserPreferencesRepository
import com.openfinds.app.core.network.NsdAdvertiser
import com.openfinds.app.core.network.NetworkConstants
import com.openfinds.app.core.network.P2pConnectionManager
import com.openfinds.app.core.network.UdpPresenceBeacon
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Foreground service that keeps this device discoverable and reachable by
 * its trusted peers while the app is backgrounded: NSD advertising, the UDP
 * beacon, and the P2P TCP listener all run for as long as this service is
 * alive, and it also carries the pairing-request notification pipeline.
 */
@AndroidEntryPoint
class DeviceMonitorService : Service() {

    @Inject lateinit var connectionManager: P2pConnectionManager
    @Inject lateinit var nsdAdvertiser: NsdAdvertiser
    @Inject lateinit var udpPresenceBeacon: UdpPresenceBeacon
    @Inject lateinit var identityStore: DeviceIdentityStore
    @Inject lateinit var preferencesRepository: UserPreferencesRepository

    private var serviceJob: Job = SupervisorJob()
    private lateinit var serviceScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()
        serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
        NotificationChannels.ensureCreated(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            buildNotification(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC,
        )
        startMonitoring()
        return START_STICKY
    }

    private fun startMonitoring() {
        serviceScope.launch {
            val identity = identityStore.getOrCreate()
            val prefs = preferencesRepository.preferences.first()
            val deviceName = prefs.deviceDisplayName.ifBlank { android.os.Build.MODEL ?: "Android device" }

            connectionManager.deviceDisplayNameProvider = { deviceName }
            connectionManager.start()
            runCatching { nsdAdvertiser.start(identity.deviceId, deviceName, NetworkConstants.TCP_PORT) }
                .onFailure { Timber.w(it, "NSD advertise failed, relying on UDP beacon only") }
            launch { udpPresenceBeacon.broadcastLoop(identity.deviceId, deviceName, NetworkConstants.TCP_PORT) }
            launch {
                connectionManager.pairingRequests.collect { request ->
                    PairingNotifier.notifyIncomingRequest(this@DeviceMonitorService, request)
                }
            }
        }
    }

    private fun buildNotification(): Notification {
        val openAppIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        return NotificationCompat.Builder(this, NotificationChannels.MONITORING)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(getString(R.string.notification_monitoring_title))
            .setContentText(getString(R.string.notification_monitoring_text))
            .setContentIntent(openAppIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()
    }

    override fun onDestroy() {
        nsdAdvertiser.stop()
        connectionManager.stop()
        serviceJob.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?) = null

    companion object {
        const val NOTIFICATION_ID = 1001
    }
}
