package com.openfinds.app.core.network

import com.openfinds.app.core.domain.model.DiscoveredDevice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import javax.inject.Inject
import javax.inject.Singleton

/**
 * UDP broadcast fallback for networks/routers that block mDNS multicast.
 * Every device periodically broadcasts a small "I'm here" beacon; listeners
 * pick it up and treat it exactly like an NSD result.
 */
@Singleton
class UdpPresenceBeacon
    @Inject
    constructor() {
        suspend fun broadcastLoop(
            deviceId: String,
            deviceName: String,
            tcpPort: Int,
        ) {
            DatagramSocket().use { socket ->
                socket.broadcast = true
                val payload = "OPENFIND|$deviceId|$deviceName|$tcpPort".toByteArray(Charsets.UTF_8)
                val broadcastAddress = InetAddress.getByName("255.255.255.255")
                while (kotlinx.coroutines.currentCoroutineContext().isActive) {
                    runCatching {
                        val packet = DatagramPacket(payload, payload.size, broadcastAddress, NetworkConstants.UDP_BEACON_PORT)
                        socket.send(packet)
                    }.onFailure { Timber.w(it, "UDP beacon send failed") }
                    kotlinx.coroutines.delay(NetworkConstants.UDP_BEACON_INTERVAL_MS)
                }
            }
        }

        fun listen(): Flow<DiscoveredDevice> =
            callbackFlow {
                val socket =
                    runCatching {
                        DatagramSocket(null).apply {
                            reuseAddress = true
                            bind(InetSocketAddress(NetworkConstants.UDP_BEACON_PORT))
                        }
                    }.getOrElse {
                        close(it)
                        return@callbackFlow
                    }

                val job =
                    CoroutineScope(Dispatchers.IO).launch {
                        val buffer = ByteArray(512)
                        while (isActive) {
                            val packet = DatagramPacket(buffer, buffer.size)
                            try {
                                socket.receive(packet)
                                val text = String(packet.data, 0, packet.length, Charsets.UTF_8)
                                val parts = text.split("|")
                                if (parts.size == 4 && parts[0] == "OPENFIND") {
                                    trySend(
                                        DiscoveredDevice(
                                            serviceName = parts[2],
                                            host = packet.address.hostAddress ?: continue,
                                            port = parts[3].toIntOrNull() ?: continue,
                                            publicKeyBase64 = null,
                                        ),
                                    )
                                }
                            } catch (
                                @Suppress("TooGenericExceptionCaught") t: Throwable,
                            ) {
                                // Broad catch is deliberate: one malformed/failed packet must never kill this loop.
                                if (isActive) Timber.w(t, "UDP beacon receive failed")
                            }
                        }
                    }

                awaitClose {
                    job.cancel()
                    socket.close()
                }
            }
    }
