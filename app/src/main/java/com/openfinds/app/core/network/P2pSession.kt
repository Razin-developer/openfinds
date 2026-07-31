package com.openfinds.app.core.network

import com.openfinds.app.core.crypto.SessionCipher
import com.openfinds.app.core.network.json.OpenFindJson
import com.openfinds.app.core.network.protocol.Framing
import com.openfinds.app.core.network.protocol.P2pMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.Socket

/** An established, encrypted connection to a peer, identified by [peerIdentityPublicKeyB64]. */
class P2pSession(
    private val socket: Socket,
    private val cipher: SessionCipher,
    val peerDeviceId: String,
    val peerDeviceName: String,
    val peerIdentityPublicKeyB64: String,
) {
    val remoteHost: String get() = socket.inetAddress.hostAddress ?: ""
    val remotePort: Int get() = socket.port

    suspend fun send(message: P2pMessage) = withContext(Dispatchers.IO) {
        val plaintext = OpenFindJson.encodeToString(P2pMessage.serializer(), message).toByteArray(Charsets.UTF_8)
        val ciphertext = cipher.encrypt(plaintext)
        Framing.writeFrame(socket.getOutputStream(), ciphertext)
    }

    suspend fun receive(): P2pMessage = withContext(Dispatchers.IO) {
        val ciphertext = Framing.readFrame(socket.getInputStream())
        val plaintext = cipher.decrypt(ciphertext)
        OpenFindJson.decodeFromString(P2pMessage.serializer(), String(plaintext, Charsets.UTF_8))
    }

    fun close() {
        runCatching { socket.close() }
    }
}
