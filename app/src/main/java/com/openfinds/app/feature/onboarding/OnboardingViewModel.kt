package com.openfinds.app.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openfinds.app.core.data.datastore.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel
    @Inject
    constructor(
        private val preferencesRepository: UserPreferencesRepository,
    ) : ViewModel() {
        fun onGetStarted(onDone: () -> Unit) {
            viewModelScope.launch {
                preferencesRepository.setOnboardingCompleted(true)
                onDone()
            }
        }
    }

@HiltViewModel
class PermissionsViewModel
    @Inject
    constructor(
        private val preferencesRepository: UserPreferencesRepository,
    ) : ViewModel() {
        fun onPermissionsAcknowledged(onDone: () -> Unit) {
            viewModelScope.launch {
                preferencesRepository.setPermissionsAcknowledged(true)
                onDone()
            }
        }
    }
