package com.toeicspace.android.feature.vocabulary.presentation

import androidx.lifecycle.SavedStateHandle
import com.toeicspace.android.core.common.AppError
import com.toeicspace.android.core.common.AppResult
import com.toeicspace.android.core.model.AuthUser
import com.toeicspace.android.core.model.UserRole
import com.toeicspace.android.feature.vocabulary.domain.Fetched
import com.toeicspace.android.feature.vocabulary.domain.FlashcardMark
import com.toeicspace.android.feature.vocabulary.navigation.FlashcardDestination
import com.toeicspace.android.feature.vocabulary.navigation.VocabularySetDestination
import com.toeicspace.android.feature.vocabulary.presentation.flashcard.FlashcardViewModel
import com.toeicspace.android.feature.vocabulary.presentation.set.VocabularySetViewModel
import com.toeicspace.android.testing.FakeFlashcardMarkRepository
import com.toeicspace.android.testing.FakeSessionRepository
import com.toeicspace.android.testing.FakeVocabularyRepository
import com.toeicspace.android.testing.MainDispatcherRule
import com.toeicspace.android.testing.testSetDetail
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class FlashcardViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeVocabularyRepository()
    private val marks = FakeFlashcardMarkRepository()
    private val user =
        AuthUser(id = "u1", fullName = "Lan", email = "lan@example.com", role = UserRole.User)

    private fun flashcards(
        startIndex: Int? = null,
        signedIn: Boolean = false,
    ) = FlashcardViewModel(
        savedStateHandle =
            SavedStateHandle(
                buildMap {
                    put(FlashcardDestination.ARG_SET_ID, "set1")
                    startIndex?.let { put(FlashcardDestination.ARG_START_INDEX, it) }
                },
            ),
        repository = repository,
        markRepository = marks,
        sessionRepository =
            FakeSessionRepository(
                isOnboardingCompleted = true,
                user = if (signedIn) user else null,
                isGuest = !signedIn,
            ),
    )

    @Test
    fun `cards start on the front of the first card with the next one underneath`() {
        val state = flashcards().uiState.value

        assertFalse(state.isLoading)
        assertEquals("word-w1", state.current?.headword)
        assertEquals("word-w2", state.upcoming?.headword)
        assertFalse(state.isFlipped)
        assertEquals(1, state.position)
        assertEquals(1f / 3, state.progress, 0.0001f)
        assertFalse(state.hasPrevious)
        assertEquals(1, marks.flushCount)
    }

    @Test
    fun `marking a card saves it and brings the next card up front side`() {
        val viewModel = flashcards()
        viewModel.onFlip()
        assertTrue(viewModel.uiState.value.isFlipped)

        viewModel.onMark(FlashcardMark.KNOWN)

        val state = viewModel.uiState.value
        assertEquals(2, state.position)
        assertFalse(state.isFlipped)
        assertEquals(FlashcardMark.KNOWN, state.marks["w1"])
        assertEquals(listOf("w1" to FlashcardMark.KNOWN), marks.marked)
    }

    @Test
    fun `the last mark ends the round with its counts and previous goes back to it`() {
        val viewModel = flashcards()
        viewModel.onMark(FlashcardMark.KNOWN)
        viewModel.onMark(FlashcardMark.LEARNING)
        viewModel.onMark(FlashcardMark.LEARNING)

        var state = viewModel.uiState.value
        assertTrue(state.isFinished)
        assertEquals(1f, state.progress, 0.0001f)
        assertEquals(1, state.knownCount)
        assertEquals(2, state.learningCount)
        viewModel.onMark(FlashcardMark.KNOWN)
        assertEquals(3, marks.marked.size)

        viewModel.onPrevious()
        state = viewModel.uiState.value
        assertFalse(state.isFinished)
        assertEquals(3, state.position)
    }

    @Test
    fun `studying the cards still being learned starts a round of just those`() {
        val viewModel = flashcards()
        viewModel.onMark(FlashcardMark.KNOWN)
        viewModel.onMark(FlashcardMark.LEARNING)
        viewModel.onMark(FlashcardMark.LEARNING)

        viewModel.onStudyLearning()

        var state = viewModel.uiState.value
        assertFalse(state.isFinished)
        assertEquals(listOf("w2", "w3"), state.words.map { it.id })
        assertEquals(1, state.position)

        viewModel.onMark(FlashcardMark.KNOWN)
        viewModel.onMark(FlashcardMark.KNOWN)
        state = viewModel.uiState.value
        assertTrue(state.isFinished)
        assertTrue(state.learningWords.isEmpty())

        viewModel.onRestart()
        assertEquals(3, viewModel.uiState.value.words.size)
        assertEquals(1, viewModel.uiState.value.position)
    }

    @Test
    fun `marks saved earlier show again and previous stops at the first card`() {
        marks.marks["set1"] = mutableMapOf("w2" to FlashcardMark.LEARNING)
        val viewModel = flashcards(startIndex = 1)

        assertEquals(FlashcardMark.LEARNING, viewModel.uiState.value.marks["w2"])
        assertEquals(2, viewModel.uiState.value.position)
        viewModel.onPrevious()
        viewModel.onPrevious()
        assertEquals(1, viewModel.uiState.value.position)
    }

    @Test
    fun `a start index past the end opens the last card`() {
        assertEquals(3, flashcards(startIndex = 10).uiState.value.position)
    }

    @Test
    fun `picking a card in the word list turns it front up`() {
        val viewModel = flashcards()
        viewModel.onFlip()

        viewModel.onCardSelect(0)
        assertTrue(viewModel.uiState.value.isFlipped)

        viewModel.onCardSelect(2)
        assertFalse(viewModel.uiState.value.isFlipped)
        assertEquals(3, viewModel.uiState.value.position)
    }

    @Test
    fun `the screen knows whether marks reach the account`() {
        assertFalse(flashcards().uiState.value.isSignedIn)
        assertTrue(flashcards(signedIn = true).uiState.value.isSignedIn)
    }

    @Test
    fun `a set without words is empty and a failure can be retried`() {
        repository.set = AppResult.Success(Fetched(testSetDetail(wordCount = 0)))
        assertTrue(flashcards().uiState.value.isEmpty)

        repository.set = AppResult.Failure(AppError.Network)
        val viewModel = flashcards()
        assertEquals(AppError.Network, viewModel.uiState.value.error)
        viewModel.onFlip()
        assertFalse(viewModel.uiState.value.isFlipped)
        viewModel.onMark(FlashcardMark.KNOWN)
        assertTrue(marks.marked.isEmpty())

        repository.set = AppResult.Success(Fetched(testSetDetail(), fromCache = true))
        viewModel.onRetry()
        assertNull(viewModel.uiState.value.error)
        assertTrue(viewModel.uiState.value.fromCache)
    }

    @Test
    fun `set detail shows its words and reports a missing set as not found`() {
        val handle = SavedStateHandle(mapOf(VocabularySetDestination.ARG_SET_ID to "set1"))
        val loaded = VocabularySetViewModel(handle, repository).uiState.value
        assertEquals(3, loaded.set?.words?.size)

        repository.set =
            AppResult.Failure(AppError.Server(code = "VOCABULARY_SET_NOT_FOUND", status = 404))
        val missing = VocabularySetViewModel(handle, repository).uiState.value
        assertTrue(missing.isNotFound)
    }
}
