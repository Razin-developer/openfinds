package com.openfinds.app

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.openfinds.app.core.background.NotificationChannels
import com.openfinds.app.core.background.ReconnectWorker
import com.openfinds.app.core.data.datastore.UserPreferencesRepository
import com.openfinds.app.core.diagnostics.BufferingTimberTree
import com.openfinds.app.core.diagnostics.LogBuffer
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class OpenFindApplication : Application(), Configuration.Provider {
    @Inject lateinit var hiltWorkerFactory: HiltWorkerFactory

    @Inject lateinit var logBuffer: LogBuffer

    @Inject lateinit var preferencesRepository: UserPreferencesRepository

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(hiltWorkerFactory).build()

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        // Always kept, in both debug and release, so the on-device Diagnostics
        // screen and log export have real data without ever leaving the device.
        Timber.plant(BufferingTimberTree(logBuffer))

        applicationScope.launch {
            preferencesRepository.preferences.collect { prefs ->
                logBuffer.minPriority = if (prefs.verboseLoggingEnabled) Log.VERBOSE else Log.WARN
            }
        }

        NotificationChannels.ensureCreated(this)
        scheduleReconnectWork()
    }

    private fun scheduleReconnectWork() {
        val request = PeriodicWorkRequestBuilder<ReconnectWorker>(15, TimeUnit.MINUTES).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "reconnect-trusted-devices",
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }
}
