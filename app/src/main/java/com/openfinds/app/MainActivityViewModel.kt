package com.openfinds.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openfinds.app.core.data.datastore.UserPreferencesRepository
import com.openfinds.app.core.navigation.OpenFindDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel
    @Inject
    constructor(
        preferencesRepository: UserPreferencesRepository,
    ) : ViewModel() {
        private val _startDestination = MutableStateFlow<OpenFindDestination?>(null)
        val startDestination: StateFlow<OpenFindDestination?> = _startDestination

        init {
            viewModelScope.launch {
                val prefs = preferencesRepository.preferences.first()
                _startDestination.value =
                    when {
                        !prefs.onboardingCompleted -> OpenFindDestination.Welcome
                        !prefs.permissionsAcknowledged -> OpenFindDestination.Permissions
                        prefs.lastSeenWhatsNewVersionCode in 1 until BuildConfig.VERSION_CODE -> OpenFindDestination.WhatsNew
                        else -> OpenFindDestination.Home
                    }
            }
        }
    }
