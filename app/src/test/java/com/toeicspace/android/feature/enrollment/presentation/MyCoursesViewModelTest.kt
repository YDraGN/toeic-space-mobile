package com.toeicspace.android.feature.enrollment.presentation

import com.toeicspace.android.core.common.AppError
import com.toeicspace.android.testing.FakeEnrollmentRepository
import com.toeicspace.android.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MyCoursesViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val repository = FakeEnrollmentRepository()
    private var now = 1_000_000L

    private fun TestScope.started(): MyCoursesViewModel =
        MyCoursesViewModel(repository, clock = { now }).also { runCurrent() }

    @Test
    fun `loads the first page`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = started()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertEquals(listOf("1", "2"), state.courses.map { it.courseId })
            assertEquals(5, state.totalCount)
            assertFalse(state.endReached)
            assertEquals(listOf("" to 1), repository.myCoursesRequests)
        }

    @Test
    fun `loads more pages until the end`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = started()

            viewModel.onLoadMore()
            runCurrent()
            viewModel.onLoadMore()
            runCurrent()
            viewModel.onLoadMore()
            runCurrent()

            val state = viewModel.uiState.value
            assertEquals(5, state.courses.size)
            assertTrue(state.endReached)
            assertEquals(listOf(1, 2, 3), repository.myCoursesRequests.map { it.second })
        }

    @Test
    fun `search waits for the debounce and restarts at page one`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = started()

            viewModel.onSearchQueryChange("toe")
            viewModel.onSearchQueryChange("toeic ")
            advanceTimeBy(MY_COURSES_SEARCH_DEBOUNCE_MILLIS - 1)
            runCurrent()
            assertEquals(1, repository.myCoursesRequests.size)

            advanceTimeBy(2)
            runCurrent()

            assertEquals("toeic" to 1, repository.myCoursesRequests.last())
            assertEquals(2, repository.myCoursesRequests.size)
            assertTrue(viewModel.uiState.value.isSearching)
        }

    @Test
    fun `clearing the search shows every course again`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = started()
            viewModel.onSearchQueryChange("x")
            viewModel.onSearchSubmit()
            runCurrent()

            viewModel.onClearSearch()
            runCurrent()

            assertEquals("", viewModel.uiState.value.searchQuery)
            assertEquals("" to 1, repository.myCoursesRequests.last())
        }

    @Test
    fun `an empty list is empty, not an error`() =
        runTest(mainDispatcherRule.testDispatcher) {
            repository.myCourses = emptyList()
            val viewModel = started()

            assertTrue(viewModel.uiState.value.isEmpty)
            assertNull(viewModel.uiState.value.error)
        }

    @Test
    fun `a failed first page shows an error, and retry loads it`() =
        runTest(mainDispatcherRule.testDispatcher) {
            repository.myCoursesFailure = AppError.Network
            val viewModel = started()
            assertEquals(AppError.Network, viewModel.uiState.value.error)

            repository.myCoursesFailure = null
            viewModel.onRetry()
            runCurrent()

            assertNull(viewModel.uiState.value.error)
            assertEquals(2, viewModel.uiState.value.courses.size)
        }

    @Test
    fun `a failed next page keeps the list and offers retry`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = started()
            repository.myCoursesFailure = AppError.ServerUnreachable

            viewModel.onLoadMore()
            runCurrent()
            assertEquals(AppError.ServerUnreachable, viewModel.uiState.value.loadMoreError)
            assertEquals(2, viewModel.uiState.value.courses.size)

            repository.myCoursesFailure = null
            viewModel.onRetryLoadMore()
            runCurrent()
            assertNull(viewModel.uiState.value.loadMoreError)
            assertEquals(4, viewModel.uiState.value.courses.size)
        }

    @Test
    fun `a cover failing on a fresh list does not refetch`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = started()

            now += COVER_REFRESH_MIN_AGE_MILLIS - 1
            viewModel.onCoverError()
            runCurrent()

            assertEquals(1, repository.myCoursesRequests.size)
        }

    @Test
    fun `a cover failing on an old list refetches the links once`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = started()

            now += COVER_REFRESH_MIN_AGE_MILLIS
            viewModel.onCoverError()
            viewModel.onCoverError()
            runCurrent()

            assertEquals(2, repository.myCoursesRequests.size)
            assertFalse(viewModel.uiState.value.isRefreshing)
            assertEquals(2, viewModel.uiState.value.courses.size)
        }

    @Test
    fun `a failed background refetch changes nothing on screen`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = started()
            val before = viewModel.uiState.value
            repository.myCoursesFailure = AppError.Network

            now += COVER_REFRESH_MIN_AGE_MILLIS
            viewModel.onCoverError()
            runCurrent()

            assertEquals(before, viewModel.uiState.value)
        }
}
