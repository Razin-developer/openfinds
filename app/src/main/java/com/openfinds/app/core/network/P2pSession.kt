package com.openfinds.app.core.network

import com.openfinds.app.core.crypto.SessionCipher
import com.openfinds.app.core.network.json.OpenFindJson
import com.openfinds.app.core.network.protocol.Framing
import com.openfinds.app.core.network.protocol.P2pMessage
import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.toJavaAddress
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.ByteWriteChannel

/** An established, encrypted connection to a peer, identified by [peerIdentityPublicKeyB64]. */
class P2pSession(
    private val socket: Socket,
    private val input: ByteReadChannel,
    private val output: ByteWriteChannel,
    private val cipher: SessionCipher,
    val peerDeviceId: String,
    val peerDeviceName: String,
    val peerIdentityPublicKeyB64: String,
) {
    private val remoteJavaAddress: java.net.InetSocketAddress?
        get() = (socket.remoteAddress as? io.ktor.network.sockets.InetSocketAddress)?.toJavaAddress() as? java.net.InetSocketAddress

    val remoteHost: String
        get() = remoteJavaAddress?.address?.hostAddress ?: ""
    val remotePort: Int
        get() = remoteJavaAddress?.port ?: 0

    suspend fun send(message: P2pMessage) {
        val plaintext = OpenFindJson.encodeToString(P2pMessage.serializer(), message).toByteArray(Charsets.UTF_8)
        val ciphertext = cipher.encrypt(plaintext)
        Framing.writeFrame(output, ciphertext)
    }

    suspend fun receive(): P2pMessage {
        val ciphertext = Framing.readFrame(input)
        val plaintext = cipher.decrypt(ciphertext)
        return OpenFindJson.decodeFromString(P2pMessage.serializer(), String(plaintext, Charsets.UTF_8))
    }

    fun close() {
        runCatching { socket.close() }
    }
}
