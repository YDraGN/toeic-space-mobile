package com.toeicspace.android.feature.course.presentation

import androidx.lifecycle.SavedStateHandle
import com.toeicspace.android.core.common.AppError
import com.toeicspace.android.core.model.AuthUser
import com.toeicspace.android.core.model.UserRole
import com.toeicspace.android.feature.course.navigation.CourseDetailDestination
import com.toeicspace.android.feature.course.presentation.detail.RelatedCoursesViewModel
import com.toeicspace.android.feature.course.presentation.detail.pickRelatedCourses
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
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** PBL-122: "Khoá học bạn có thể khám phá" under a course. */
@OptIn(ExperimentalCoroutinesApi::class)
class RelatedCoursesTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val catalog = (1..6).map { testCourse("$it") }
    private val courses = FakeCourseRepository(courses = catalog, pageSize = 20)
    private val enrollments = FakeEnrollmentRepository()
    private val student =
        AuthUser(id = "u1", fullName = "An", email = "an@example.com", role = UserRole.User)

    private fun TestScope.started(session: FakeSessionRepository): RelatedCoursesViewModel =
        RelatedCoursesViewModel(
            savedStateHandle =
                SavedStateHandle(
                    mapOf(CourseDetailDestination.ARG_COURSE_ID to "1"),
                ),
            courseRepository = courses,
            enrollmentRepository = enrollments,
            sessionRepository = session,
        ).also { runCurrent() }

    private val RelatedCoursesViewModel.ids get() = this.courses.value.map { it.id }

    @Test
    fun `leaves out the current and owned courses and keeps three`() {
        val picked = pickRelatedCourses(catalog, currentId = "2", ownedIds = setOf("1", "4"))

        assertEquals(listOf("3", "5", "6"), picked.map { it.id })
    }

    @Test
    fun `nothing left to suggest gives an empty list`() {
        val picked = pickRelatedCourses(catalog.take(2), currentId = "1", ownedIds = setOf("2"))

        assertTrue(picked.isEmpty())
    }

    @Test
    fun `guests get the first three other courses`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = started(FakeSessionRepository(isGuest = true))

            assertEquals(listOf("2", "3", "4"), viewModel.ids)
            assertEquals(0, enrollments.ownedRequests)
        }

    @Test
    fun `students don't get courses they own`() =
        runTest(mainDispatcherRule.testDispatcher) {
            enrollments.ownedIds = setOf("2", "3")
            val viewModel = started(FakeSessionRepository(user = student))

            assertEquals(listOf("4", "5", "6"), viewModel.ids)
        }

    @Test
    fun `a failed owned load hides the section`() =
        runTest(mainDispatcherRule.testDispatcher) {
            enrollments.ownedFailure = AppError.Network
            val viewModel = started(FakeSessionRepository(user = student))

            assertTrue(viewModel.ids.isEmpty())
        }

    @Test
    fun `a failed catalog load hides the section`() =
        runTest(mainDispatcherRule.testDispatcher) {
            courses.failure = AppError.ServerUnreachable
            val viewModel = started(FakeSessionRepository(isGuest = true))

            assertTrue(viewModel.ids.isEmpty())
        }
}
