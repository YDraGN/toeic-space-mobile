package com.toeicspace.android.feature.vocabulary.presentation.flashcard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toeicspace.android.core.common.AppError
import com.toeicspace.android.core.common.AppResult
import com.toeicspace.android.core.data.repository.SessionRepository
import com.toeicspace.android.core.model.SessionState
import com.toeicspace.android.feature.vocabulary.domain.FlashcardMark
import com.toeicspace.android.feature.vocabulary.domain.FlashcardMarkRepository
import com.toeicspace.android.feature.vocabulary.domain.VocabularyRepository
import com.toeicspace.android.feature.vocabulary.domain.VocabularyWord
import com.toeicspace.android.feature.vocabulary.navigation.FlashcardDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FlashcardUiState(
    val isLoading: Boolean = true,
    val setTitle: String = "",
    /** Every word of the set. */
    val allWords: List<VocabularyWord> = emptyList(),
    /** The cards of this round: the whole set, or only the ones still being learned. */
    val words: List<VocabularyWord> = emptyList(),
    val index: Int = 0,
    /** The current card shows its back: meaning and example. */
    val isFlipped: Boolean = false,
    /** Every card of the round has been sorted; the screen shows the round's summary. */
    val isFinished: Boolean = false,
    /** The learner's marks for this set, keyed by word id, saved on the device. */
    val marks: Map<String, FlashcardMark> = emptyMap(),
    val isSignedIn: Boolean = false,
    val error: AppError? = null,
    val fromCache: Boolean = false,
) {
    val current: VocabularyWord? get() = words.getOrNull(index)

    /** The card after the current one, drawn underneath it as the next in the stack. */
    val upcoming: VocabularyWord? get() = words.getOrNull(index + 1)

    /** 1-based position for the "3/20" label. */
    val position: Int get() = if (words.isEmpty()) 0 else index + 1

    val progress: Float
        get() =
            when {
                words.isEmpty() -> 0f
                isFinished -> 1f
                else -> position.toFloat() / words.size
            }

    val hasPrevious: Boolean get() = isFinished || index > 0

    val hasNext: Boolean get() = index < words.lastIndex

    val isEmpty: Boolean get() = !isLoading && error == null && words.isEmpty()

    /** Cards of this round marked known. */
    val knownCount: Int get() = words.count { marks[it.id] == FlashcardMark.KNOWN }

    /** Cards of this round marked still learning. */
    val learningCount: Int get() = words.count { marks[it.id] == FlashcardMark.LEARNING }

    /** Words of the whole set still being learned, for "Học lại thẻ chưa thuộc". */
    val learningWords: List<VocabularyWord>
        get() = allWords.filter { marks[it.id] == FlashcardMark.LEARNING }
}

@HiltViewModel
class FlashcardViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val repository: VocabularyRepository,
        private val markRepository: FlashcardMarkRepository,
        sessionRepository: SessionRepository,
    ) : ViewModel() {
        private val setId: String =
            checkNotNull(savedStateHandle[FlashcardDestination.ARG_SET_ID]) {
                "FlashcardDestination needs a setId"
            }
        private val startIndex: Int = savedStateHandle[FlashcardDestination.ARG_START_INDEX] ?: 0

        private val _uiState = MutableStateFlow(FlashcardUiState())
        val uiState: StateFlow<FlashcardUiState> = _uiState.asStateFlow()

        init {
            viewModelScope.launch {
                sessionRepository.session
                    .filterIsInstance<SessionState.Ready>()
                    .map { it.isSignedIn }
                    .distinctUntilChanged()
                    .collect { signedIn -> _uiState.update { it.copy(isSignedIn = signedIn) } }
            }
            // Reviews queued while offline go out as soon as the learner opens cards again.
            viewModelScope.launch { markRepository.flushPending() }
            load()
        }

        fun onRetry() {
            if (_uiState.value.isLoading) return
            load()
        }

        fun onFlip() {
            _uiState.update {
                if (it.current == null || it.isFinished) it else it.copy(isFlipped = !it.isFlipped)
            }
        }

        /**
         * A swipe or a button sorted the current card: the mark is saved (and sent when signed in)
         * and the next card comes up, or the round's summary after the last one.
         */
        fun onMark(mark: FlashcardMark) {
            val state = _uiState.value
            val word = state.current ?: return
            if (state.isFinished) return
            _uiState.update {
                it.copy(
                    marks = it.marks + (word.id to mark),
                    index = if (it.hasNext) it.index + 1 else it.index,
                    isFinished = !it.hasNext,
                    isFlipped = false,
                )
            }
            viewModelScope.launch { markRepository.mark(setId, word.id, mark) }
        }

        /** Back one card; from the summary, back to the last card. */
        fun onPrevious() {
            _uiState.update {
                when {
                    it.isFinished -> it.copy(isFinished = false, isFlipped = false)
                    it.index > 0 -> it.copy(index = it.index - 1, isFlipped = false)
                    else -> it
                }
            }
        }

        /** A tap in the tablet word list; every new card starts on its front. */
        fun onCardSelect(index: Int) {
            _uiState.update { state ->
                if (index !in state.words.indices || (index == state.index && !state.isFinished)) {
                    state
                } else {
                    state.copy(index = index, isFlipped = false, isFinished = false)
                }
            }
        }

        /** The whole set again, from the first card. */
        fun onRestart() {
            _uiState.update {
                it.copy(words = it.allWords, index = 0, isFlipped = false, isFinished = false)
            }
        }

        /** Only the cards marked still learning, as a new round. */
        fun onStudyLearning() {
            _uiState.update {
                val learning = it.learningWords
                if (learning.isEmpty()) {
                    it
                } else {
                    it.copy(words = learning, index = 0, isFlipped = false, isFinished = false)
                }
            }
        }

        private fun load() {
            _uiState.update { it.copy(isLoading = true, error = null) }
            viewModelScope.launch {
                val marks = markRepository.getMarks(setId)
                when (val result = repository.getSet(setId)) {
                    is AppResult.Success -> {
                        val set = result.data.data
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                setTitle = set.title,
                                allWords = set.words,
                                words = set.words,
                                index =
                                    startIndex.coerceIn(
                                        0,
                                        (set.words.size - 1).coerceAtLeast(0),
                                    ),
                                isFlipped = false,
                                isFinished = false,
                                marks = marks,
                                fromCache = result.data.fromCache,
                            )
                        }
                    }
                    is AppResult.Failure ->
                        _uiState.update { it.copy(isLoading = false, error = result.error) }
                }
            }
        }
    }
