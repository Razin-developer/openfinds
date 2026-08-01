package com.openfinds.app.feature.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openfinds.app.core.domain.model.DeviceGroup
import com.openfinds.app.core.domain.repository.DeviceGroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeviceGroupsViewModel
    @Inject
    constructor(
        private val groupRepository: DeviceGroupRepository,
    ) : ViewModel() {
        val groups: StateFlow<List<DeviceGroup>> =
            groupRepository.observeGroups()
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        fun createGroup(
            name: String,
            colorArgb: Int,
        ) {
            if (name.isBlank()) return
            viewModelScope.launch { groupRepository.createGroup(name, colorArgb) }
        }

        fun renameGroup(
            groupId: String,
            name: String,
        ) {
            if (name.isBlank()) return
            viewModelScope.launch { groupRepository.renameGroup(groupId, name) }
        }

        fun deleteGroup(groupId: String) {
            viewModelScope.launch { groupRepository.deleteGroup(groupId) }
        }
    }
