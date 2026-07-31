package com.openfinds.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.openfinds.app.core.background.NotificationChannels
import com.openfinds.app.core.background.ReconnectWorker
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class OpenFindApplication : Application(), Configuration.Provider {

    @Inject lateinit var hiltWorkerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(hiltWorkerFactory).build()

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(ReleaseCrashReportingTree())
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

/**
 * Release logging never leaves the device: OpenFind has no analytics or crash
 * backend, so only warnings and errors are kept for the on-device
 * diagnostics/log export screen.
 */
private class ReleaseCrashReportingTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority < android.util.Log.WARN) return
        android.util.Log.println(priority, tag ?: "OpenFind", message)
    }
}
