package com.openfinds.app.feature.devices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openfinds.app.core.domain.model.ConnectionState
import com.openfinds.app.core.domain.model.DeviceGroup
import com.openfinds.app.core.domain.model.TrustedDevice
import com.openfinds.app.core.domain.repository.DeviceGroupRepository
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
    val groups: List<DeviceGroup> = emptyList(),
    val selectedGroupId: String? = null,
    val query: String = "",
    val sortOrder: DeviceSortOrder = DeviceSortOrder.ONLINE_FIRST,
    val isLoading: Boolean = true,
)

@HiltViewModel
class DevicesViewModel
    @Inject
    constructor(
        private val deviceRepository: DeviceRepository,
        private val groupRepository: DeviceGroupRepository,
    ) : ViewModel() {
        private val query = MutableStateFlow("")
        private val sortOrder = MutableStateFlow(DeviceSortOrder.ONLINE_FIRST)
        private val selectedGroupId = MutableStateFlow<String?>(null)

        val uiState: StateFlow<DevicesUiState> =
            combine(
                deviceRepository.observeTrustedDevices(),
                groupRepository.observeGroups(),
                query,
                sortOrder,
                selectedGroupId,
            ) { devices, groups, currentQuery, order, groupFilter ->
                val filtered =
                    devices
                        .filter { currentQuery.isBlank() || it.name.contains(currentQuery, ignoreCase = true) }
                        .filter { groupFilter == null || it.groupId == groupFilter }
                val sorted =
                    when (order) {
                        DeviceSortOrder.NAME -> filtered.sortedBy { it.name.lowercase() }
                        DeviceSortOrder.LAST_SEEN -> filtered.sortedByDescending { it.lastSeenEpochMillis ?: 0L }
                        DeviceSortOrder.ONLINE_FIRST ->
                            filtered.sortedWith(
                                compareByDescending<TrustedDevice> { it.connectionState == ConnectionState.ONLINE }
                                    .thenBy { it.name.lowercase() },
                            )
                    }
                DevicesUiState(
                    allDevices = devices,
                    visibleDevices = sorted,
                    groups = groups,
                    selectedGroupId = groupFilter,
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

        fun onGroupFilterChange(groupId: String?) {
            selectedGroupId.value = groupId
        }
    }
