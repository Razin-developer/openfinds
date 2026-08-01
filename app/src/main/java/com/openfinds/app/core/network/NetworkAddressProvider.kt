package com.openfinds.app.core.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import dagger.hilt.android.qualifiers.ApplicationContext
import java.net.Inet4Address
import java.net.NetworkInterface
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Resolves this device's own local Wi-Fi IPv4 address, for embedding in pairing QR codes and
 * for the UDP beacon.
 *
 * Deliberately does **not** use `ConnectivityManager.activeNetwork`: on a phone with both Wi-Fi
 * and a SIM, Android can consider mobile data the "active" network for internet-bound traffic
 * even while Wi-Fi is connected (e.g. if Wi-Fi hasn't validated internet access), which would
 * embed an unreachable cellular address in the QR code instead of the LAN address peers can
 * actually connect to. This looks specifically for the network whose transport is Wi-Fi.
 */
@Singleton
class NetworkAddressProvider
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        fun currentLocalIpv4Address(): String? {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            wifiAddressFrom(connectivityManager)?.let { return it }
            return fallbackViaNetworkInterfaces()
        }

        private fun wifiAddressFrom(connectivityManager: ConnectivityManager?): String? {
            if (connectivityManager == null) return null
            val wifiNetwork =
                connectivityManager.allNetworks.firstOrNull { network ->
                    val capabilities = connectivityManager.getNetworkCapabilities(network)
                    capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
                } ?: return null

            val linkProperties = connectivityManager.getLinkProperties(wifiNetwork) ?: return null
            return linkProperties.linkAddresses
                .map { it.address }
                .filterIsInstance<Inet4Address>()
                .firstOrNull { !it.isLoopbackAddress }
                ?.hostAddress
        }

        /** Only reached if there's no Wi-Fi network at all — prefers a `wlan*` interface if present. */
        private fun fallbackViaNetworkInterfaces(): String? =
            runCatching {
                val interfaces = NetworkInterface.getNetworkInterfaces().toList()
                val wlanFirst = interfaces.sortedByDescending { it.name.startsWith("wlan") }
                wlanFirst.asSequence()
                    .flatMap { it.inetAddresses.asSequence() }
                    .filterIsInstance<Inet4Address>()
                    .firstOrNull { !it.isLoopbackAddress }
                    ?.hostAddress
            }.getOrNull()
    }
