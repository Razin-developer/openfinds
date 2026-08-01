package com.openfinds.app.core.domain.repository

import com.openfinds.app.core.domain.model.DiscoveredDevice
import com.openfinds.app.core.network.BleScanner
import com.openfinds.app.core.network.NsdDiscoverer
import com.openfinds.app.core.network.UdpPresenceBeacon
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.scan
import javax.inject.Inject
import javax.inject.Singleton

interface DiscoveryRepository {
    /** Emits the running, de-duplicated (by host:port) set of devices seen so far in this discovery session. */
    fun discoverNearbyDevices(): Flow<List<DiscoveredDevice>>

    /**
     * Emits `true` whenever a BLE OpenFind beacon is (re)detected nearby. BLE alone carries no
     * host/port, so this is purely an informational "something is close by" signal — actual
     * pairing still requires NSD/UDP once both devices share the same Wi-Fi network.
     */
    fun bleNearbySignal(): Flow<Boolean>
}

@Singleton
class DiscoveryRepositoryImpl
    @Inject
    constructor(
        private val nsdDiscoverer: NsdDiscoverer,
        private val udpPresenceBeacon: UdpPresenceBeacon,
        private val bleScanner: BleScanner,
    ) : DiscoveryRepository {
        override fun discoverNearbyDevices(): Flow<List<DiscoveredDevice>> =
            merge(nsdDiscoverer.discover(), udpPresenceBeacon.listen())
                .scan(emptyMap<String, DiscoveredDevice>()) { acc, device ->
                    acc + (device.host + ":" + device.port to device)
                }
                .map { it.values.toList() }

        override fun bleNearbySignal(): Flow<Boolean> = bleScanner.scan().map { true }
    }
