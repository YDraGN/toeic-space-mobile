package com.toeicspace.android.feature.vocabulary.presentation.review

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toeicspace.android.core.common.AppError
import com.toeicspace.android.core.common.AppResult
import com.toeicspace.android.core.network.NetworkMonitor
import com.toeicspace.android.feature.vocabulary.data.repository.isConnectivity
import com.toeicspace.android.feature.vocabulary.domain.QuizQuestion
import com.toeicspace.android.feature.vocabulary.domain.ReviewGrade
import com.toeicspace.android.feature.vocabulary.domain.ReviewRepository
import com.toeicspace.android.feature.vocabulary.domain.ReviewResult
import com.toeicspace.android.feature.vocabulary.domain.ReviewSession
import com.toeicspace.android.feature.vocabulary.domain.ReviewSource
import com.toeicspace.android.feature.vocabulary.domain.VocabularyWord
import com.toeicspace.android.feature.vocabulary.navigation.ReviewSessionDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** The answer just given, shown until the learner moves on. */
data class AnswerFeedback(
    val question: QuizQuestion,
    val chosenWordId: String,
) {
    val isCorrect: Boolean get() = chosenWordId == question.wordId
}

data class ReviewSessionUiState(
    val source: ReviewSource = ReviewSource.DUE,
    val isLoading: Boolean = true,
    val session: ReviewSession? = null,
    val error: AppError? = null,
    /** The source had nothing to review (nothing due today, no mistakes). */
    val isEmpty: Boolean = false,
    val feedback: AnswerFeedback? = null,
    val isCompleting: Boolean = false,
    val result: ReviewResult? = null,
    /** The result was worked out on the device; the server gets it when the network is back. */
    val resultWaitingForNetwork: Boolean = false,
    val pendingAnswers: Int = 0,
    val isOnline: Boolean = true,
) {
    /** The question on screen: the one just answered while its feedback shows, else the next one. */
    val question: QuizQuestion?
        get() = feedback?.question ?: session?.currentWordId?.let { session.questions[it] }

    val questionWord: VocabularyWord? get() = question?.let { session?.words?.get(it.wordId) }

    /** 1-based number of the question on screen, counting re-queued cards. */
    val questionNumber: Int
        get() {
            val position = session?.position ?: 0
            return if (feedback != null) position else position + 1
        }

    val total: Int get() = session?.queue?.size ?: 0

    val progress: Float get() = if (total == 0) 0f else (session?.position ?: 0).toFloat() / total

    val wrongWords: List<VocabularyWord>
        get() = result?.wordIds.orEmpty().mapNotNull { session?.words?.get(it) }
}

private const val CODE_SESSION_EMPTY = "STUDY_SESSION_EMPTY"

@HiltViewModel
class ReviewSessionViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val repository: ReviewRepository,
        networkMonitor: NetworkMonitor,
    ) : ViewModel() {
        private val initialSource: ReviewSource =
            savedStateHandle
                .get<String>(ReviewSessionDestination.ARG_SOURCE)
                ?.let { name -> ReviewSource.entries.firstOrNull { it.name == name } }
                ?: ReviewSource.DUE

        private val _uiState = MutableStateFlow(ReviewSessionUiState(source = initialSource))
        val uiState: StateFlow<ReviewSessionUiState> = _uiState.asStateFlow()

        private var wordIds: List<String> = emptyList()

        init {
            viewModelScope.launch {
                repository.pendingAnswerCount.collect { count ->
                    _uiState.update { it.copy(pendingAnswers = count) }
                }
            }
            viewModelScope.launch {
                networkMonitor.isOnline.distinctUntilChanged().collect { online ->
                    _uiState.update { it.copy(isOnline = online) }
                    // The finished session is closed on the server as soon as it can be.
                    if (online && _uiState.value.resultWaitingForNetwork) complete()
                }
            }
            start()
        }

        fun onRetry() {
            if (_uiState.value.isLoading) return
            start()
        }

        fun onOptionSelect(optionWordId: String) {
            val state = _uiState.value
            val session = state.session ?: return
            if (state.feedback != null || state.isCompleting) return
            val question = state.question ?: return
            val feedback = AnswerFeedback(question = question, chosenWordId = optionWordId)
            // Quiz grading from the web: right on the first pick is Good, anything else Forget.
            val grade = if (feedback.isCorrect) ReviewGrade.GOOD else ReviewGrade.FORGET
            // Right or wrong shows at once; recording and sending happen behind it.
            _uiState.update {
                it.copy(
                    feedback = feedback,
                    session = session.answered(question.wordId, grade),
                )
            }
            viewModelScope.launch {
                val recorded = repository.answer(session, question.wordId, grade)
                // A slow send can return after the learner already answered the next card; keep
                // whichever copy knows more answers.
                _uiState.update { current ->
                    val shown = current.session
                    if (shown == null ||
                        shown.id != recorded.id ||
                        recorded.reviewCount >= shown.reviewCount
                    ) {
                        current.copy(session = recorded)
                    } else {
                        current
                    }
                }
            }
        }

        fun onContinue() {
            val state = _uiState.value
            if (state.feedback == null) return
            _uiState.update { it.copy(feedback = null) }
            if (state.session?.isFinished == true) complete()
        }

        /** "Ôn lại từ sai": a new session with only the words just answered wrong. */
        fun onReviewWrongWords() {
            val ids =
                _uiState.value.result
                    ?.wordIds
                    .orEmpty()
            if (ids.isEmpty()) return
            wordIds = ids
            _uiState.update {
                ReviewSessionUiState(
                    source = ReviewSource.WORDS,
                    isOnline = it.isOnline,
                    pendingAnswers = it.pendingAnswers,
                )
            }
            start()
        }

        private fun start() {
            val source = _uiState.value.source
            _uiState.update { it.copy(isLoading = true, error = null, isEmpty = false) }
            viewModelScope.launch {
                when (val result = repository.startSession(source, wordIds)) {
                    is AppResult.Success -> {
                        _uiState.update { it.copy(isLoading = false, session = result.data) }
                        // Every card was answered before the learner left: go straight to the result.
                        if (result.data.isFinished) complete()
                    }
                    is AppResult.Failure -> {
                        val error = result.error
                        val empty = error is AppError.Server && error.code == CODE_SESSION_EMPTY
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isEmpty = empty,
                                error = error.takeUnless { empty },
                            )
                        }
                    }
                }
            }
        }

        private fun complete() {
            val session = _uiState.value.session ?: return
            if (_uiState.value.isCompleting) return
            _uiState.update { it.copy(isCompleting = true) }
            viewModelScope.launch {
                when (val result = repository.completeSession(session)) {
                    is AppResult.Success ->
                        _uiState.update {
                            it.copy(
                                isCompleting = false,
                                result = result.data,
                                resultWaitingForNetwork = false,
                            )
                        }
                    is AppResult.Failure ->
                        _uiState.update {
                            it.copy(
                                isCompleting = false,
                                result = session.localResult,
                                resultWaitingForNetwork =
                                    result.error.isConnectivity ||
                                        result.error == AppError.Unauthorized,
                            )
                        }
                }
            }
        }
    }
