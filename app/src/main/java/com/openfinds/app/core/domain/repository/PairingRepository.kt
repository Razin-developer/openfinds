package com.openfinds.app.core.domain.repository

import com.openfinds.app.core.crypto.PairingCrypto
import com.openfinds.app.core.data.local.TrustedDeviceDao
import com.openfinds.app.core.data.local.TrustedDeviceEntity
import com.openfinds.app.core.network.P2pConnectionManager
import com.openfinds.app.core.network.PairingOutcome
import com.openfinds.app.core.network.PairingRequest
import com.openfinds.app.core.network.protocol.PairingMode
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject
import javax.inject.Singleton

interface PairingRepository {
    val incomingPairingRequests: SharedFlow<PairingRequest>
    fun generatePin(): String
    fun beginPinPairing(pin: String)
    fun endPinPairing()
    suspend fun pairViaQr(host: String, port: Int, expectedIdentityPublicKeyB64: String): PairingOutcome
    suspend fun pairViaPin(host: String, port: Int, pin: String): PairingOutcome
    suspend fun acceptIncoming(request: PairingRequest)
    suspend fun rejectIncoming(request: PairingRequest)
}

@Singleton
class PairingRepositoryImpl @Inject constructor(
    private val connectionManager: P2pConnectionManager,
    private val trustedDeviceDao: TrustedDeviceDao,
) : PairingRepository {

    override val incomingPairingRequests: SharedFlow<PairingRequest> = connectionManager.pairingRequests

    override fun generatePin(): String = PairingCrypto.generatePin()

    override fun beginPinPairing(pin: String) {
        connectionManager.activeOutgoingPin = pin
    }

    override fun endPinPairing() {
        connectionManager.activeOutgoingPin = null
    }

    override suspend fun pairViaQr(host: String, port: Int, expectedIdentityPublicKeyB64: String): PairingOutcome =
        persistIfSuccessful(
            connectionManager.pairWith(
                host = host,
                port = port,
                mode = PairingMode.QR,
                pin = null,
                expectedPeerIdentityPublicKeyB64 = expectedIdentityPublicKeyB64,
            ),
        )

    override suspend fun pairViaPin(host: String, port: Int, pin: String): PairingOutcome =
        persistIfSuccessful(connectionManager.pairWith(host, port, PairingMode.PIN, pin = pin))

    override suspend fun acceptIncoming(request: PairingRequest) {
        request.respond(true)
        trustedDeviceDao.upsert(
            TrustedDeviceEntity(
                id = request.fromDeviceId,
                displayName = request.fromDeviceName,
                nickname = null,
                avatarColorArgb = avatarColorFor(request.fromDeviceId),
                publicKeyBase64 = request.fromIdentityPublicKeyB64,
                lastKnownHost = request.remoteHost,
                lastKnownPort = null,
                pairedAtEpochMillis = System.currentTimeMillis(),
                lastSeenEpochMillis = System.currentTimeMillis(),
            ),
        )
    }

    override suspend fun rejectIncoming(request: PairingRequest) {
        request.respond(false)
    }

    private suspend fun persistIfSuccessful(outcome: PairingOutcome): PairingOutcome {
        if (outcome is PairingOutcome.Success) {
            trustedDeviceDao.upsert(
                TrustedDeviceEntity(
                    id = outcome.peerDeviceId,
                    displayName = outcome.peerDeviceName,
                    nickname = null,
                    avatarColorArgb = avatarColorFor(outcome.peerDeviceId),
                    publicKeyBase64 = outcome.peerIdentityPublicKeyB64,
                    lastKnownHost = outcome.host,
                    lastKnownPort = outcome.port,
                    pairedAtEpochMillis = System.currentTimeMillis(),
                    lastSeenEpochMillis = System.currentTimeMillis(),
                ),
            )
        }
        return outcome
    }

    private fun avatarColorFor(deviceId: String): Int {
        val palette = intArrayOf(0xFF5B6CFF.toInt(), 0xFF2FA766.toInt(), 0xFFCC8A1E.toInt(), 0xFFE05252.toInt(), 0xFF8B94FF.toInt())
        return palette[(deviceId.hashCode() and Int.MAX_VALUE) % palette.size]
    }
}
