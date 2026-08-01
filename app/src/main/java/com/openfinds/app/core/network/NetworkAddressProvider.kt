package com.openfinds.app.core.network

import android.content.Context
import android.net.ConnectivityManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.net.Inet4Address
import java.net.NetworkInterface
import javax.inject.Inject
import javax.inject.Singleton

/** Resolves this device's own local Wi-Fi IPv4 address, for embedding in pairing QR codes. */
@Singleton
class NetworkAddressProvider
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        fun currentLocalIpv4Address(): String? {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val activeNetwork = connectivityManager?.activeNetwork
            val linkProperties = activeNetwork?.let { connectivityManager.getLinkProperties(it) }
            linkProperties?.linkAddresses?.forEach { linkAddress ->
                val address = linkAddress.address
                if (address is Inet4Address && !address.isLoopbackAddress) {
                    return address.hostAddress
                }
            }
            return fallbackViaNetworkInterfaces()
        }

        private fun fallbackViaNetworkInterfaces(): String? =
            runCatching {
                NetworkInterface.getNetworkInterfaces().asSequence()
                    .flatMap { it.inetAddresses.asSequence() }
                    .filterIsInstance<Inet4Address>()
                    .firstOrNull { !it.isLoopbackAddress }
                    ?.hostAddress
            }.getOrNull()
    }
