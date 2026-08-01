package com.openfinds.app.feature.developer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openfinds.app.core.data.datastore.AppPreferences
import com.openfinds.app.core.data.datastore.UserPreferencesRepository
import com.openfinds.app.core.data.local.DeviceGroupDao
import com.openfinds.app.core.data.local.DeviceHistoryDao
import com.openfinds.app.core.data.local.TrustedDeviceDao
import com.openfinds.app.core.domain.model.DiscoveredDevice
import com.openfinds.app.core.domain.repository.DiscoveryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeveloperSettingsViewModel
    @Inject
    constructor(
        private val preferencesRepository: UserPreferencesRepository,
        private val discoveryRepository: DiscoveryRepository,
        private val trustedDeviceDao: TrustedDeviceDao,
        private val deviceGroupDao: DeviceGroupDao,
        private val deviceHistoryDao: DeviceHistoryDao,
    ) : ViewModel() {
        val preferences: StateFlow<AppPreferences> =
            preferencesRepository.preferences
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppPreferences())

        val rawDiscoveredDevices: StateFlow<List<DiscoveredDevice>> =
            discoveryRepository.discoverNearbyDevices()
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        fun setVerboseLoggingEnabled(enabled: Boolean) {
            viewModelScope.launch { preferencesRepository.setVerboseLoggingEnabled(enabled) }
        }

        fun setShowRawDiscoveredDevices(enabled: Boolean) {
            viewModelScope.launch { preferencesRepository.setShowRawDiscoveredDevices(enabled) }
        }

        fun clearLocalDatabase() {
            viewModelScope.launch {
                trustedDeviceDao.deleteAll()
                deviceGroupDao.deleteAll()
                deviceHistoryDao.clearAll()
            }
        }

        fun resetOnboarding() {
            viewModelScope.launch {
                preferencesRepository.setOnboardingCompleted(false)
                preferencesRepository.setPermissionsAcknowledged(false)
            }
        }
    }
