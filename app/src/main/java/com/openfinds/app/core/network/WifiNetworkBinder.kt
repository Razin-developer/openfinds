package com.openfinds.app.core.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Binds this process's default network to Wi-Fi so every outbound socket this app opens
 * (P2P TCP connect, UDP beacon send) routes over the LAN.
 *
 * Without this, a phone with both Wi-Fi and a SIM can have Android pick cellular as the
 * process default network even while Wi-Fi is connected. Discovery still works (broadcast
 * and mDNS packets go out over Wi-Fi's own multicast/broadcast path), but a plain outbound
 * `connect()` to a peer's private Wi-Fi address gets routed over cellular instead and times
 * out — pairing fails right after the device shows up in "Nearby devices".
 */
@Singleton
class WifiNetworkBinder
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

        private var callback: ConnectivityManager.NetworkCallback? = null

        @Synchronized
        fun ensureBoundToWifi() {
            val manager = connectivityManager ?: return
            if (callback != null) return

            val request =
                NetworkRequest.Builder()
                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    .build()
            val newCallback =
                object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        runCatching { manager.bindProcessToNetwork(network) }
                            .onSuccess { Timber.i("Bound process to Wi-Fi network for P2P sockets") }
                            .onFailure { Timber.w(it, "Failed to bind process to Wi-Fi network") }
                    }

                    override fun onLost(network: Network) {
                        runCatching { manager.bindProcessToNetwork(null) }
                    }
                }

            runCatching { manager.requestNetwork(request, newCallback) }
                .onSuccess { callback = newCallback }
                .onFailure { Timber.w(it, "Failed to request Wi-Fi network for process binding") }
        }
    }
