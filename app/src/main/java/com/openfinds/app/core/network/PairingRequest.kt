package com.openfinds.app.core.network

import com.openfinds.app.core.network.protocol.PairingMode

/** An inbound pairing attempt from a peer, waiting for this device's user to accept or reject it. */
data class PairingRequest(
    val fromDeviceId: String,
    val fromDeviceName: String,
    val fromIdentityPublicKeyB64: String,
    val mode: PairingMode,
    val remoteHost: String,
    val respond: suspend (accepted: Boolean) -> Unit,
)

sealed interface PairingOutcome {
    data class Success(
        val peerDeviceId: String,
        val peerDeviceName: String,
        val peerIdentityPublicKeyB64: String,
        val host: String,
        val port: Int,
    ) : PairingOutcome

    data class Failure(val reason: String) : PairingOutcome
}
