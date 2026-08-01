package com.openfinds.app.core.network

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** Advertises this device on the local network via mDNS/NSD so trusted peers can find it. */
@Singleton
class NsdAdvertiser
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private val nsdManager by lazy { context.getSystemService(Context.NSD_SERVICE) as NsdManager }
        private var registrationListener: NsdManager.RegistrationListener? = null

        suspend fun start(
            deviceId: String,
            deviceName: String,
            port: Int,
        ) {
            stop()
            val serviceInfo =
                NsdServiceInfo().apply {
                    serviceName = "OpenFind-${deviceId.take(8)}"
                    serviceType = NetworkConstants.NSD_SERVICE_TYPE
                    setPort(port)
                    setAttribute("deviceId", deviceId)
                    setAttribute("deviceName", deviceName)
                }

            suspendCancellableCoroutine<Unit> { continuation ->
                val listener =
                    object : NsdManager.RegistrationListener {
                        override fun onServiceRegistered(info: NsdServiceInfo) {
                            Timber.d("NSD service registered: ${info.serviceName}")
                            if (continuation.isActive) continuation.resume(Unit)
                        }

                        override fun onRegistrationFailed(
                            info: NsdServiceInfo,
                            errorCode: Int,
                        ) {
                            Timber.w("NSD registration failed: $errorCode")
                            if (continuation.isActive) {
                                continuation.resumeWithException(
                                    IllegalStateException("NSD registration failed: $errorCode"),
                                )
                            }
                        }

                        override fun onServiceUnregistered(info: NsdServiceInfo) {
                            Timber.d("NSD service unregistered: ${info.serviceName}")
                        }

                        override fun onUnregistrationFailed(
                            info: NsdServiceInfo,
                            errorCode: Int,
                        ) {
                            Timber.w("NSD unregistration failed: $errorCode")
                        }
                    }
                registrationListener = listener
                runCatching {
                    nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, listener)
                }.onFailure { if (continuation.isActive) continuation.resumeWithException(it) }
            }
        }

        fun stop() {
            registrationListener?.let { listener ->
                runCatching { nsdManager.unregisterService(listener) }
            }
            registrationListener = null
        }
    }
