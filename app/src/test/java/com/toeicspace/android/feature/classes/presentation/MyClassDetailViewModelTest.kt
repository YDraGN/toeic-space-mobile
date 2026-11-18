package com.toeicspace.android.feature.classes.presentation

import androidx.lifecycle.SavedStateHandle
import com.toeicspace.android.core.common.AppError
import com.toeicspace.android.core.common.AppResult
import com.toeicspace.android.feature.classes.navigation.MyClassDetailDestination
import com.toeicspace.android.feature.classes.presentation.detail.MyClassDetailViewModel
import com.toeicspace.android.testing.FakeStudentClassRepository
import com.toeicspace.android.testing.MainDispatcherRule
import com.toeicspace.android.testing.testClass
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MyClassDetailViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val repository = FakeStudentClassRepository()
    private val studentClass = testClass("c1")
    private val notFound = AppError.Server(code = "CLASS_NOT_FOUND", status = 404)

    private fun viewModel() =
        MyClassDetailViewModel(
            savedStateHandle =
                SavedStateHandle(
                    mapOf(MyClassDetailDestination.ARG_CLASS_ID to "c1"),
                ),
            repository = repository,
        )

    private fun TestScope.started(): MyClassDetailViewModel = viewModel().also { runCurrent() }

    @Test
    fun `loads the class from its id`() =
        runTest(mainDispatcherRule.testDispatcher) {
            repository.detail = AppResult.Success(studentClass)
            val viewModel = viewModel()
            assertTrue(viewModel.uiState.value.isLoading)

            runCurrent()

            assertEquals("c1", repository.lastDetailId)
            assertEquals(studentClass, viewModel.uiState.value.studentClass)
            assertFalse(viewModel.uiState.value.isLoading)
        }

    @Test
    fun `not enrolled any more is not found, with nothing to retry`() =
        runTest(mainDispatcherRule.testDispatcher) {
            repository.detail = AppResult.Failure(notFound)

            val state = started().uiState.value

            assertTrue(state.isNotFound)
            assertNull(state.studentClass)
            assertFalse(state.isStale)
        }

    @Test
    fun `a not found answer drops the class cached from the list`() =
        runTest(mainDispatcherRule.testDispatcher) {
            repository.cachedDetail = studentClass
            repository.detail = AppResult.Failure(notFound)

            val state = started().uiState.value

            assertNull(state.studentClass)
            assertTrue(state.isNotFound)
        }

    @Test
    fun `offline keeps the class loaded before`() =
        runTest(mainDispatcherRule.testDispatcher) {
            repository.cachedDetail = studentClass
            repository.detail = AppResult.Failure(AppError.Network)

            val state = started().uiState.value

            assertEquals(studentClass, state.studentClass)
            assertTrue(state.isStale)
            assertFalse(state.isRefreshing)
        }

    @Test
    fun `retry after an error loads again`() =
        runTest(mainDispatcherRule.testDispatcher) {
            repository.detail = AppResult.Failure(AppError.ServerUnreachable)
            val viewModel = started()

            repository.detail = AppResult.Success(studentClass)
            viewModel.onRetry()
            assertTrue(viewModel.uiState.value.isLoading)
            runCurrent()

            assertEquals(studentClass, viewModel.uiState.value.studentClass)
            assertNull(viewModel.uiState.value.error)
            assertEquals(2, repository.detailCalls)
        }

    @Test
    fun `pull to refresh updates the class`() =
        runTest(mainDispatcherRule.testDispatcher) {
            repository.detail = AppResult.Success(studentClass)
            val viewModel = started()

            val renamed = studentClass.copy(name = "Renamed")
            repository.detail = AppResult.Success(renamed)
            viewModel.onRefresh()
            assertTrue(viewModel.uiState.value.isRefreshing)
            runCurrent()

            assertEquals(
                "Renamed",
                viewModel.uiState.value.studentClass
                    ?.name,
            )
            assertFalse(viewModel.uiState.value.isRefreshing)
        }
}
