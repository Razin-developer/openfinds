package com.openfinds.app.feature.devices

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.openfinds.app.core.domain.model.DeviceGroup
import com.openfinds.app.core.domain.model.DeviceSnapshot
import com.openfinds.app.core.domain.model.TrustedDevice
import com.openfinds.app.core.domain.repository.DeviceGroupRepository
import com.openfinds.app.core.domain.repository.DeviceRepository
import com.openfinds.app.core.navigation.OpenFindDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DeviceDetailsUiState(
    val device: TrustedDevice? = null,
    val snapshot: DeviceSnapshot? = null,
    val groups: List<DeviceGroup> = emptyList(),
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class DeviceDetailsViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val deviceRepository: DeviceRepository,
        private val groupRepository: DeviceGroupRepository,
    ) : ViewModel() {
        private val deviceId: String = savedStateHandle.toRoute<OpenFindDestination.DeviceDetails>().deviceId

        private val _snapshotState = MutableStateFlow<DeviceSnapshot?>(null)
        private val _isRefreshing = MutableStateFlow(false)
        private val _errorMessage = MutableStateFlow<String?>(null)

        val uiState: StateFlow<DeviceDetailsUiState> =
            combine(
                deviceRepository.observeDevice(deviceId),
                groupRepository.observeGroups(),
                _snapshotState,
                _isRefreshing,
                _errorMessage,
            ) { device, groups, snapshot, refreshing, error ->
                DeviceDetailsUiState(device, snapshot, groups, refreshing, error)
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DeviceDetailsUiState())

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
                    onSuccess = { snapshot ->
                        _snapshotState.value = snapshot
                        _errorMessage.value = null
                    },
                    onFailure = { error -> _errorMessage.value = error.message ?: "Couldn't reach this device" },
                )
                _isRefreshing.value = false
            }
        }

        fun rename(nickname: String) {
            viewModelScope.launch { deviceRepository.renameDevice(deviceId, nickname) }
        }

        fun setAvatarImage(imageUri: String?) {
            viewModelScope.launch { deviceRepository.setAvatarImage(deviceId, imageUri) }
        }

        fun assignGroup(groupId: String?) {
            viewModelScope.launch { groupRepository.assignDevice(deviceId, groupId) }
        }

        fun forget(onForgotten: () -> Unit) {
            viewModelScope.launch {
                deviceRepository.forgetDevice(deviceId)
                onForgotten()
            }
        }
    }
