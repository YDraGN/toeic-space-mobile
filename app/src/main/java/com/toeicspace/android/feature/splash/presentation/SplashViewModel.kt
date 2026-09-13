package com.toeicspace.android.feature.splash.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toeicspace.android.core.datastore.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class SplashViewModel
    @Inject
    constructor(
        private val userPreferences: UserPreferences,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<SplashUiState>(SplashUiState.Loading)
        val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

        private var isSimulatingNetworkError = false

        init {
            checkInitialDestination()
        }

        fun restart(simulateError: Boolean = false) {
            isSimulatingNetworkError = simulateError
            _uiState.value = SplashUiState.Loading
            checkInitialDestination()
        }

        fun retryConnection() {
            isSimulatingNetworkError = !isSimulatingNetworkError
            _uiState.value = SplashUiState.Loading
            checkInitialDestination()
        }

        private fun checkInitialDestination() {
            viewModelScope.launch {
                delay(5000L.milliseconds)

                if (isSimulatingNetworkError) {
                    _uiState.value = SplashUiState.NetworkError("Không có kết nối Internet")
                    return@launch
                }

                val isOnboarded = userPreferences.isOnboardingCompleted.first()
                val token = userPreferences.authToken.first()

                val target =
                    when {
                        !isOnboarded -> SplashNavTarget.ONBOARDING
                        token.isNullOrBlank() -> SplashNavTarget.AUTH
                        else -> SplashNavTarget.HOME
                    }

                _uiState.value = SplashUiState.Success(target)
            }
        }
    }
