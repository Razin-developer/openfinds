package com.openfinds.app.feature.find

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openfinds.app.core.domain.model.TrustedDevice
import com.openfinds.app.core.domain.repository.DeviceRepository
import com.openfinds.app.core.navigation.OpenFindDestination
import com.openfinds.app.core.network.protocol.DeviceAction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import androidx.navigation.toRoute
import javax.inject.Inject

enum class FindPhase { CONNECTING, ACTIVE, FAILED, STOPPED }

data class FindDeviceUiState(
    val device: TrustedDevice? = null,
    val phase: FindPhase = FindPhase.CONNECTING,
    val ringing: Boolean = false,
    val vibrating: Boolean = false,
    val flashing: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class FindDeviceViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val deviceRepository: DeviceRepository,
) : ViewModel() {

    private val deviceId: String = savedStateHandle.toRoute<OpenFindDestination.FindDevice>().deviceId

    private val _state = MutableStateFlow(FindDeviceUiState())
    val state: StateFlow<FindDeviceUiState> = _state

    init {
        viewModelScope.launch {
            val device = deviceRepository.observeDevice(deviceId).first()
            _state.value = _state.value.copy(device = device)
            if (device != null) startFind(device)
        }
    }

    private suspend fun startFind(device: TrustedDevice) {
        val ring = deviceRepository.sendFindAction(device, DeviceAction.RING)
        val vibrate = deviceRepository.sendFindAction(device, DeviceAction.VIBRATE)
        _state.value = _state.value.copy(
            phase = if (ring.isSuccess || vibrate.isSuccess) FindPhase.ACTIVE else FindPhase.FAILED,
            ringing = ring.isSuccess,
            vibrating = vibrate.isSuccess,
            errorMessage = if (ring.isFailure && vibrate.isFailure) ring.exceptionOrNull()?.message else null,
        )
    }

    fun toggleFlash() {
        val device = _state.value.device ?: return
        val turnOn = !_state.value.flashing
        viewModelScope.launch {
            val action = if (turnOn) DeviceAction.FLASH else DeviceAction.STOP_FIND
            val result = deviceRepository.sendFindAction(device, action)
            if (result.isSuccess) _state.value = _state.value.copy(flashing = turnOn)
        }
    }

    fun stopFind() {
        val device = _state.value.device ?: return
        viewModelScope.launch {
            deviceRepository.sendFindAction(device, DeviceAction.STOP_FIND)
            _state.value = _state.value.copy(phase = FindPhase.STOPPED, ringing = false, vibrating = false, flashing = false)
        }
    }

    override fun onCleared() {
        // viewModelScope is already cancelled by the time onCleared runs, so this best-effort
        // "stop ringing/vibrating" signal is fired on a short-lived scope of its own.
        val device = _state.value.device
        if (device != null && _state.value.phase == FindPhase.ACTIVE) {
            @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
            kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                deviceRepository.sendFindAction(device, DeviceAction.STOP_FIND)
            }
        }
        super.onCleared()
    }
}
