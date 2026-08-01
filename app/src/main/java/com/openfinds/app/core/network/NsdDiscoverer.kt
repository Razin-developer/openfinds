package com.openfinds.app.core.network

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import com.openfinds.app.core.domain.model.DiscoveredDevice
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/** Discovers other OpenFind devices advertising on the local network via mDNS/NSD. */
@Singleton
class NsdDiscoverer
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private val nsdManager by lazy { context.getSystemService(Context.NSD_SERVICE) as NsdManager }

        fun discover(): Flow<DiscoveredDevice> =
            callbackFlow {
                val resolveListener =
                    object : NsdManager.ResolveListener {
                        override fun onResolveFailed(
                            serviceInfo: NsdServiceInfo,
                            errorCode: Int,
                        ) {
                            Timber.w("NSD resolve failed for ${serviceInfo.serviceName}: $errorCode")
                        }

                        override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                            val host = serviceInfo.host?.hostAddress ?: return
                            // The NSD service name itself is a technical, ID-based identifier
                            // (see NsdAdvertiser); the human-readable name and stable device ID
                            // travel in the TXT record so devices show the same friendly name
                            // and dedupe correctly whether found via NSD or UDP.
                            val deviceId =
                                runCatching { serviceInfo.attributes["deviceId"]?.toString(Charsets.UTF_8) }
                                    .getOrNull()
                                    ?.takeIf { it.isNotBlank() }
                                    ?: serviceInfo.serviceName
                            val friendlyName =
                                runCatching { serviceInfo.attributes["deviceName"]?.toString(Charsets.UTF_8) }
                                    .getOrNull()
                                    ?.takeIf { it.isNotBlank() }
                                    ?: serviceInfo.serviceName
                            trySend(
                                DiscoveredDevice(
                                    deviceId = deviceId,
                                    serviceName = friendlyName,
                                    host = host,
                                    port = serviceInfo.port,
                                    publicKeyBase64 = null,
                                ),
                            )
                        }
                    }

                val discoveryListener =
                    object : NsdManager.DiscoveryListener {
                        override fun onDiscoveryStarted(serviceType: String) {
                            Timber.d("NSD discovery started for $serviceType")
                        }

                        override fun onServiceFound(serviceInfo: NsdServiceInfo) {
                            runCatching { nsdManager.resolveService(serviceInfo, resolveListener) }
                        }

                        override fun onServiceLost(serviceInfo: NsdServiceInfo) {
                            Timber.d("NSD service lost: ${serviceInfo.serviceName}")
                        }

                        override fun onDiscoveryStopped(serviceType: String) {
                            Timber.d("NSD discovery stopped for $serviceType")
                        }

                        override fun onStartDiscoveryFailed(
                            serviceType: String,
                            errorCode: Int,
                        ) {
                            Timber.w("NSD start discovery failed: $errorCode")
                            close(IllegalStateException("Start discovery failed: $errorCode"))
                        }

                        override fun onStopDiscoveryFailed(
                            serviceType: String,
                            errorCode: Int,
                        ) {
                            Timber.w("NSD stop discovery failed: $errorCode")
                        }
                    }

                runCatching {
                    nsdManager.discoverServices(NetworkConstants.NSD_SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
                }.onFailure { close(it) }

                awaitClose {
                    runCatching { nsdManager.stopServiceDiscovery(discoveryListener) }
                }
            }
    }
