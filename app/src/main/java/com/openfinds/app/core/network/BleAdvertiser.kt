package com.openfinds.app.core.network

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.content.Context
import android.os.ParcelUuid
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Advertises a fixed OpenFind service UUID over BLE so other OpenFind
 * devices can sense this one is physically nearby, even before both devices
 * have joined the same Wi-Fi network. BLE alone carries no host/port, so it
 * only speeds up *awareness* — actual pairing still happens over NSD/UDP+TCP
 * once both devices share a network.
 */
@Singleton
class BleAdvertiser
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private var callback: AdvertiseCallback? = null

        private val bluetoothManager: BluetoothManager?
            get() = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager

        @SuppressLint("MissingPermission")
        fun start() {
            if (callback != null) return
            val advertiser =
                bluetoothManager?.adapter?.bluetoothLeAdvertiser ?: run {
                    Timber.d("BLE advertising unavailable on this device")
                    return
                }

            val settings =
                AdvertiseSettings.Builder()
                    .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                    .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_LOW)
                    .setConnectable(false)
                    .build()

            val data =
                AdvertiseData.Builder()
                    .addServiceUuid(ParcelUuid(OPENFIND_SERVICE_UUID))
                    .setIncludeDeviceName(false)
                    .build()

            val newCallback =
                object : AdvertiseCallback() {
                    override fun onStartFailure(errorCode: Int) {
                        Timber.w("BLE advertise failed to start: $errorCode")
                    }
                }
            callback = newCallback
            runCatching { advertiser.startAdvertising(settings, data, newCallback) }
                .onFailure { Timber.w(it, "BLE advertise threw") }
        }

        @SuppressLint("MissingPermission")
        fun stop() {
            val advertiser = bluetoothManager?.adapter?.bluetoothLeAdvertiser
            callback?.let { runCatching { advertiser?.stopAdvertising(it) } }
            callback = null
        }

        companion object {
            /** A fixed, randomly-generated UUID identifying "this is an OpenFind device" over BLE. */
            val OPENFIND_SERVICE_UUID: UUID = UUID.fromString("6f70656e-6669-6e64-2d62-6c652d766401")
        }
    }
