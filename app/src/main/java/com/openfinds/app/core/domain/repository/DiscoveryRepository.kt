package com.openfinds.app.core.domain.repository

import com.openfinds.app.core.domain.model.DiscoveredDevice
import com.openfinds.app.core.network.BleScanner
import com.openfinds.app.core.network.NsdDiscoverer
import com.openfinds.app.core.network.UdpPresenceBeacon
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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
            merge(
                nsdDiscoverer.discover().map { DiscoveryEvent.Sighting(it) },
                udpPresenceBeacon.listen().map { DiscoveryEvent.Sighting(it) },
                stalenessTicker(),
            )
                .scan(emptyMap<String, Sighting>()) { acc, event ->
                    when (event) {
                        is DiscoveryEvent.Sighting -> {
                            // Keyed by the peer's stable device ID rather than host:port, so the same
                            // physical device found via both NSD and UDP — or one that gets a new DHCP
                            // lease mid-session — collapses to a single, up-to-date entry instead of
                            // appearing as a duplicate.
                            acc + (event.device.deviceId to Sighting(event.device, System.currentTimeMillis()))
                        }
                        DiscoveryEvent.Tick -> {
                            val now = System.currentTimeMillis()
                            acc.filterValues { now - it.seenAtMs < STALE_AFTER_MS }
                        }
                    }
                }
                .map { it.values.map(Sighting::device) }

        override fun bleNearbySignal(): Flow<Boolean> = bleScanner.scan().map { true }

        private fun stalenessTicker(): Flow<DiscoveryEvent> =
            flow {
                while (true) {
                    delay(STALE_CHECK_INTERVAL_MS)
                    emit(DiscoveryEvent.Tick)
                }
            }

        private data class Sighting(val device: DiscoveredDevice, val seenAtMs: Long)

        private sealed interface DiscoveryEvent {
            data class Sighting(val device: DiscoveredDevice) : DiscoveryEvent

            data object Tick : DiscoveryEvent
        }

        private companion object {
            /** A few missed UDP beacon cycles (4s each) before a device is considered gone. */
            const val STALE_AFTER_MS = 15_000L
            const val STALE_CHECK_INTERVAL_MS = 3_000L
        }
    }
