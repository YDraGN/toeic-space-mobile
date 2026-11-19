package com.toeicspace.android.feature.vocabulary.presentation

import androidx.lifecycle.SavedStateHandle
import com.toeicspace.android.core.common.AppError
import com.toeicspace.android.core.common.AppResult
import com.toeicspace.android.core.model.AuthUser
import com.toeicspace.android.core.model.UserRole
import com.toeicspace.android.feature.vocabulary.domain.ReviewGrade
import com.toeicspace.android.feature.vocabulary.domain.ReviewResult
import com.toeicspace.android.feature.vocabulary.domain.ReviewSource
import com.toeicspace.android.feature.vocabulary.navigation.ReviewSessionDestination
import com.toeicspace.android.feature.vocabulary.presentation.review.ReviewSessionViewModel
import com.toeicspace.android.feature.vocabulary.presentation.review.ReviewSummaryViewModel
import com.toeicspace.android.testing.FakeNetworkMonitor
import com.toeicspace.android.testing.FakeReviewRepository
import com.toeicspace.android.testing.FakeSessionRepository
import com.toeicspace.android.testing.MainDispatcherRule
import com.toeicspace.android.testing.testReviewSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ReviewSessionViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeReviewRepository()
    private val network = FakeNetworkMonitor()

    private fun viewModel(source: ReviewSource = ReviewSource.DUE) =
        ReviewSessionViewModel(
            savedStateHandle =
                SavedStateHandle(
                    mapOf(
                        ReviewSessionDestination.ARG_SOURCE to source.name,
                    ),
                ),
            repository = repository,
            networkMonitor = network,
        )

    @Test
    fun `the first question of the session is shown`() {
        val state = viewModel().uiState.value

        assertFalse(state.isLoading)
        assertEquals("w1", state.question?.wordId)
        assertEquals(1, state.questionNumber)
        assertEquals(2, state.total)
        assertEquals(listOf(ReviewSource.DUE to emptyList<String>()), repository.starts)
    }

    @Test
    fun `a right answer shows at once and is recorded as Good`() {
        val viewModel = viewModel()

        viewModel.onOptionSelect("w1")

        val state = viewModel.uiState.value
        assertTrue(state.feedback?.isCorrect == true)
        assertEquals("w1", state.question?.wordId)
        assertEquals(1, state.questionNumber)
        assertEquals(listOf("w1" to ReviewGrade.GOOD), repository.answers)
    }

    @Test
    fun `a wrong answer is Forget, a second tap is ignored and the card comes back`() {
        val viewModel = viewModel()

        viewModel.onOptionSelect("decoy")
        viewModel.onOptionSelect("w1")

        assertFalse(
            viewModel.uiState.value.feedback!!
                .isCorrect,
        )
        assertEquals(listOf("w1" to ReviewGrade.FORGET), repository.answers)

        viewModel.onContinue()
        val state = viewModel.uiState.value
        assertNull(state.feedback)
        assertEquals("w2", state.question?.wordId)
        assertEquals(listOf("w1", "w2", "w1"), state.session?.queue)
    }

    @Test
    fun `after the last card the session is completed with the score and wrong words`() {
        val viewModel = viewModel()
        viewModel.onOptionSelect("w1")
        viewModel.onContinue()
        viewModel.onOptionSelect("decoy")
        viewModel.onContinue()
        viewModel.onOptionSelect("decoy")
        viewModel.onContinue()

        val state = viewModel.uiState.value
        assertEquals(
            ReviewResult(remembered = 1, needReview = 1, wordIds = listOf("w2")),
            state.result,
        )
        assertEquals(listOf("word-w2"), state.wrongWords.map { it.headword })
        assertFalse(state.resultWaitingForNetwork)
        assertEquals(1, repository.completeRequests)
    }

    @Test
    fun `offline at the end shows the result and completes once the network is back`() {
        network.setOnline(false)
        repository.complete = AppResult.Failure(AppError.Network)
        repository.start = AppResult.Success(testReviewSession("w1"))
        val viewModel = viewModel()
        // A wrong card comes back once, so it is answered twice before the session ends.
        repeat(2) {
            viewModel.onOptionSelect("decoy")
            viewModel.onContinue()
        }

        val waiting = viewModel.uiState.value
        assertTrue(waiting.resultWaitingForNetwork)
        assertEquals(listOf("w1"), waiting.result?.wordIds)

        repository.complete = null
        network.setOnline(true)

        assertFalse(viewModel.uiState.value.resultWaitingForNetwork)
        assertEquals(2, repository.completeRequests)
    }

    @Test
    fun `review wrong words starts a word-list session with only those words`() {
        repository.start = AppResult.Success(testReviewSession("w1"))
        val viewModel = viewModel()
        // A wrong card comes back once, so it is answered twice before the session ends.
        repeat(2) {
            viewModel.onOptionSelect("decoy")
            viewModel.onContinue()
        }

        viewModel.onReviewWrongWords()

        assertEquals(ReviewSource.WORDS to listOf("w1"), repository.starts.last())
        val state = viewModel.uiState.value
        assertEquals(ReviewSource.WORDS, state.source)
        assertNull(state.result)
        assertNotNull(state.question)
    }

    @Test
    fun `nothing to review is an empty state and other failures can be retried`() {
        repository.start =
            AppResult.Failure(AppError.Server(code = "STUDY_SESSION_EMPTY", status = 400))
        assertTrue(viewModel(ReviewSource.MISTAKES).uiState.value.isEmpty)

        repository.start = AppResult.Failure(AppError.ServerUnreachable)
        val viewModel = viewModel()
        assertEquals(AppError.ServerUnreachable, viewModel.uiState.value.error)

        repository.start = AppResult.Success(testReviewSession("w1"))
        viewModel.onRetry()
        assertNull(viewModel.uiState.value.error)
        assertEquals(
            "w1",
            viewModel.uiState.value.question
                ?.wordId,
        )
    }

    @Test
    fun `a session already fully answered goes straight to its result`() {
        repository.start =
            AppResult.Success(testReviewSession("w1").answered("w1", ReviewGrade.GOOD))

        val state = viewModel().uiState.value

        assertEquals(1, state.result?.remembered)
        assertEquals(1, repository.completeRequests)
    }

    @Test
    fun `the review card shows the due count to members and nothing to load for guests`() {
        val user =
            AuthUser(id = "u1", fullName = "Lan", email = "lan@example.com", role = UserRole.User)
        val member =
            ReviewSummaryViewModel(
                repository,
                FakeSessionRepository(isOnboardingCompleted = true, user = user),
            )
        assertEquals(true, member.uiState.value.isSignedIn)
        assertEquals(
            5,
            member.uiState.value.summary
                ?.due,
        )

        member.onRefresh()
        assertEquals(2, repository.summaryRequests)

        repository.pendingAnswerCount.value = 3
        assertEquals(3, member.uiState.value.pendingAnswers)

        val guest =
            ReviewSummaryViewModel(
                repository,
                FakeSessionRepository(isOnboardingCompleted = true, isGuest = true),
            )
        assertEquals(false, guest.uiState.value.isSignedIn)
        guest.onRefresh()
        assertNull(guest.uiState.value.summary)
        assertEquals(2, repository.summaryRequests)
    }
}
