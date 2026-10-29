package com.toeicspace.android.feature.splash.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toeicspace.android.core.data.repository.SessionRepository
import com.toeicspace.android.core.network.NetworkMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel
    @Inject
    constructor(
        private val networkMonitor: NetworkMonitor,
        private val sessionRepository: SessionRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<SplashUiState>(SplashUiState.Loading)
        val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

        private var resolveJob: Job? = null

        init {
            resolveStartDestination()
        }

        fun onRetryClick() {
            resolveStartDestination()
        }

        private fun resolveStartDestination() {
            resolveJob?.cancel()
            _uiState.value = SplashUiState.Loading
            resolveJob =
                viewModelScope.launch {
                    if (!networkMonitor.isOnline.first()) {
                        _uiState.value = SplashUiState.NetworkError
                        return@launch
                    }
                    val startDestination =
                        if (sessionRepository.isOnboardingCompleted.first()) {
                            StartDestination.HOME
                        } else {
                            StartDestination.ONBOARDING
                        }
                    _uiState.value = SplashUiState.Ready(startDestination)
                }
        }
    }
