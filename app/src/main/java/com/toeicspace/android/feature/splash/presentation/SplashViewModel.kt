package com.toeicspace.android.feature.splash.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toeicspace.android.core.data.repository.SessionRepository
import com.toeicspace.android.core.model.SessionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/** The brand splash stays at least this long, even when the session is ready sooner. */
internal const val MIN_SPLASH_MILLIS = 3_000L

/**
 * Picks the first screen from the locally stored session only. No network call is made, so the app
 * opens just as fast offline; the session is checked with the backend on the first protected request.
 * Navigation waits for both the session and [MIN_SPLASH_MILLIS], whichever finishes last.
 */
@HiltViewModel
class SplashViewModel
    @Inject
    constructor(
        private val sessionRepository: SessionRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<SplashUiState>(SplashUiState.Loading)
        val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

        init {
            viewModelScope.launch {
                val minimumDisplay = launch { delay(MIN_SPLASH_MILLIS) }
                val session =
                    sessionRepository.session
                        .filterIsInstance<SessionState.Ready>()
                        .first()
                minimumDisplay.join()
                _uiState.value = SplashUiState.Ready(session.startDestination())
            }
        }

        private fun SessionState.Ready.startDestination(): StartDestination =
            when {
                canEnterApp -> StartDestination.HOME
                !isOnboardingCompleted -> StartDestination.ONBOARDING
                else -> StartDestination.SIGN_IN
            }
    }
