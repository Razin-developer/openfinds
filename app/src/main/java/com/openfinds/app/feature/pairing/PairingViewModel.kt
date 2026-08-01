package com.openfinds.app.feature.pairing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openfinds.app.core.crypto.DeviceIdentityStore
import com.openfinds.app.core.data.datastore.UserPreferencesRepository
import com.openfinds.app.core.domain.model.DiscoveredDevice
import com.openfinds.app.core.domain.repository.DiscoveryRepository
import com.openfinds.app.core.domain.repository.PairingRepository
import com.openfinds.app.core.network.NetworkAddressProvider
import com.openfinds.app.core.network.NetworkConstants
import com.openfinds.app.core.network.PairingOutcome
import com.openfinds.app.core.network.PairingRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface PairingUiState {
    data object Idle : PairingUiState

    data object Connecting : PairingUiState

    data class Result(val outcome: PairingOutcome) : PairingUiState
}

@HiltViewModel
class PairDiscoverViewModel
    @Inject
    constructor(
        private val discoveryRepository: DiscoveryRepository,
        private val pairingRepository: PairingRepository,
        private val identityStore: DeviceIdentityStore,
        private val addressProvider: NetworkAddressProvider,
        private val preferencesRepository: UserPreferencesRepository,
    ) : ViewModel() {
        val nearbyDevices: StateFlow<List<DiscoveredDevice>> =
            discoveryRepository.discoverNearbyDevices()
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        val bleNearbySignal: StateFlow<Boolean> =
            discoveryRepository.bleNearbySignal()
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

        private val _activePin = MutableStateFlow<String?>(null)
        val activePin: StateFlow<String?> = _activePin.asStateFlow()

        private val _myQrPayload = MutableStateFlow<String?>(null)
        val myQrPayload: StateFlow<String?> = _myQrPayload.asStateFlow()

        private val _uiState = MutableStateFlow<PairingUiState>(PairingUiState.Idle)
        val uiState: StateFlow<PairingUiState> = _uiState.asStateFlow()

        val incomingPairingRequests: StateFlow<PairingRequest?> =
            pairingRepository.incomingPairingRequests
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        fun startPinPairing() {
            val pin = pairingRepository.generatePin()
            pairingRepository.beginPinPairing(pin)
            _activePin.value = pin
        }

        fun stopPinPairing() {
            pairingRepository.endPinPairing()
            _activePin.value = null
        }

        fun loadMyQrPayload() {
            viewModelScope.launch {
                val identity = identityStore.getOrCreate()
                val host = addressProvider.currentLocalIpv4Address() ?: return@launch
                val name =
                    preferencesRepository.preferences.first().deviceDisplayName
                        .ifBlank { android.os.Build.MODEL ?: "Android device" }
                _myQrPayload.value =
                    QrPairingPayload(
                        deviceId = identity.deviceId,
                        deviceName = name,
                        host = host,
                        port = NetworkConstants.TCP_PORT,
                        identityPublicKeyB64 = java.util.Base64.getEncoder().encodeToString(identity.publicKey),
                    ).encode()
            }
        }

        fun clearMyQrPayload() {
            _myQrPayload.value = null
        }

        fun pairWithPin(
            device: DiscoveredDevice,
            pin: String,
        ) {
            _uiState.value = PairingUiState.Connecting
            viewModelScope.launch {
                val outcome = pairingRepository.pairViaPin(device.host, device.port, pin)
                _uiState.value = PairingUiState.Result(outcome)
            }
        }

        fun pairWithQrPayload(payload: QrPairingPayload) {
            _uiState.value = PairingUiState.Connecting
            viewModelScope.launch {
                val outcome = pairingRepository.pairViaQr(payload.host, payload.port, payload.identityPublicKeyB64)
                _uiState.value = PairingUiState.Result(outcome)
            }
        }

        fun acceptIncoming(request: PairingRequest) {
            viewModelScope.launch { pairingRepository.acceptIncoming(request) }
        }

        fun rejectIncoming(request: PairingRequest) {
            viewModelScope.launch { pairingRepository.rejectIncoming(request) }
        }

        fun resetUiState() {
            _uiState.value = PairingUiState.Idle
        }
    }
