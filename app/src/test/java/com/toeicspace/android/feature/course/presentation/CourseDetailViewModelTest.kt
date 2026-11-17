package com.toeicspace.android.feature.course.presentation

import androidx.lifecycle.SavedStateHandle
import com.toeicspace.android.core.common.AppError
import com.toeicspace.android.core.common.AppResult
import com.toeicspace.android.feature.course.domain.CourseDetail
import com.toeicspace.android.feature.course.domain.CourseModule
import com.toeicspace.android.feature.course.navigation.CourseDetailDestination
import com.toeicspace.android.feature.course.presentation.detail.CourseDetailViewModel
import com.toeicspace.android.testing.FakeCourseRepository
import com.toeicspace.android.testing.MainDispatcherRule
import com.toeicspace.android.testing.testCourse
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
class CourseDetailViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val repository = FakeCourseRepository()

    private fun module(id: String) =
        CourseModule(id = id, title = "Module $id", description = null, lessons = emptyList())

    private val course =
        CourseDetail(
            summary = testCourse("c1"),
            description = null,
            skills = emptyList(),
            objectives = emptyList(),
            prerequisites = emptyList(),
            modules = listOf(module("m1"), module("m2")),
        )

    private fun TestScope.started(): CourseDetailViewModel =
        CourseDetailViewModel(
            savedStateHandle =
                SavedStateHandle(
                    mapOf(CourseDetailDestination.ARG_COURSE_ID to "c1"),
                ),
            courseRepository = repository,
        ).also { runCurrent() }

    @Test
    fun `starts loading before the course arrives`() =
        runTest(mainDispatcherRule.testDispatcher) {
            repository.detail = AppResult.Success(course)
            val viewModel =
                CourseDetailViewModel(
                    savedStateHandle =
                        SavedStateHandle(
                            mapOf(
                                CourseDetailDestination.ARG_COURSE_ID to "c1",
                            ),
                        ),
                    courseRepository = repository,
                )

            assertTrue(viewModel.uiState.value.isLoading)
        }

    @Test
    fun `loads the course from the route id and opens the first chapter`() =
        runTest(mainDispatcherRule.testDispatcher) {
            repository.detail = AppResult.Success(course)

            val state = started().uiState.value

            assertEquals(listOf("c1"), repository.detailRequests)
            assertFalse(state.isLoading)
            assertEquals(course, state.course)
            assertNull(state.error)
            assertEquals(setOf("m1"), state.expandedModuleIds)
        }

    @Test
    fun `a course without chapters opens nothing`() =
        runTest(mainDispatcherRule.testDispatcher) {
            repository.detail = AppResult.Success(course.copy(modules = emptyList()))

            assertTrue(
                started()
                    .uiState.value.expandedModuleIds
                    .isEmpty(),
            )
        }

    @Test
    fun `404 is reported as not found`() =
        runTest(mainDispatcherRule.testDispatcher) {
            repository.detail = AppResult.Failure(AppError.Server(code = null, status = 404))

            val state = started().uiState.value

            assertFalse(state.isLoading)
            assertNull(state.course)
            assertTrue(state.isNotFound)
        }

    @Test
    fun `a network failure is an error, not a missing course`() =
        runTest(mainDispatcherRule.testDispatcher) {
            repository.detail = AppResult.Failure(AppError.Network)

            val state = started().uiState.value

            assertEquals(AppError.Network, state.error)
            assertFalse(state.isNotFound)
        }

    @Test
    fun `retry after a failure loads the course`() =
        runTest(mainDispatcherRule.testDispatcher) {
            repository.detail = AppResult.Failure(AppError.Network)
            val viewModel = started()

            repository.detail = AppResult.Success(course)
            viewModel.onRetry()
            runCurrent()

            val state = viewModel.uiState.value
            assertEquals(2, repository.detailRequests.size)
            assertNull(state.error)
            assertEquals(course, state.course)
        }

    @Test
    fun `toggling a chapter opens and closes it`() =
        runTest(mainDispatcherRule.testDispatcher) {
            repository.detail = AppResult.Success(course)
            val viewModel = started()

            viewModel.onModuleToggle("m2")
            assertEquals(setOf("m1", "m2"), viewModel.uiState.value.expandedModuleIds)

            viewModel.onModuleToggle("m1")
            assertEquals(setOf("m2"), viewModel.uiState.value.expandedModuleIds)
        }
}
