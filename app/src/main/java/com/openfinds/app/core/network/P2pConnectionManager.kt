package com.openfinds.app.core.network

import com.openfinds.app.core.crypto.DeviceIdentityStore
import com.openfinds.app.core.data.local.TrustedDeviceDao
import com.openfinds.app.core.network.protocol.DeviceAction
import com.openfinds.app.core.network.protocol.P2pMessage
import com.openfinds.app.core.network.protocol.PairingMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import timber.log.Timber
import java.net.ServerSocket
import java.net.Socket
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Owns the single TCP listen socket for OpenFind's P2P protocol and mediates
 * both directions of traffic: accepting inbound pairing/reconnect attempts,
 * and opening outbound connections to send commands to trusted devices.
 */
@Singleton
class P2pConnectionManager @Inject constructor(
    private val identityStore: DeviceIdentityStore,
    private val handshakeExecutor: HandshakeExecutor,
    private val trustedDeviceDao: TrustedDeviceDao,
    private val statusProvider: DeviceStatusProvider,
    private val findActionExecutor: FindActionExecutor,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var serverSocket: ServerSocket? = null

    private val _pairingRequests = MutableSharedFlow<PairingRequest>(extraBufferCapacity = 4)
    val pairingRequests: SharedFlow<PairingRequest> = _pairingRequests

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening

    /** The PIN this device most recently generated for an in-progress PIN pairing, if any. */
    @Volatile var activeOutgoingPin: String? = null

    var deviceDisplayNameProvider: () -> String = { android.os.Build.MODEL ?: "Android device" }

    fun start() {
        if (serverSocket != null) return
        scope.launch {
            runCatching {
                ServerSocket(NetworkConstants.TCP_PORT).also { serverSocket = it; _isListening.value = true }
            }.onSuccess { server ->
                Timber.i("OpenFind P2P listening on port ${server.localPort}")
                acceptLoop(server)
            }.onFailure {
                Timber.e(it, "Failed to start P2P server socket")
                _isListening.value = false
            }
        }
    }

    fun stop() {
        runCatching { serverSocket?.close() }
        serverSocket = null
        _isListening.value = false
    }

    private suspend fun acceptLoop(server: ServerSocket) {
        while (!server.isClosed) {
            val socket = runCatching { server.accept() }.getOrNull() ?: continue
            scope.launch { handleIncoming(socket) }
        }
    }

    private suspend fun handleIncoming(socket: Socket) {
        val identity = identityStore.getOrCreate()
        val myName = deviceDisplayNameProvider()

        val session = runCatching {
            withTimeout(NetworkConstants.HANDSHAKE_TIMEOUT_MS) {
                handshakeExecutor.respond(socket, identity, myName) { mode ->
                    if (mode == PairingMode.PIN) activeOutgoingPin else null
                }
            }
        }.getOrElse {
            Timber.w(it, "Inbound handshake failed")
            runCatching { socket.close() }
            return
        }

        val trusted = trustedDeviceDao.getById(session.peerDeviceId)
        if (trusted != null && trusted.publicKeyBase64 == session.peerIdentityPublicKeyB64) {
            trustedDeviceDao.updateLastSeen(trusted.id, System.currentTimeMillis(), session.remoteHost, session.remotePort)
            serveCommands(session)
            return
        }

        // Unknown peer: surface as a pairing request for the UI to approve.
        var resolved = false
        _pairingRequests.emit(
            PairingRequest(
                fromDeviceId = session.peerDeviceId,
                fromDeviceName = session.peerDeviceName,
                fromIdentityPublicKeyB64 = session.peerIdentityPublicKeyB64,
                mode = PairingMode.PIN,
                remoteHost = session.remoteHost,
                respond = { accepted ->
                    resolved = true
                    if (accepted) serveCommands(session) else session.close()
                },
            ),
        )
        // If nothing observes pairingRequests (e.g. app in background with no active pairing UI),
        // don't leak the socket — close it after the handshake timeout window.
        scope.launch {
            kotlinx.coroutines.delay(NetworkConstants.HANDSHAKE_TIMEOUT_MS)
            if (!resolved) session.close()
        }
    }

    private suspend fun serveCommands(session: P2pSession) {
        runCatching {
            while (true) {
                when (val message = session.receive()) {
                    is P2pMessage.Command -> handleCommand(session, message.action)
                    else -> Unit
                }
            }
        }.onFailure {
            Timber.d("P2P session with ${session.peerDeviceName} ended: ${it.message}")
        }
        session.close()
    }

    private suspend fun handleCommand(session: P2pSession, action: DeviceAction) {
        when (action) {
            DeviceAction.RING -> { findActionExecutor.startRing(); session.send(P2pMessage.Ack(action)) }
            DeviceAction.VIBRATE -> { findActionExecutor.startVibrate(); session.send(P2pMessage.Ack(action)) }
            DeviceAction.FLASH -> { findActionExecutor.startFlash(); session.send(P2pMessage.Ack(action)) }
            DeviceAction.STOP_FIND -> { findActionExecutor.stopAll(); session.send(P2pMessage.Ack(action)) }
            DeviceAction.STATUS_REQUEST -> {
                val snapshot = statusProvider.currentSnapshot()
                session.send(
                    P2pMessage.StatusResponse(
                        batteryPercent = snapshot.batteryPercent,
                        isCharging = snapshot.isCharging,
                        storageUsedBytes = snapshot.storageUsedBytes,
                        storageTotalBytes = snapshot.storageTotalBytes,
                        ramUsedBytes = snapshot.ramUsedBytes,
                        ramTotalBytes = snapshot.ramTotalBytes,
                        uptimeMillis = snapshot.uptimeMillis,
                    ),
                )
            }
        }
    }

    /**
     * Outbound pairing: connect to a discovered device and run the handshake as initiator.
     * [expectedPeerIdentityPublicKeyB64] should be set for QR pairing (the QR payload carries
     * the shown device's real public key, so we can refuse to pair with an impostor answering
     * at that address); it's left null for PIN pairing, where the PIN itself is the proof.
     */
    suspend fun pairWith(
        host: String,
        port: Int,
        mode: PairingMode,
        pin: String?,
        expectedPeerIdentityPublicKeyB64: String? = null,
    ): PairingOutcome {
        val identity = identityStore.getOrCreate()
        return runCatching {
            withTimeout(NetworkConstants.HANDSHAKE_TIMEOUT_MS) {
                Socket(host, port).use { socket ->
                    val session = handshakeExecutor.initiate(
                        socket = socket,
                        myIdentity = identity,
                        myDeviceName = deviceDisplayNameProvider(),
                        mode = mode,
                        pin = pin,
                        expectedPeerIdentityPublicKeyB64 = expectedPeerIdentityPublicKeyB64,
                    )
                    val outcome = PairingOutcome.Success(
                        peerDeviceId = session.peerDeviceId,
                        peerDeviceName = session.peerDeviceName,
                        peerIdentityPublicKeyB64 = session.peerIdentityPublicKeyB64,
                        host = host,
                        port = port,
                    )
                    session.close()
                    outcome
                }
            }
        }.getOrElse { PairingOutcome.Failure(it.message ?: "Pairing failed") }
    }

    /** Sends a single command to an already-trusted device, verifying its identity key before trusting the reply. */
    suspend fun sendCommand(
        host: String,
        port: Int,
        expectedIdentityPublicKeyB64: String,
        action: DeviceAction,
    ): P2pMessage {
        val identity = identityStore.getOrCreate()
        return withTimeout(NetworkConstants.COMMAND_TIMEOUT_MS) {
            Socket(host, port).use { socket ->
                val session = handshakeExecutor.initiate(
                    socket = socket,
                    myIdentity = identity,
                    myDeviceName = deviceDisplayNameProvider(),
                    mode = PairingMode.RECONNECT,
                    pin = null,
                    expectedPeerIdentityPublicKeyB64 = expectedIdentityPublicKeyB64,
                )
                session.send(P2pMessage.Command(action))
                val reply = session.receive()
                session.close()
                reply
            }
        }
    }
}
