package com.toeicspace.android.feature.vocabulary.presentation

import com.toeicspace.android.core.common.AppError
import com.toeicspace.android.core.common.AppResult
import com.toeicspace.android.core.model.AuthUser
import com.toeicspace.android.core.model.UserRole
import com.toeicspace.android.feature.vocabulary.domain.Fetched
import com.toeicspace.android.feature.vocabulary.domain.StageProgress
import com.toeicspace.android.feature.vocabulary.presentation.home.VocabularyHomeViewModel
import com.toeicspace.android.testing.FakeSessionRepository
import com.toeicspace.android.testing.FakeVocabularyRepository
import com.toeicspace.android.testing.MainDispatcherRule
import com.toeicspace.android.testing.testStage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class VocabularyHomeViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeVocabularyRepository()
    private val user =
        AuthUser(id = "u1", fullName = "Lan", email = "lan@example.com", role = UserRole.User)

    private fun viewModel(signedIn: Boolean = false) =
        VocabularyHomeViewModel(
            repository = repository,
            sessionRepository =
                FakeSessionRepository(
                    isOnboardingCompleted = true,
                    user = if (signedIn) user else null,
                    isGuest = !signedIn,
                ),
        )

    @Test
    fun `a guest sees the first stage and its sets without progress`() {
        val state = viewModel().uiState.value

        assertFalse(state.isLoading)
        assertFalse(state.isSignedIn)
        assertEquals("s1", state.selectedStageId)
        assertEquals(listOf("set1", "set2"), state.selectedSets?.sets?.map { it.id })
        assertTrue(state.progress.isEmpty())
        assertTrue(repository.progressRequests.isEmpty())
    }

    @Test
    fun `a signed-in learner also gets the progress of each set`() {
        val state = viewModel(signedIn = true).uiState.value

        assertTrue(state.isSignedIn)
        assertEquals(15, state.progress.getValue("set1").learned)
        assertEquals(listOf("s1"), repository.progressRequests)
    }

    @Test
    fun `a signed-in learner gets the progress of every stage, a guest none`() {
        val guest = viewModel().uiState.value
        assertTrue(guest.stageProgress.isEmpty())
        assertEquals(0, repository.stagesProgressRequests)

        val learner = viewModel(signedIn = true).uiState.value
        val s1 = learner.stageProgress.getValue("s1")
        assertEquals(1, s1.setsCompleted)
        assertEquals(0.5f, s1.fraction, 0.0001f)
        assertEquals(1, repository.stagesProgressRequests)
    }

    @Test
    fun `stage progress reloads on refresh and a failure just leaves no bars`() {
        repository.stagesProgress = AppResult.Failure(AppError.Network)
        val viewModel = viewModel(signedIn = true)
        assertTrue(
            viewModel.uiState.value.stageProgress
                .isEmpty(),
        )
        assertNull(viewModel.uiState.value.error)

        repository.stagesProgress =
            AppResult.Success(mapOf("s2" to StageProgress("s2", 80, 4, 0, 2, 0)))
        viewModel.onRefresh()

        assertEquals(setOf("s2"), viewModel.uiState.value.stageProgress.keys)
        assertEquals(2, repository.stagesProgressRequests)
    }

    @Test
    fun `choosing a stage loads its sets once`() {
        val viewModel = viewModel()

        viewModel.onStageSelect("s2")
        viewModel.onStageSelect("s1")
        viewModel.onStageSelect("s2")

        assertEquals(
            listOf("set3"),
            viewModel.uiState.value.selectedSets
                ?.sets
                ?.map { it.id },
        )
        assertEquals(listOf("s1", "s2"), repository.stageSetRequests)
    }

    @Test
    fun `a failed stage list shows the error and retry loads it again`() {
        repository.stages = AppResult.Failure(AppError.Network)
        val viewModel = viewModel()
        assertEquals(AppError.Network, viewModel.uiState.value.error)

        repository.stages = AppResult.Success(Fetched(listOf(testStage("s1"))))
        viewModel.onRetry()

        val state = viewModel.uiState.value
        assertNull(state.error)
        assertEquals(1, state.stages.size)
        assertEquals(2, repository.stageRequests)
    }

    @Test
    fun `failed sets keep the stages and can be retried on their own`() {
        repository.stageSets["s1"] = AppResult.Failure(AppError.ServerUnreachable)
        val viewModel = viewModel()
        assertEquals(
            AppError.ServerUnreachable,
            viewModel.uiState.value.selectedSets
                ?.error,
        )
        assertEquals(2, viewModel.uiState.value.stages.size)

        repository.stageSets.remove("s1")
        repository.stageSets["s1"] = AppResult.Success(Fetched(emptyList()))
        viewModel.onRetrySets()

        val sets = viewModel.uiState.value.selectedSets
        assertNull(sets?.error)
        assertTrue(sets?.sets?.isEmpty() == true)
    }

    @Test
    fun `data read from the device marks the page offline`() {
        repository.stages = AppResult.Success(Fetched(listOf(testStage("s1")), fromCache = true))

        assertTrue(viewModel().uiState.value.isOffline)
    }

    @Test
    fun `a failed refresh keeps the roadmap on screen`() {
        val viewModel = viewModel()
        repository.stages = AppResult.Failure(AppError.Network)

        viewModel.onRefresh()

        val state = viewModel.uiState.value
        assertFalse(state.isRefreshing)
        assertNull(state.error)
        assertEquals(2, state.stages.size)
    }
}
