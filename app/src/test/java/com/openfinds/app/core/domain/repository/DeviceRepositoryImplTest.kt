package com.openfinds.app.core.domain.repository

import com.google.common.truth.Truth.assertThat
import com.openfinds.app.core.background.DeviceAlertsNotifier
import com.openfinds.app.core.data.local.TrustedDeviceDao
import com.openfinds.app.core.data.local.TrustedDeviceEntity
import com.openfinds.app.core.data.local.toDomain
import com.openfinds.app.core.domain.model.HistoryEventType
import com.openfinds.app.core.network.DeviceStatusProvider
import com.openfinds.app.core.network.P2pConnectionManager
import com.openfinds.app.core.network.protocol.DeviceAction
import com.openfinds.app.core.network.protocol.P2pMessage
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DeviceRepositoryImplTest {
    private val dao = mockk<TrustedDeviceDao>(relaxUnitFun = true)
    private val connectionManager = mockk<P2pConnectionManager>()
    private val statusProvider = mockk<DeviceStatusProvider>()
    private val historyRepository = mockk<DeviceHistoryRepository>(relaxUnitFun = true)
    private val alertsNotifier = mockk<DeviceAlertsNotifier>(relaxUnitFun = true)

    private val repository = DeviceRepositoryImpl(dao, connectionManager, statusProvider, historyRepository, alertsNotifier)

    private val entity =
        TrustedDeviceEntity(
            id = "device-1",
            displayName = "Pixel 8",
            nickname = null,
            avatarColorArgb = 0,
            publicKeyBase64 = "pubkey",
            lastKnownHost = "192.168.1.10",
            lastKnownPort = 47331,
            pairedAtEpochMillis = 0,
            lastSeenEpochMillis = null,
        )

    @Test
    fun `requestStatus on a previously-offline device records a CONNECTED event and alert`() =
        runTest {
            val device = entity.toDomain(isOnline = false, isConnecting = false)
            coEvery { connectionManager.sendCommand(any(), any(), any(), DeviceAction.STATUS_REQUEST) } returns
                P2pMessage.StatusResponse(
                    batteryPercent = 42,
                    isCharging = false,
                    storageUsedBytes = 1,
                    storageTotalBytes = 2,
                    ramUsedBytes = 1,
                    ramTotalBytes = 2,
                    uptimeMillis = 1000,
                )

            val result = repository.requestStatus(device)

            assertThat(result.isSuccess).isTrue()
            assertThat(result.getOrNull()?.batteryPercent).isEqualTo(42)
            coVerify { historyRepository.record(device.id, device.displayName, HistoryEventType.CONNECTED) }
            coVerify { alertsNotifier.notifyConnectionChange(device, isNowOnline = true) }
            coVerify { alertsNotifier.notifyLowBattery(device, 42) }
        }

    @Test
    fun `requestStatus without a known address fails without contacting the network`() =
        runTest {
            val device = entity.copy(lastKnownHost = null).toDomain(isOnline = false, isConnecting = false)

            val result = repository.requestStatus(device)

            assertThat(result.isFailure).isTrue()
        }

    @Test
    fun `forgetDevice deletes the device and records a FORGOTTEN event`() =
        runTest {
            coEvery { dao.getById("device-1") } returns entity

            repository.forgetDevice("device-1")

            coVerify { dao.deleteById("device-1") }
            coVerify { historyRepository.record("device-1", "Pixel 8", HistoryEventType.FORGOTTEN) }
        }

    @Test
    fun `sendFindAction records a FIND_TRIGGERED event on success`() =
        runTest {
            val device = entity.toDomain(isOnline = true, isConnecting = false)
            coEvery { connectionManager.sendCommand(any(), any(), any(), DeviceAction.RING) } returns P2pMessage.Ack(DeviceAction.RING)

            val result = repository.sendFindAction(device, DeviceAction.RING)

            assertThat(result.isSuccess).isTrue()
            coVerify { historyRepository.record(device.id, device.displayName, HistoryEventType.FIND_TRIGGERED, detail = "RING") }
        }
}
