package com.openfinds.app.core.domain.repository

import com.openfinds.app.core.domain.model.DiscoveredDevice
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
}

@Singleton
class DiscoveryRepositoryImpl @Inject constructor(
    private val nsdDiscoverer: NsdDiscoverer,
    private val udpPresenceBeacon: UdpPresenceBeacon,
) : DiscoveryRepository {

    override fun discoverNearbyDevices(): Flow<List<DiscoveredDevice>> =
        merge(nsdDiscoverer.discover(), udpPresenceBeacon.listen())
            .scan(emptyMap<String, DiscoveredDevice>()) { acc, device ->
                acc + (device.host + ":" + device.port to device)
            }
            .map { it.values.toList() }
}
