package com.openfinds.app.core.network

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.ParcelUuid
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/** Scans for the OpenFind BLE service UUID; emits a beacon each time a nearby advertiser is (re)seen. */
@Singleton
class BleScanner
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        @SuppressLint("MissingPermission")
        fun scan(): Flow<Unit> =
            callbackFlow {
                val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
                val scanner = bluetoothManager?.adapter?.bluetoothLeScanner
                if (scanner == null) {
                    close()
                    return@callbackFlow
                }

                val callback =
                    object : ScanCallback() {
                        override fun onScanResult(
                            callbackType: Int,
                            result: ScanResult,
                        ) {
                            trySend(Unit)
                        }

                        override fun onScanFailed(errorCode: Int) {
                            Timber.w("BLE scan failed: $errorCode")
                        }
                    }

                val filters = listOf(ScanFilter.Builder().setServiceUuid(ParcelUuid(BleAdvertiser.OPENFIND_SERVICE_UUID)).build())
                val settings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_POWER).build()

                runCatching { scanner.startScan(filters, settings, callback) }
                    .onFailure { close(it) }

                awaitClose { runCatching { scanner.stopScan(callback) } }
            }
    }
