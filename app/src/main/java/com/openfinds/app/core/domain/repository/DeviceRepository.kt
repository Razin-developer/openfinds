package com.openfinds.app.core.domain.repository

import com.openfinds.app.core.data.local.TrustedDeviceDao
import com.openfinds.app.core.data.local.TrustedDeviceEntity
import com.openfinds.app.core.data.local.toDomain
import com.openfinds.app.core.domain.model.ConnectionState
import com.openfinds.app.core.domain.model.DeviceSnapshot
import com.openfinds.app.core.domain.model.TrustedDevice
import com.openfinds.app.core.network.DeviceStatusProvider
import com.openfinds.app.core.network.P2pConnectionManager
import com.openfinds.app.core.network.protocol.DeviceAction
import com.openfinds.app.core.network.protocol.P2pMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** A device considered "online" if it's been seen in the last 45 seconds (roughly 2 UDP beacon cycles). */
private const val ONLINE_WINDOW_MS = 45_000L

interface DeviceRepository {
    fun observeTrustedDevices(): Flow<List<TrustedDevice>>
    fun observeDevice(deviceId: String): Flow<TrustedDevice?>
    suspend fun renameDevice(deviceId: String, nickname: String?)
    suspend fun forgetDevice(deviceId: String)
    suspend fun requestStatus(device: TrustedDevice): Result<DeviceSnapshot>
    suspend fun sendFindAction(device: TrustedDevice, action: DeviceAction): Result<Unit>
    fun localSnapshot(): DeviceSnapshot
}

@Singleton
class DeviceRepositoryImpl @Inject constructor(
    private val dao: TrustedDeviceDao,
    private val connectionManager: P2pConnectionManager,
    private val statusProvider: DeviceStatusProvider,
) : DeviceRepository {

    override fun observeTrustedDevices(): Flow<List<TrustedDevice>> =
        dao.observeAll().map { entities -> entities.map { it.toDomainWithComputedState() } }

    override fun observeDevice(deviceId: String): Flow<TrustedDevice?> =
        dao.observeById(deviceId).map { it?.toDomainWithComputedState() }

    override suspend fun renameDevice(deviceId: String, nickname: String?) {
        dao.renameDevice(deviceId, nickname?.trim()?.takeIf { it.isNotEmpty() })
    }

    override suspend fun forgetDevice(deviceId: String) {
        dao.deleteById(deviceId)
    }

    override suspend fun requestStatus(device: TrustedDevice): Result<DeviceSnapshot> {
        val host = device.lastKnownHost ?: return Result.failure(IllegalStateException("Device has no known address"))
        val port = device.lastKnownPort ?: return Result.failure(IllegalStateException("Device has no known port"))
        return runCatching {
            val reply = connectionManager.sendCommand(host, port, device.publicKeyBase64, DeviceAction.STATUS_REQUEST)
            val status = reply as? P2pMessage.StatusResponse
                ?: throw IllegalStateException("Unexpected reply: $reply")
            dao.updateLastSeen(device.id, System.currentTimeMillis(), host, port)
            DeviceSnapshot(
                batteryPercent = status.batteryPercent,
                isCharging = status.isCharging,
                storageUsedBytes = status.storageUsedBytes,
                storageTotalBytes = status.storageTotalBytes,
                ramUsedBytes = status.ramUsedBytes,
                ramTotalBytes = status.ramTotalBytes,
                uptimeMillis = status.uptimeMillis,
                capturedAtEpochMillis = System.currentTimeMillis(),
            )
        }
    }

    override suspend fun sendFindAction(device: TrustedDevice, action: DeviceAction): Result<Unit> {
        val host = device.lastKnownHost ?: return Result.failure(IllegalStateException("Device has no known address"))
        val port = device.lastKnownPort ?: return Result.failure(IllegalStateException("Device has no known port"))
        return runCatching {
            connectionManager.sendCommand(host, port, device.publicKeyBase64, action)
            Unit
        }
    }

    override fun localSnapshot(): DeviceSnapshot = statusProvider.currentSnapshot()

    private fun TrustedDeviceEntity.toDomainWithComputedState(): TrustedDevice {
        val lastSeen = lastSeenEpochMillis
        val isOnline = lastSeen != null && (System.currentTimeMillis() - lastSeen) < ONLINE_WINDOW_MS
        return toDomain(isOnline = isOnline, isConnecting = false)
    }
}
