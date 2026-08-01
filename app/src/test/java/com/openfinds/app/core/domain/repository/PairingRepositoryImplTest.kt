package com.openfinds.app.core.domain.repository

import com.google.common.truth.Truth.assertThat
import com.openfinds.app.core.data.local.TrustedDeviceDao
import com.openfinds.app.core.domain.model.HistoryEventType
import com.openfinds.app.core.network.P2pConnectionManager
import com.openfinds.app.core.network.PairingOutcome
import com.openfinds.app.core.network.PairingRequest
import com.openfinds.app.core.network.protocol.PairingMode
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test

class PairingRepositoryImplTest {
    private val connectionManager = mockk<P2pConnectionManager>(relaxUnitFun = true)
    private val trustedDeviceDao = mockk<TrustedDeviceDao>(relaxUnitFun = true)
    private val historyRepository = mockk<DeviceHistoryRepository>(relaxUnitFun = true)

    init {
        every { connectionManager.pairingRequests } returns MutableSharedFlow()
    }

    private val repository = PairingRepositoryImpl(connectionManager, trustedDeviceDao, historyRepository)

    @Test
    fun `successful QR pairing persists the device and records a PAIRED event`() =
        runTest {
            val outcome =
                PairingOutcome.Success(
                    peerDeviceId = "peer-1",
                    peerDeviceName = "Peer Phone",
                    peerIdentityPublicKeyB64 = "pubkey",
                    host = "192.168.1.5",
                    port = 47331,
                )
            coEvery {
                connectionManager.pairWith(
                    host = "192.168.1.5",
                    port = 47331,
                    mode = PairingMode.QR,
                    pin = null,
                    expectedPeerIdentityPublicKeyB64 = "pubkey",
                )
            } returns outcome

            val result = repository.pairViaQr("192.168.1.5", 47331, "pubkey")

            assertThat(result).isEqualTo(outcome)
            coVerify { trustedDeviceDao.upsert(match { it.id == "peer-1" }) }
            coVerify { historyRepository.record("peer-1", "Peer Phone", HistoryEventType.PAIRED) }
        }

    @Test
    fun `failed pairing does not touch the database or history`() =
        runTest {
            val failure = PairingOutcome.Failure("timed out")
            coEvery {
                connectionManager.pairWith(host = any(), port = any(), mode = PairingMode.PIN, pin = "123456")
            } returns failure

            val result = repository.pairViaPin("192.168.1.5", 47331, "123456")

            assertThat(result).isEqualTo(failure)
            coVerify(exactly = 0) { trustedDeviceDao.upsert(any()) }
            coVerify(exactly = 0) { historyRepository.record(any(), any(), any(), any()) }
        }

    @Test
    fun `accepting an incoming request trusts the device and records history`() =
        runTest {
            var accepted: Boolean? = null
            val request =
                PairingRequest(
                    fromDeviceId = "peer-2",
                    fromDeviceName = "Nearby Tablet",
                    fromIdentityPublicKeyB64 = "pubkey-2",
                    mode = PairingMode.PIN,
                    remoteHost = "192.168.1.6",
                    respond = { accepted = it },
                )

            repository.acceptIncoming(request)

            assertThat(accepted).isTrue()
            coVerify { trustedDeviceDao.upsert(match { it.id == "peer-2" }) }
            coVerify { historyRepository.record("peer-2", "Nearby Tablet", HistoryEventType.PAIRED) }
        }
}
