package com.openfinds.app.core.background

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.openfinds.app.core.data.local.TrustedDeviceDao
import com.openfinds.app.core.data.local.toDomain
import com.openfinds.app.core.domain.repository.DeviceRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first

/**
 * Periodically probes every trusted device that hasn't been seen recently so
 * "last seen" / online state stays accurate even if a device's UDP beacon
 * was missed (e.g. this phone was asleep with Doze restricting background
 * network access until this job's maintenance window).
 */
@HiltWorker
class ReconnectWorker
    @AssistedInject
    constructor(
        @Assisted context: Context,
        @Assisted params: WorkerParameters,
        private val trustedDeviceDao: TrustedDeviceDao,
        private val deviceRepository: DeviceRepository,
    ) : CoroutineWorker(context, params) {
        override suspend fun doWork(): Result =
            coroutineScope {
                val entities = trustedDeviceDao.observeAll().first()
                val reachableDevices = entities.filter { it.lastKnownHost != null && it.lastKnownPort != null }

                reachableDevices.map { entity ->
                    async {
                        runCatching {
                            deviceRepository.requestStatus(entity.toDomain(isOnline = false, isConnecting = true))
                        }
                    }
                }.awaitAll()

                Result.success()
            }
    }
