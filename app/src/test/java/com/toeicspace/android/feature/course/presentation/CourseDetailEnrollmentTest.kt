package com.toeicspace.android.feature.course.presentation

import androidx.lifecycle.SavedStateHandle
import com.toeicspace.android.core.analytics.AnalyticsEvent
import com.toeicspace.android.core.common.AppError
import com.toeicspace.android.core.common.AppResult
import com.toeicspace.android.core.model.AuthUser
import com.toeicspace.android.core.model.UserRole
import com.toeicspace.android.feature.course.domain.CourseDetail
import com.toeicspace.android.feature.course.domain.CourseLesson
import com.toeicspace.android.feature.course.domain.CourseModule
import com.toeicspace.android.feature.course.domain.CoursePrice
import com.toeicspace.android.feature.course.navigation.CourseDetailDestination
import com.toeicspace.android.feature.course.presentation.detail.CourseDetailDialog
import com.toeicspace.android.feature.course.presentation.detail.CourseDetailEvent
import com.toeicspace.android.feature.course.presentation.detail.CourseDetailMessage
import com.toeicspace.android.feature.course.presentation.detail.CourseDetailViewModel
import com.toeicspace.android.feature.course.presentation.detail.CoursePrimaryAction
import com.toeicspace.android.feature.course.presentation.detail.EnrollmentStatus
import com.toeicspace.android.feature.enrollment.domain.EnrollmentRepository
import com.toeicspace.android.testing.FakeAnalyticsHelper
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

@OptIn(ExperimentalCoroutinesApi::class)
class CourseDetailEnrollmentTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val courses = FakeCourseRepository()
    private val enrollments = FakeEnrollmentRepository()
    private val analytics = FakeAnalyticsHelper()
    private val user =
        AuthUser(id = "u1", fullName = "An", email = "an@example.com", role = UserRole.User)

    private fun lesson(
        id: String,
        preview: Boolean = false,
    ) = CourseLesson(id, "Lesson $id", null, null, isFreePreview = preview)

    private fun course(
        price: Long = 0,
        remainingSeats: Int? = null,
        modules: List<CourseModule> =
            listOf(
                CourseModule("m1", "M1", null, listOf(lesson("l1"), lesson("l2", preview = true))),
            ),
    ) = CourseDetail(
        summary =
            testCourse("c1").copy(
                price = CoursePrice(amount = price, currency = "VND"),
                capacity = remainingSeats?.let { 10 },
                remainingSeats = remainingSeats,
            ),
        description = null,
        skills = emptyList(),
        objectives = emptyList(),
        prerequisites = emptyList(),
        modules = modules,
    )

    private fun TestScope.started(
        session: FakeSessionRepository,
        detail: CourseDetail = course(),
    ): CourseDetailViewModel {
        courses.detail = AppResult.Success(detail)
        return CourseDetailViewModel(
            savedStateHandle =
                SavedStateHandle(
                    mapOf(CourseDetailDestination.ARG_COURSE_ID to "c1"),
                ),
            courseRepository = courses,
            enrollmentRepository = enrollments,
            sessionRepository = session,
            analytics = analytics,
        ).also { runCurrent() }
    }

    private fun signedIn() = FakeSessionRepository(isOnboardingCompleted = true, user = user)

    private fun guest() = FakeSessionRepository(isOnboardingCompleted = true, isGuest = true)

    @Test
    fun `a guest sees enroll and is sent to sign in, without asking the server`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = started(guest())

            assertEquals(CoursePrimaryAction.ENROLL, viewModel.uiState.value.primaryAction)
            viewModel.onPrimaryClick()

            assertEquals(CourseDetailEvent.SignInRequired, viewModel.uiState.value.event)
            assertTrue(enrollments.statusRequests.isEmpty())
            assertTrue(enrollments.enrollRequests.isEmpty())
        }

    @Test
    fun `a guest buying a paid course is sent to sign in too`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = started(guest(), course(price = 499_000))

            assertEquals(CoursePrimaryAction.BUY, viewModel.uiState.value.primaryAction)
            viewModel.onPrimaryClick()

            assertEquals(CourseDetailEvent.SignInRequired, viewModel.uiState.value.event)
        }

    @Test
    fun `signing in from the page checks the enrollment`() =
        runTest(mainDispatcherRule.testDispatcher) {
            enrollments.enrolled += "c1"
            val session = guest()
            val viewModel = started(session)

            session.signIn(user)
            runCurrent()

            assertEquals(listOf("c1"), enrollments.statusRequests)
            assertEquals(CoursePrimaryAction.START, viewModel.uiState.value.primaryAction)
        }

    @Test
    fun `an enrolled learner goes straight in, to the first lesson`() =
        runTest(mainDispatcherRule.testDispatcher) {
            enrollments.enrolled += "c1"
            val viewModel = started(signedIn())

            assertEquals(EnrollmentStatus.ENROLLED, viewModel.uiState.value.enrollment)
            viewModel.onPrimaryClick()

            assertEquals(
                CourseDetailEvent.OpenLesson("c1", "l1", preview = false),
                viewModel.uiState.value.event,
            )
            viewModel.onEventHandled()
            assertNull(viewModel.uiState.value.event)
        }

    @Test
    fun `the button waits while the enrollment is being checked`() =
        runTest(mainDispatcherRule.testDispatcher) {
            courses.detail = AppResult.Success(course())
            val viewModel =
                CourseDetailViewModel(
                    savedStateHandle =
                        SavedStateHandle(
                            mapOf(
                                CourseDetailDestination.ARG_COURSE_ID to "c1",
                            ),
                        ),
                    courseRepository = courses,
                    enrollmentRepository = enrollments,
                    sessionRepository = signedIn(),
                )

            assertEquals(CoursePrimaryAction.CHECKING, viewModel.uiState.value.primaryAction)
            runCurrent()
            assertEquals(CoursePrimaryAction.ENROLL, viewModel.uiState.value.primaryAction)
        }

    @Test
    fun `a failed check counts as not enrolled`() =
        runTest(mainDispatcherRule.testDispatcher) {
            enrollments.statusFailure = AppError.Network
            val viewModel = started(signedIn())

            assertEquals(EnrollmentStatus.NOT_ENROLLED, viewModel.uiState.value.enrollment)
        }

    @Test
    fun `enroll asks for confirmation before calling the server`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = started(signedIn())

            viewModel.onPrimaryClick()
            runCurrent()

            assertEquals(CourseDetailDialog.ConfirmEnroll, viewModel.uiState.value.dialog)
            assertTrue(enrollments.enrollRequests.isEmpty())

            viewModel.onDialogDismiss()
            assertNull(viewModel.uiState.value.dialog)
            assertTrue(enrollments.enrollRequests.isEmpty())
        }

    @Test
    fun `confirming enrolls in a free course and turns the button into start`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = started(signedIn())

            viewModel.onPrimaryClick()
            viewModel.onConfirmEnroll()
            assertTrue(viewModel.uiState.value.isEnrolling)
            // The summary can't be closed while the request runs.
            viewModel.onDialogDismiss()
            assertEquals(CourseDetailDialog.ConfirmEnroll, viewModel.uiState.value.dialog)
            runCurrent()

            val state = viewModel.uiState.value
            assertFalse(state.isEnrolling)
            assertNull(state.dialog)
            assertEquals(listOf("c1"), enrollments.enrollRequests)
            assertEquals(CoursePrimaryAction.START, state.primaryAction)
            assertEquals(CourseDetailMessage.Enrolled, state.message)
            viewModel.onMessageShown()
            assertNull(viewModel.uiState.value.message)
            assertEquals(
                listOf(AnalyticsEvent.enrollCourse("c1")),
                analytics.named(AnalyticsEvent.Names.ENROLL_COURSE),
            )
        }

    @Test
    fun `confirming without the dialog open does nothing`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = started(signedIn())

            viewModel.onConfirmEnroll()
            runCurrent()

            assertTrue(enrollments.enrollRequests.isEmpty())
        }

    @Test
    fun `a course that fills up meanwhile switches the dialog to sold out`() =
        runTest(mainDispatcherRule.testDispatcher) {
            enrollments.enrollFailure =
                AppError.Server(code = EnrollmentRepository.ERROR_COURSE_FULL, status = 409)
            val viewModel = started(signedIn())

            viewModel.onPrimaryClick()
            viewModel.onConfirmEnroll()
            runCurrent()

            val state = viewModel.uiState.value
            assertEquals(CoursePrimaryAction.SOLD_OUT, state.primaryAction)
            assertEquals(CourseDetailDialog.SoldOut, state.dialog)
            assertNull(state.message)
        }

    @Test
    fun `other enroll errors close the dialog and say what happened`() =
        runTest(mainDispatcherRule.testDispatcher) {
            enrollments.enrollFailure = AppError.Network
            val viewModel = started(signedIn())

            viewModel.onPrimaryClick()
            viewModel.onConfirmEnroll()
            runCurrent()

            val state = viewModel.uiState.value
            assertEquals(CoursePrimaryAction.ENROLL, state.primaryAction)
            assertNull(state.dialog)
            assertEquals(CourseDetailMessage.Failed(AppError.Network), state.message)
        }

    @Test
    fun `a paid course that is sold out explains why instead of opening checkout`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = started(signedIn(), course(price = 499_000, remainingSeats = 0))

            viewModel.onPrimaryClick()
            runCurrent()

            assertEquals(CourseDetailDialog.SoldOut, viewModel.uiState.value.dialog)
            assertNull(viewModel.uiState.value.event)
        }

    @Test
    fun `sold out cannot be enrolled, but an owner still goes in`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = started(signedIn(), course(remainingSeats = 0))

            assertEquals(CoursePrimaryAction.SOLD_OUT, viewModel.uiState.value.primaryAction)
            viewModel.onPrimaryClick()
            runCurrent()
            assertTrue(enrollments.enrollRequests.isEmpty())
            assertNull(viewModel.uiState.value.event)
            assertEquals(CourseDetailDialog.SoldOut, viewModel.uiState.value.dialog)

            enrollments.enrolled += "c1"
            val owner = started(signedIn(), course(remainingSeats = 0))
            assertEquals(CoursePrimaryAction.START, owner.uiState.value.primaryAction)
        }

    @Test
    fun `a paid course opens checkout for a signed-in learner`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = started(signedIn(), course(price = 499_000))

            viewModel.onPrimaryClick()

            assertEquals(CourseDetailEvent.OpenCheckout("c1"), viewModel.uiState.value.event)
            assertTrue(enrollments.enrollRequests.isEmpty())
        }

    @Test
    fun `coming back after buying rechecks the enrollment`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = started(signedIn(), course(price = 499_000))
            enrollments.enrolled += "c1"

            viewModel.onResume()
            runCurrent()

            assertEquals(CoursePrimaryAction.START, viewModel.uiState.value.primaryAction)
        }

    @Test
    fun `coming back reloads the seats quietly, and keeps the page if that fails`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = started(signedIn(), course(price = 499_000, remainingSeats = 5))
            courses.detail = AppResult.Success(course(price = 499_000, remainingSeats = 4))

            viewModel.onResume()
            assertFalse(viewModel.uiState.value.isLoading)
            runCurrent()
            assertEquals(
                4,
                viewModel.uiState.value.course
                    ?.summary
                    ?.remainingSeats,
            )

            courses.detail = AppResult.Failure(AppError.Network)
            viewModel.onResume()
            runCurrent()
            assertEquals(
                4,
                viewModel.uiState.value.course
                    ?.summary
                    ?.remainingSeats,
            )
            assertNull(viewModel.uiState.value.error)
        }

    @Test
    fun `before enrolling only free previews open`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = started(guest())

            viewModel.onLessonClick("l1")
            assertEquals(CourseDetailMessage.LessonLocked, viewModel.uiState.value.message)
            assertNull(viewModel.uiState.value.event)

            viewModel.onLessonClick("l2")
            assertEquals(
                CourseDetailEvent.OpenLesson("c1", "l2", preview = true),
                viewModel.uiState.value.event,
            )
        }

    @Test
    fun `an enrolled learner opens any lesson`() =
        runTest(mainDispatcherRule.testDispatcher) {
            enrollments.enrolled += "c1"
            val viewModel = started(signedIn())

            viewModel.onLessonClick("l1")

            assertEquals(
                CourseDetailEvent.OpenLesson("c1", "l1", preview = false),
                viewModel.uiState.value.event,
            )
        }

    @Test
    fun `a course without lessons says so instead of opening`() =
        runTest(mainDispatcherRule.testDispatcher) {
            enrollments.enrolled += "c1"
            val viewModel = started(signedIn(), course(modules = emptyList()))

            viewModel.onPrimaryClick()

            assertEquals(CourseDetailMessage.NoLessons, viewModel.uiState.value.message)
            assertNull(viewModel.uiState.value.event)
        }
}
