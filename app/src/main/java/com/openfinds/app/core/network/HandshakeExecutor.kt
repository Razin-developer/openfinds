package com.openfinds.app.core.network

import com.openfinds.app.core.crypto.DeviceIdentity
import com.openfinds.app.core.crypto.PairingCrypto
import com.openfinds.app.core.crypto.SessionCipher
import com.openfinds.app.core.network.json.OpenFindJson
import com.openfinds.app.core.network.protocol.Framing
import com.openfinds.app.core.network.protocol.P2pMessage
import com.openfinds.app.core.network.protocol.PairingMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.Socket
import java.util.Base64
import javax.inject.Inject
import javax.inject.Singleton

class HandshakeException(message: String) : Exception(message)

/**
 * Runs the X25519 + HKDF handshake described in [PairingCrypto] over an
 * already-connected [Socket], producing an authenticated, encrypted
 * [P2pSession] on success.
 */
@Singleton
class HandshakeExecutor @Inject constructor() {

    suspend fun initiate(
        socket: Socket,
        myIdentity: DeviceIdentity,
        myDeviceName: String,
        mode: PairingMode,
        pin: String?,
        expectedPeerIdentityPublicKeyB64: String?,
    ): P2pSession = withContext(Dispatchers.IO) {
        val (ephemeralPrivate, ephemeralPublic) = PairingCrypto.generateEphemeralKeyPair()
        val output = socket.getOutputStream()
        val input = socket.getInputStream()

        writePlain(
            output,
            P2pMessage.HandshakeInit(
                fromDeviceId = myIdentity.deviceId,
                fromDeviceName = myDeviceName,
                ephemeralPublicKeyB64 = ephemeralPublic.toB64(),
                pairingMode = mode,
            ),
        )

        val response = readPlain(input) as? P2pMessage.HandshakeResponse
            ?: throw HandshakeException("Expected HandshakeResponse")

        if (expectedPeerIdentityPublicKeyB64 != null && response.identityPublicKeyB64 != expectedPeerIdentityPublicKeyB64) {
            throw HandshakeException("Peer identity key does not match trusted device")
        }

        val peerEphemeralPublic = response.ephemeralPublicKeyB64.fromB64()
        val sharedSecret = PairingCrypto.computeSharedSecret(ephemeralPrivate, peerEphemeralPublic)
        val transcript = ephemeralPublic + peerEphemeralPublic
        val sessionKey = PairingCrypto.deriveSessionKey(sharedSecret, transcript, pin)
        val cipher = SessionCipher(sessionKey)

        val confirmPlaintext = OpenFindJson.encodeToString(
            P2pMessage.serializer(),
            P2pMessage.HandshakeConfirm(myIdentity.publicKey.toB64()) as P2pMessage,
        ).toByteArray(Charsets.UTF_8)
        Framing.writeFrame(output, cipher.encrypt(confirmPlaintext))

        val ackCiphertext = Framing.readFrame(input)
        val ack = runCatching {
            OpenFindJson.decodeFromString(P2pMessage.serializer(), String(cipher.decrypt(ackCiphertext), Charsets.UTF_8))
        }.getOrNull()
        if (ack !is P2pMessage.Ack) throw HandshakeException("PIN or key mismatch — handshake not confirmed")

        P2pSession(socket, cipher, response.fromDeviceId, response.fromDeviceName, response.identityPublicKeyB64)
    }

    suspend fun respond(
        socket: Socket,
        myIdentity: DeviceIdentity,
        myDeviceName: String,
        pinForMode: (PairingMode) -> String?,
    ): P2pSession = withContext(Dispatchers.IO) {
        val output = socket.getOutputStream()
        val input = socket.getInputStream()

        val init = readPlain(input) as? P2pMessage.HandshakeInit
            ?: throw HandshakeException("Expected HandshakeInit")

        val (ephemeralPrivate, ephemeralPublic) = PairingCrypto.generateEphemeralKeyPair()
        writePlain(
            output,
            P2pMessage.HandshakeResponse(
                fromDeviceId = myIdentity.deviceId,
                fromDeviceName = myDeviceName,
                ephemeralPublicKeyB64 = ephemeralPublic.toB64(),
                identityPublicKeyB64 = myIdentity.publicKey.toB64(),
            ),
        )

        val peerEphemeralPublic = init.ephemeralPublicKeyB64.fromB64()
        val sharedSecret = PairingCrypto.computeSharedSecret(ephemeralPrivate, peerEphemeralPublic)
        val transcript = peerEphemeralPublic + ephemeralPublic
        val pin = pinForMode(init.pairingMode)
        val sessionKey = PairingCrypto.deriveSessionKey(sharedSecret, transcript, pin)
        val cipher = SessionCipher(sessionKey)

        val confirmCiphertext = Framing.readFrame(input)
        val confirm = runCatching {
            OpenFindJson.decodeFromString(P2pMessage.serializer(), String(cipher.decrypt(confirmCiphertext), Charsets.UTF_8))
        }.getOrNull() as? P2pMessage.HandshakeConfirm ?: run {
            runCatching { socket.close() }
            throw HandshakeException("PIN or key mismatch — handshake not confirmed")
        }

        val ackPlaintext = OpenFindJson.encodeToString(P2pMessage.serializer(), P2pMessage.Ack() as P2pMessage)
            .toByteArray(Charsets.UTF_8)
        Framing.writeFrame(output, cipher.encrypt(ackPlaintext))

        P2pSession(socket, cipher, init.fromDeviceId, init.fromDeviceName, confirm.identityPublicKeyB64)
    }

    private fun writePlain(output: java.io.OutputStream, message: P2pMessage) {
        val bytes = OpenFindJson.encodeToString(P2pMessage.serializer(), message).toByteArray(Charsets.UTF_8)
        Framing.writeFrame(output, bytes)
    }

    private fun readPlain(input: java.io.InputStream): P2pMessage {
        val bytes = Framing.readFrame(input)
        return OpenFindJson.decodeFromString(P2pMessage.serializer(), String(bytes, Charsets.UTF_8))
    }

    private fun ByteArray.toB64(): String = Base64.getEncoder().encodeToString(this)
    private fun String.fromB64(): ByteArray = Base64.getDecoder().decode(this)
}
