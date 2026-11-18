package com.toeicspace.android.feature.course.presentation

import com.toeicspace.android.core.common.AppError
import com.toeicspace.android.feature.course.domain.CourseExamType
import com.toeicspace.android.feature.course.domain.CourseFilter
import com.toeicspace.android.feature.course.domain.CourseLevel
import com.toeicspace.android.feature.course.presentation.catalog.CATALOG_STALE_AFTER_MILLIS
import com.toeicspace.android.feature.course.presentation.catalog.CourseCatalogViewModel
import com.toeicspace.android.feature.course.presentation.catalog.SEARCH_DEBOUNCE_MILLIS
import com.toeicspace.android.testing.FakeCourseRepository
import com.toeicspace.android.testing.MainDispatcherRule
import com.toeicspace.android.testing.testCourse
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
class CourseCatalogViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val repository = FakeCourseRepository()

    private var now = 1_000_000L

    private fun TestScope.started(): CourseCatalogViewModel =
        CourseCatalogViewModel(repository) { now }.also {
            runCurrent()
        }

    private val CourseCatalogViewModel.ids get() = uiState.value.courses.map { it.id }

    @Test
    fun `coming back after a while reloads quietly and shows new courses`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = started()
            repository.courses = listOf(testCourse("new")) + repository.courses

            now += CATALOG_STALE_AFTER_MILLIS
            viewModel.onResume()
            assertFalse(viewModel.uiState.value.isRefreshing)
            runCurrent()

            assertEquals(listOf("new", "1"), viewModel.ids)
            assertEquals(2, repository.requests.size)
        }

    @Test
    fun `coming back right away does not reload`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = started()

            now += CATALOG_STALE_AFTER_MILLIS - 1
            viewModel.onResume()
            runCurrent()

            assertEquals(1, repository.requests.size)
        }

    @Test
    fun `first page loads on start`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = started()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertEquals(listOf("1", "2"), viewModel.ids)
            assertEquals(5, state.totalCount)
            assertFalse(state.endReached)
        }

    @Test
    fun `more pages are appended until the end`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = started()

            viewModel.onLoadMore()
            runCurrent()
            viewModel.onLoadMore()
            runCurrent()

            assertEquals(listOf("1", "2", "3", "4", "5"), viewModel.ids)
            assertTrue(viewModel.uiState.value.endReached)
            viewModel.onLoadMore()
            runCurrent()
            assertEquals(3, repository.requests.size)
        }

    @Test
    fun `a course repeated on the next page is shown once`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = started()
            // A course published after page 1 pushes course 2 onto page 2.
            repository.courses = listOf(testCourse("new")) + repository.courses

            viewModel.onLoadMore()
            runCurrent()

            assertEquals(listOf("1", "2", "3"), viewModel.ids)
        }

    @Test
    fun `search waits for typing to pause and trims the query`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = started()

            viewModel.onSearchQueryChange("list")
            advanceTimeBy(SEARCH_DEBOUNCE_MILLIS - 1)
            runCurrent()
            viewModel.onSearchQueryChange("listening ")
            advanceTimeBy(SEARCH_DEBOUNCE_MILLIS)
            runCurrent()

            assertEquals(2, repository.requests.size)
            assertEquals(CourseFilter(search = "listening") to 1, repository.requests.last())
            assertEquals("listening ", viewModel.uiState.value.searchQuery)
        }

    @Test
    fun `search key searches without waiting`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = started()

            viewModel.onSearchQueryChange("reading")
            viewModel.onSearchSubmit()
            runCurrent()
            advanceTimeBy(SEARCH_DEBOUNCE_MILLIS)
            runCurrent()

            assertEquals(
                listOf(CourseFilter(search = "reading")),
                repository.requests.drop(1).map { it.first },
            )
        }

    @Test
    fun `a filter change reloads from the first page and counts sheet filters`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = started()
            viewModel.onLoadMore()
            runCurrent()

            viewModel.onExamTypeSelect(CourseExamType.SPEAKING_WRITING)
            viewModel.onLevelSelect(CourseLevel.BEGINNER)
            viewModel.onTargetScoreSelect(650)
            runCurrent()

            val expected =
                CourseFilter(
                    examType = CourseExamType.SPEAKING_WRITING,
                    level = CourseLevel.BEGINNER,
                    targetScore = 650,
                )
            assertEquals(expected to 1, repository.requests.last())
            assertEquals(listOf("1", "2"), viewModel.ids)
            assertEquals(2, viewModel.uiState.value.sheetFilterCount)
        }

    @Test
    fun `selecting the same filter again sends nothing`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = started()

            viewModel.onExamTypeSelect(null)
            runCurrent()

            assertEquals(1, repository.requests.size)
        }

    @Test
    fun `clear filters resets the search box too`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = started()
            viewModel.onSearchQueryChange("ielts")
            viewModel.onSearchSubmit()
            viewModel.onLevelSelect(CourseLevel.ADVANCED)
            runCurrent()

            viewModel.onClearFilters()
            runCurrent()

            val state = viewModel.uiState.value
            assertEquals("", state.searchQuery)
            assertEquals(CourseFilter(), state.filter)
            assertFalse(state.hasActiveFilter)
        }

    @Test
    fun `first page failure shows an error that retry clears`() =
        runTest(mainDispatcherRule.testDispatcher) {
            repository.failure = AppError.Network
            val viewModel = started()
            assertEquals(AppError.Network, viewModel.uiState.value.error)

            repository.failure = null
            viewModel.onRetry()
            runCurrent()

            assertNull(viewModel.uiState.value.error)
            assertEquals(listOf("1", "2"), viewModel.ids)
        }

    @Test
    fun `a failed next page keeps the list and can be retried`() =
        runTest(mainDispatcherRule.testDispatcher) {
            repository.failOnPage = 2
            val viewModel = started()

            viewModel.onLoadMore()
            runCurrent()
            assertEquals(AppError.Network, viewModel.uiState.value.loadMoreError)
            assertEquals(listOf("1", "2"), viewModel.ids)
            // No automatic retry while the error is showing.
            viewModel.onLoadMore()
            runCurrent()
            assertEquals(2, repository.requests.size)

            repository.failOnPage = null
            viewModel.onRetryLoadMore()
            runCurrent()

            assertNull(viewModel.uiState.value.loadMoreError)
            assertEquals(listOf("1", "2", "3", "4"), viewModel.ids)
        }

    @Test
    fun `a failed refresh keeps the courses on screen`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = started()
            repository.failure = AppError.ServerUnreachable

            viewModel.onRefresh()
            runCurrent()

            val state = viewModel.uiState.value
            assertFalse(state.isRefreshing)
            assertNull(state.error)
            assertEquals(listOf("1", "2"), viewModel.ids)
        }

    @Test
    fun `an empty result is reported as empty`() =
        runTest(mainDispatcherRule.testDispatcher) {
            repository.courses = emptyList()
            val viewModel = started()

            assertTrue(viewModel.uiState.value.isEmpty)
            assertTrue(viewModel.uiState.value.endReached)
        }
}
