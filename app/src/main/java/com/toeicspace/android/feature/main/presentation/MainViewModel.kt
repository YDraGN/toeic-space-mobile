package com.toeicspace.android.feature.main.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toeicspace.android.core.common.Clock
import com.toeicspace.android.core.data.repository.SessionRepository
import com.toeicspace.android.core.model.SessionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

/** Who is using the shell. Derived from [SessionState], so it works offline. */
sealed interface Viewer {
    data object Guest : Viewer

    data class Member(
        val fullName: String,
        val email: String,
    ) : Viewer
}

/** Same hours as the web teacher shell: morning 5–11, afternoon 12–17, evening otherwise. */
enum class TimeOfDay {
    MORNING,
    AFTERNOON,
    EVENING,
    ;

    companion object {
        fun fromHour(hour: Int): TimeOfDay =
            when (hour) {
                in 5..11 -> MORNING
                in 12..17 -> AFTERNOON
                else -> EVENING
            }
    }
}

sealed interface MainUiState {
    data object Loading : MainUiState

    data class Ready(
        val viewer: Viewer,
        val timeOfDay: TimeOfDay,
    ) : MainUiState

    /** Signed out here, or the session expired on the server: the user has to sign in again. */
    data object SessionEnded : MainUiState
}

@HiltViewModel
class MainViewModel
    @Inject
    constructor(
        private val sessionRepository: SessionRepository,
        private val clock: Clock,
    ) : ViewModel() {
        val uiState: StateFlow<MainUiState> =
            sessionRepository.session
                .map { session ->
                    val viewer =
                        when {
                            session !is SessionState.Ready -> return@map MainUiState.Loading
                            session.user != null ->
                                Viewer.Member(
                                    fullName = session.user.fullName,
                                    email = session.user.email,
                                )
                            session.isGuest -> Viewer.Guest
                            else -> return@map MainUiState.SessionEnded
                        }
                    MainUiState.Ready(viewer = viewer, timeOfDay = currentTimeOfDay())
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = MainUiState.Loading,
                )

        fun onSignOutClick() {
            viewModelScope.launch { sessionRepository.signOut() }
        }

        private fun currentTimeOfDay(): TimeOfDay {
            val hour = Instant.ofEpochMilli(clock.nowMillis()).atZone(ZoneId.systemDefault()).hour
            return TimeOfDay.fromHour(hour)
        }
    }
