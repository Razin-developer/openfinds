package com.openfinds.app.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openfinds.app.core.data.datastore.AppPreferences
import com.openfinds.app.core.data.datastore.AppThemeMode
import com.openfinds.app.core.data.datastore.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel
    @Inject
    constructor(
        private val preferencesRepository: UserPreferencesRepository,
    ) : ViewModel() {
        val preferences: StateFlow<AppPreferences> =
            preferencesRepository.preferences
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppPreferences())

        fun setThemeMode(mode: AppThemeMode) {
            viewModelScope.launch { preferencesRepository.setThemeMode(mode) }
        }

        fun setBackgroundMonitoringEnabled(enabled: Boolean) {
            viewModelScope.launch { preferencesRepository.setBackgroundMonitoringEnabled(enabled) }
        }

        fun setAutoReconnectEnabled(enabled: Boolean) {
            viewModelScope.launch { preferencesRepository.setAutoReconnectEnabled(enabled) }
        }

        fun setDeviceDisplayName(name: String) {
            viewModelScope.launch { preferencesRepository.setDeviceDisplayName(name) }
        }
    }
