package com.toeicspace.android.feature.classes.presentation

import com.toeicspace.android.core.common.AppError
import com.toeicspace.android.core.common.AppResult
import com.toeicspace.android.feature.classes.presentation.list.MyClassesViewModel
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
class MyClassesViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val repository = FakeStudentClassRepository()
    private val classes = listOf(testClass("c1"), testClass("c2"))

    private fun TestScope.started(): MyClassesViewModel =
        MyClassesViewModel(repository).also { runCurrent() }

    @Test
    fun `starts loading with nothing to show`() =
        runTest(mainDispatcherRule.testDispatcher) {
            repository.list = AppResult.Success(classes)
            val viewModel = MyClassesViewModel(repository)

            assertTrue(viewModel.uiState.value.isLoading)
            assertNull(viewModel.uiState.value.classes)
        }

    @Test
    fun `shows the classes once loaded`() =
        runTest(mainDispatcherRule.testDispatcher) {
            repository.list = AppResult.Success(classes)

            val state = started().uiState.value

            assertFalse(state.isLoading)
            assertEquals(classes, state.classes)
            assertNull(state.error)
            assertFalse(state.isEmpty)
        }

    @Test
    fun `no classes is the empty state, not an error`() =
        runTest(mainDispatcherRule.testDispatcher) {
            repository.list = AppResult.Success(emptyList())

            val state = started().uiState.value

            assertTrue(state.isEmpty)
            assertNull(state.error)
        }

    @Test
    fun `a failed first load shows the error and retry loads again`() =
        runTest(mainDispatcherRule.testDispatcher) {
            repository.list = AppResult.Failure(AppError.ServerUnreachable)
            val viewModel = started()
            assertEquals(AppError.ServerUnreachable, viewModel.uiState.value.error)
            assertNull(viewModel.uiState.value.classes)

            repository.list = AppResult.Success(classes)
            viewModel.onRetry()
            assertTrue(viewModel.uiState.value.isLoading)
            runCurrent()

            assertEquals(classes, viewModel.uiState.value.classes)
            assertNull(viewModel.uiState.value.error)
            assertEquals(2, repository.listCalls)
        }

    @Test
    fun `offline shows the classes loaded before`() =
        runTest(mainDispatcherRule.testDispatcher) {
            repository.cachedList = classes
            repository.list = AppResult.Failure(AppError.Network)

            val state = started().uiState.value

            assertEquals(classes, state.classes)
            assertEquals(AppError.Network, state.error)
            assertTrue(state.isStale)
            assertFalse(state.isRefreshing)
        }

    @Test
    fun `the fresh list replaces the cached one`() =
        runTest(mainDispatcherRule.testDispatcher) {
            repository.cachedList = listOf(testClass("old"))
            repository.list = AppResult.Success(classes)

            val state = started().uiState.value

            assertEquals(classes, state.classes)
            assertFalse(state.isLoading)
            assertFalse(state.isRefreshing)
            assertFalse(state.isStale)
        }

    @Test
    fun `a failed refresh keeps the list and flags it stale`() =
        runTest(mainDispatcherRule.testDispatcher) {
            repository.list = AppResult.Success(classes)
            val viewModel = started()

            repository.list = AppResult.Failure(AppError.Network)
            viewModel.onRefresh()
            assertTrue(viewModel.uiState.value.isRefreshing)
            runCurrent()

            val state = viewModel.uiState.value
            assertFalse(state.isRefreshing)
            assertEquals(classes, state.classes)
            assertTrue(state.isStale)
        }

    @Test
    fun `refresh replaces the list and clears the error`() =
        runTest(mainDispatcherRule.testDispatcher) {
            repository.cachedList = classes
            repository.list = AppResult.Failure(AppError.Network)
            val viewModel = started()

            repository.list = AppResult.Success(listOf(testClass("c3")))
            viewModel.onRefresh()
            runCurrent()

            assertEquals(
                listOf("c3"),
                viewModel.uiState.value.classes
                    ?.map { it.id },
            )
            assertNull(viewModel.uiState.value.error)
        }

    @Test
    fun `refresh is ignored while a load is running`() =
        runTest(mainDispatcherRule.testDispatcher) {
            repository.list = AppResult.Success(classes)
            val viewModel = MyClassesViewModel(repository)

            viewModel.onRefresh()
            viewModel.onRetry()
            runCurrent()

            assertEquals(1, repository.listCalls)
        }
}
