package com.openfinds.app.feature.devices

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openfinds.app.core.domain.model.DeviceSnapshot
import com.openfinds.app.core.domain.model.TrustedDevice
import com.openfinds.app.core.domain.repository.DeviceRepository
import com.openfinds.app.core.navigation.OpenFindDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DeviceDetailsUiState(
    val device: TrustedDevice? = null,
    val snapshot: DeviceSnapshot? = null,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class DeviceDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val deviceRepository: DeviceRepository,
) : ViewModel() {

    private val deviceId: String = savedStateHandle.toRoute<OpenFindDestination.DeviceDetails>().deviceId

    private val _snapshotState = MutableStateFlow<DeviceSnapshot?>(null)
    private val _isRefreshing = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<DeviceDetailsUiState> = deviceRepository.observeDevice(deviceId)
        .let { deviceFlow ->
            kotlinx.coroutines.flow.combine(deviceFlow, _snapshotState, _isRefreshing, _errorMessage) { device, snapshot, refreshing, error ->
                DeviceDetailsUiState(device, snapshot, refreshing, error)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DeviceDetailsUiState())

    init {
        refreshStatus()
    }

    fun refreshStatus() {
        _isRefreshing.value = true
        viewModelScope.launch {
            val device = deviceRepository.observeDevice(deviceId).first()
            if (device == null) {
                _isRefreshing.value = false
                return@launch
            }
            deviceRepository.requestStatus(device).fold(
                onSuccess = { snapshot -> _snapshotState.value = snapshot; _errorMessage.value = null },
                onFailure = { error -> _errorMessage.value = error.message ?: "Couldn't reach this device" },
            )
            _isRefreshing.value = false
        }
    }

    fun rename(nickname: String) {
        viewModelScope.launch { deviceRepository.renameDevice(deviceId, nickname) }
    }

    fun forget(onForgotten: () -> Unit) {
        viewModelScope.launch {
            deviceRepository.forgetDevice(deviceId)
            onForgotten()
        }
    }
}
