package com.toeicspace.android.feature.vocabulary.presentation.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toeicspace.android.core.common.AppError
import com.toeicspace.android.core.common.AppResult
import com.toeicspace.android.core.data.repository.SessionRepository
import com.toeicspace.android.core.model.SessionState
import com.toeicspace.android.feature.vocabulary.domain.ReviewRepository
import com.toeicspace.android.feature.vocabulary.domain.ReviewSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReviewSummaryUiState(
    /** Unknown until the session is read; guests get a sign-in invitation instead of numbers. */
    val isSignedIn: Boolean? = null,
    val isLoading: Boolean = false,
    val summary: ReviewSummary? = null,
    val error: AppError? = null,
    val fromCache: Boolean = false,
    /** Answers given offline and not sent yet. */
    val pendingAnswers: Int = 0,
)

/** The review card at the top of the "Từ vựng" tab: how many words are due today. */
@HiltViewModel
class ReviewSummaryViewModel
    @Inject
    constructor(
        private val repository: ReviewRepository,
        sessionRepository: SessionRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(ReviewSummaryUiState())
        val uiState: StateFlow<ReviewSummaryUiState> = _uiState.asStateFlow()

        private var loadJob: Job? = null

        init {
            viewModelScope.launch {
                sessionRepository.session
                    .filterIsInstance<SessionState.Ready>()
                    .map { it.isSignedIn }
                    .distinctUntilChanged()
                    .collect { signedIn ->
                        _uiState.update {
                            ReviewSummaryUiState(
                                isSignedIn = signedIn,
                                pendingAnswers = it.pendingAnswers,
                            )
                        }
                        if (signedIn) load()
                    }
            }
            viewModelScope.launch {
                repository.pendingAnswerCount.collect { count ->
                    _uiState.update { it.copy(pendingAnswers = count) }
                }
            }
        }

        /** Called when the tab comes back into view, e.g. after a session, so the count is current. */
        fun onRefresh() {
            if (_uiState.value.isSignedIn == true) load()
        }

        fun onRetry() = onRefresh()

        private fun load() {
            loadJob?.cancel()
            _uiState.update { it.copy(isLoading = true, error = null) }
            loadJob =
                viewModelScope.launch {
                    when (val result = repository.getSummary()) {
                        is AppResult.Success ->
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    summary = result.data.data,
                                    fromCache = result.data.fromCache,
                                )
                            }
                        is AppResult.Failure ->
                            _uiState.update { it.copy(isLoading = false, error = result.error) }
                    }
                }
        }
    }
