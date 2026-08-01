package com.openfinds.app.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openfinds.app.core.domain.model.HistoryEvent
import com.openfinds.app.core.domain.repository.DeviceHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel
    @Inject
    constructor(
        private val historyRepository: DeviceHistoryRepository,
    ) : ViewModel() {
        val events: StateFlow<List<HistoryEvent>> =
            historyRepository.observeRecent()
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        fun clearHistory() {
            viewModelScope.launch { historyRepository.clearAll() }
        }
    }
