package com.openfinds.app.feature.devices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openfinds.app.core.domain.model.ConnectionState
import com.openfinds.app.core.domain.model.TrustedDevice
import com.openfinds.app.core.domain.repository.DeviceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

enum class DeviceSortOrder { NAME, LAST_SEEN, ONLINE_FIRST }

data class DevicesUiState(
    val allDevices: List<TrustedDevice> = emptyList(),
    val visibleDevices: List<TrustedDevice> = emptyList(),
    val query: String = "",
    val sortOrder: DeviceSortOrder = DeviceSortOrder.ONLINE_FIRST,
    val isLoading: Boolean = true,
)

@HiltViewModel
class DevicesViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val sortOrder = MutableStateFlow(DeviceSortOrder.ONLINE_FIRST)

    val uiState: StateFlow<DevicesUiState> = combine(
        deviceRepository.observeTrustedDevices(),
        query,
        sortOrder,
    ) { devices, currentQuery, order ->
        val filtered = devices.filter {
            currentQuery.isBlank() || it.name.contains(currentQuery, ignoreCase = true)
        }
        val sorted = when (order) {
            DeviceSortOrder.NAME -> filtered.sortedBy { it.name.lowercase() }
            DeviceSortOrder.LAST_SEEN -> filtered.sortedByDescending { it.lastSeenEpochMillis ?: 0L }
            DeviceSortOrder.ONLINE_FIRST -> filtered.sortedWith(
                compareByDescending<TrustedDevice> { it.connectionState == ConnectionState.ONLINE }
                    .thenBy { it.name.lowercase() },
            )
        }
        DevicesUiState(
            allDevices = devices,
            visibleDevices = sorted,
            query = currentQuery,
            sortOrder = order,
            isLoading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DevicesUiState())

    fun onQueryChange(newQuery: String) {
        query.value = newQuery
    }

    fun onSortOrderChange(newOrder: DeviceSortOrder) {
        sortOrder.value = newOrder
    }
}
