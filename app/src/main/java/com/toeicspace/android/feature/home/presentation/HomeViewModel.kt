package com.toeicspace.android.feature.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toeicspace.android.core.data.repository.SessionRepository
import com.toeicspace.android.core.model.SessionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface HomeUiState {
    data object Loading : HomeUiState

    data class SignedIn(
        val fullName: String,
    ) : HomeUiState

    data object Guest : HomeUiState

    /** Signed out here, or the session expired on the server: the user has to sign in again. */
    data object SessionEnded : HomeUiState
}

@HiltViewModel
class HomeViewModel
    @Inject
    constructor(
        private val sessionRepository: SessionRepository,
    ) : ViewModel() {
        val uiState: StateFlow<HomeUiState> =
            sessionRepository.session
                .map { session ->
                    when {
                        session !is SessionState.Ready -> HomeUiState.Loading
                        session.user != null -> HomeUiState.SignedIn(session.user.fullName)
                        session.isGuest -> HomeUiState.Guest
                        else -> HomeUiState.SessionEnded
                    }
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = HomeUiState.Loading,
                )

        fun onSignOutClick() {
            viewModelScope.launch { sessionRepository.signOut() }
        }
    }
