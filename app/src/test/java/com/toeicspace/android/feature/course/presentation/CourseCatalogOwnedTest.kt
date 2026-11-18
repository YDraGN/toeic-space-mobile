package com.toeicspace.android.feature.course.presentation

import com.toeicspace.android.core.common.AppError
import com.toeicspace.android.core.model.AuthUser
import com.toeicspace.android.core.model.UserRole
import com.toeicspace.android.feature.course.presentation.catalog.CATALOG_STALE_AFTER_MILLIS
import com.toeicspace.android.feature.course.presentation.catalog.CourseCatalogViewModel
import com.toeicspace.android.testing.FakeCourseRepository
import com.toeicspace.android.testing.FakeEnrollmentRepository
import com.toeicspace.android.testing.FakeSessionRepository
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

/** PBL-122: signed-in students don't see the courses they own in the catalog. */
@OptIn(ExperimentalCoroutinesApi::class)
class CourseCatalogOwnedTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val courses =
        FakeCourseRepository(courses = (1..6).map { testCourse("$it") }, pageSize = 20)
    private val enrollments = FakeEnrollmentRepository()
    private var now = 1_000_000L

    private fun user(role: UserRole) =
        AuthUser(id = "u1", fullName = "An", email = "an@example.com", role = role)

    private fun TestScope.started(session: FakeSessionRepository): CourseCatalogViewModel =
        CourseCatalogViewModel(courses, enrollments, session) { now }.also { runCurrent() }

    private val CourseCatalogViewModel.visibleIds get() = uiState.value.visibleCourses.map { it.id }

    @Test
    fun `guests see every course and the full count`() =
        runTest(mainDispatcherRule.testDispatcher) {
            enrollments.ownedIds = setOf("1", "2")
            val viewModel = started(FakeSessionRepository(isGuest = true))

            assertEquals(listOf("1", "2", "3", "4", "5", "6"), viewModel.visibleIds)
            assertEquals(6, viewModel.uiState.value.visibleCount)
            assertEquals(0, enrollments.ownedRequests)
        }

    @Test
    fun `students don't see owned courses and the count leaves them out`() =
        runTest(mainDispatcherRule.testDispatcher) {
            enrollments.ownedIds = setOf("2", "5")
            val viewModel = started(FakeSessionRepository(user = user(UserRole.User)))

            assertEquals(listOf("1", "3", "4", "6"), viewModel.visibleIds)
            assertEquals(4, viewModel.uiState.value.visibleCount)
            assertFalse(viewModel.uiState.value.isEmpty)
        }

    @Test
    fun `owning every course shows the empty state`() =
        runTest(mainDispatcherRule.testDispatcher) {
            enrollments.ownedIds = (1..6).map { "$it" }.toSet()
            val viewModel = started(FakeSessionRepository(user = user(UserRole.User)))

            assertTrue(viewModel.uiState.value.isEmpty)
            assertEquals(0, viewModel.uiState.value.visibleCount)
        }

    @Test
    fun `teachers see every course, as on the web`() =
        runTest(mainDispatcherRule.testDispatcher) {
            enrollments.ownedIds = setOf("1")
            val viewModel = started(FakeSessionRepository(user = user(UserRole.Teacher)))

            assertEquals(6, viewModel.visibleIds.size)
            assertEquals(0, enrollments.ownedRequests)
        }

    @Test
    fun `the catalog waits for the owned ids before showing courses`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel =
                CourseCatalogViewModel(
                    courses,
                    enrollments,
                    FakeSessionRepository(user = user(UserRole.User)),
                ) {
                    now
                }

            assertTrue(viewModel.uiState.value.isWaiting)
            runCurrent()
            assertFalse(viewModel.uiState.value.isWaiting)
        }

    @Test
    fun `a failed owned load shows an error and retry recovers`() =
        runTest(mainDispatcherRule.testDispatcher) {
            enrollments.ownedIds = setOf("1")
            enrollments.ownedFailure = AppError.Network
            val viewModel = started(FakeSessionRepository(user = user(UserRole.User)))

            assertEquals(AppError.Network, viewModel.uiState.value.ownedError)
            assertFalse(viewModel.uiState.value.isEmpty)

            enrollments.ownedFailure = null
            viewModel.onRetry()
            runCurrent()

            assertNull(viewModel.uiState.value.ownedError)
            assertEquals(listOf("2", "3", "4", "5", "6"), viewModel.visibleIds)
            // Only the owned ids were retried; the course page was fine.
            assertEquals(1, courses.requests.size)
        }

    @Test
    fun `a page made of owned courses loads the next one by itself`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val paged =
                FakeCourseRepository(courses = (1..6).map { testCourse("$it") }, pageSize = 2)
            enrollments.ownedIds = setOf("1", "2")
            val viewModel =
                CourseCatalogViewModel(
                    paged,
                    enrollments,
                    FakeSessionRepository(user = user(UserRole.User)),
                ) {
                    now
                }
            runCurrent()

            assertEquals(listOf("3", "4", "5", "6"), viewModel.visibleIds)
            assertTrue(paged.requests.size >= 2)
        }

    @Test
    fun `a course enrolled in meanwhile leaves the list when the tab comes back`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = started(FakeSessionRepository(user = user(UserRole.User)))
            assertEquals(6, viewModel.visibleIds.size)

            enrollments.enrolled += "3"
            now += CATALOG_STALE_AFTER_MILLIS
            viewModel.onResume()
            runCurrent()

            assertEquals(listOf("1", "2", "4", "5", "6"), viewModel.visibleIds)
            assertEquals(2, enrollments.ownedRequests)
        }

    @Test
    fun `signing out shows every course again`() =
        runTest(mainDispatcherRule.testDispatcher) {
            enrollments.ownedIds = setOf("1")
            val session = FakeSessionRepository(user = user(UserRole.User))
            val viewModel = started(session)
            assertEquals(5, viewModel.visibleIds.size)

            session.signOut()
            runCurrent()

            assertEquals(6, viewModel.visibleIds.size)
        }
}
