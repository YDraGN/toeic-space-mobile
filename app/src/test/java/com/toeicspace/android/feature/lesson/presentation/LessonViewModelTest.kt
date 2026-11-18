package com.toeicspace.android.feature.lesson.presentation

import androidx.lifecycle.SavedStateHandle
import com.toeicspace.android.core.common.AppError
import com.toeicspace.android.core.common.AppResult
import com.toeicspace.android.feature.course.domain.CourseDetail
import com.toeicspace.android.feature.course.domain.CourseLesson
import com.toeicspace.android.feature.course.domain.CourseModule
import com.toeicspace.android.feature.lesson.navigation.LessonDestination
import com.toeicspace.android.testing.FakeCourseRepository
import com.toeicspace.android.testing.FakeLessonRepository
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
class LessonViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val courses = FakeCourseRepository()
    private val lessons = FakeLessonRepository()
    private var now = 10_000_000L

    private fun lesson(
        id: String,
        preview: Boolean = false,
    ) = CourseLesson(id, "Lesson $id", null, null, isFreePreview = preview)

    // Curriculum order: l1 (preview), l2 | l3 (preview), l4.
    private val course =
        CourseDetail(
            summary = testCourse("c1"),
            description = null,
            skills = emptyList(),
            objectives = emptyList(),
            prerequisites = emptyList(),
            modules =
                listOf(
                    CourseModule(
                        "m1",
                        "M1",
                        null,
                        listOf(lesson("l1", preview = true), lesson("l2")),
                    ),
                    CourseModule(
                        "m2",
                        "M2",
                        null,
                        listOf(lesson("l3", preview = true), lesson("l4")),
                    ),
                ),
        )

    private lateinit var handle: SavedStateHandle

    private fun TestScope.started(
        lessonId: String,
        preview: Boolean = false,
    ): LessonViewModel {
        courses.detail = AppResult.Success(course)
        handle =
            SavedStateHandle(
                mapOf(
                    LessonDestination.ARG_COURSE_ID to "c1",
                    LessonDestination.ARG_LESSON_ID to lessonId,
                    LessonDestination.ARG_PREVIEW to preview,
                ),
            )
        return LessonViewModel(handle, courses, lessons, clock = { now }).also { runCurrent() }
    }

    @Test
    fun `loads the course outline and the lesson`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = started("l2")

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertEquals("Course c1", state.courseTitle)
            assertEquals(listOf("l1", "l2", "l3", "l4"), state.outline.map { it.id })
            assertEquals("l2", state.content?.id)
            assertEquals(listOf(Triple("c1", "l2", false)), lessons.requests)
        }

    @Test
    fun `an enrolled learner moves through every lesson in order`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = started("l2")

            assertEquals(
                "l1",
                viewModel.uiState.value.previous
                    ?.id,
            )
            assertEquals(
                "l3",
                viewModel.uiState.value.next
                    ?.id,
            )

            viewModel.onLessonSelect("l3")
            runCurrent()

            val state = viewModel.uiState.value
            assertEquals("l3", state.content?.id)
            assertEquals("l3", handle[LessonDestination.ARG_LESSON_ID])
            assertEquals(1, courses.detailRequests.size)
        }

    @Test
    fun `the first and last lessons have no neighbour on that side`() =
        runTest(mainDispatcherRule.testDispatcher) {
            assertNull(started("l1").uiState.value.previous)
            assertNull(started("l4").uiState.value.next)
        }

    @Test
    fun `a preview only moves between free previews`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = started("l1", preview = true)

            assertNull(viewModel.uiState.value.previous)
            assertEquals(
                "l3",
                viewModel.uiState.value.next
                    ?.id,
            )
            assertEquals(Triple("c1", "l1", true), lessons.requests.single())

            viewModel.onLessonSelect("l2")
            runCurrent()
            assertEquals("l1", viewModel.uiState.value.lessonId)
        }

    @Test
    fun `a locked lesson is not opened before enrolling`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = started("l2", preview = true)

            val state = viewModel.uiState.value
            assertTrue(state.isLocked)
            assertFalse(state.isLoading)
            assertNull(state.content)
            assertTrue(lessons.requests.isEmpty())
        }

    @Test
    fun `a failed load shows the error and retry loads again`() =
        runTest(mainDispatcherRule.testDispatcher) {
            lessons.failure = AppError.Network
            val viewModel = started("l2")
            assertEquals(AppError.Network, viewModel.uiState.value.error)

            lessons.failure = null
            viewModel.onRetry()
            runCurrent()

            assertNull(viewModel.uiState.value.error)
            assertEquals(
                "l2",
                viewModel.uiState.value.content
                    ?.id,
            )
        }

    @Test
    fun `not enrolled answers 404`() =
        runTest(mainDispatcherRule.testDispatcher) {
            lessons.failure = AppError.Server(code = "not_found", status = 404)
            val viewModel = started("l2")

            assertTrue(viewModel.uiState.value.isNotFound)
        }

    @Test
    fun `an expired video link is fetched again`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = started("l2")
            val before =
                viewModel.uiState.value.selectedVideo
                    ?.url

            now += MEDIA_REFRESH_MIN_AGE_MILLIS
            viewModel.onMediaExpired()
            runCurrent()

            val after = viewModel.uiState.value.selectedVideo
            assertEquals("v1", after?.id)
            assertTrue(after?.url != before)
            assertEquals(2, lessons.requests.size)
        }

    @Test
    fun `a video refused right after loading is reported, not refetched`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = started("l2")

            viewModel.onMediaExpired()
            runCurrent()

            assertEquals(LessonMessage.VideoFailed, viewModel.uiState.value.message)
            assertEquals(1, lessons.requests.size)
        }

    @Test
    fun `an attachment opens at once while its link is fresh`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = started("l2")

            viewModel.onAttachmentClick("a1")

            assertEquals(
                LessonEvent.OpenUrl("https://cdn/l2/a1?sig=1"),
                viewModel.uiState.value.event,
            )
            assertEquals(1, lessons.requests.size)
        }

    @Test
    fun `an old attachment link is renewed before opening`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = started("l2")

            now += MEDIA_LINK_FRESH_MILLIS
            viewModel.onAttachmentClick("a1")
            assertEquals("a1", viewModel.uiState.value.openingAttachmentId)
            runCurrent()

            val state = viewModel.uiState.value
            assertNull(state.openingAttachmentId)
            assertEquals(LessonEvent.OpenUrl("https://cdn/l2/a1?sig=2"), state.event)
            viewModel.onEventHandled()
            assertNull(viewModel.uiState.value.event)
        }

    @Test
    fun `a failed renewal says so instead of opening`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = started("l2")
            lessons.failure = AppError.Network

            now += MEDIA_LINK_FRESH_MILLIS
            viewModel.onAttachmentClick("a1")
            runCurrent()

            val state = viewModel.uiState.value
            assertNull(state.event)
            assertEquals(LessonMessage.AttachmentFailed(AppError.Network), state.message)
        }

    @Test
    fun `choosing another video of the lesson`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = started("l2")

            viewModel.onVideoSelect(1)
            assertEquals(
                "v2",
                viewModel.uiState.value.selectedVideo
                    ?.id,
            )

            viewModel.onVideoSelect(5)
            assertEquals(
                "v2",
                viewModel.uiState.value.selectedVideo
                    ?.id,
            )
        }
}
