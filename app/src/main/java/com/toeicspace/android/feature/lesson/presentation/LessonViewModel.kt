package com.toeicspace.android.feature.lesson.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toeicspace.android.core.common.AppError
import com.toeicspace.android.core.common.AppResult
import com.toeicspace.android.core.common.Clock
import com.toeicspace.android.feature.course.domain.CourseDetail
import com.toeicspace.android.feature.course.domain.CourseRepository
import com.toeicspace.android.feature.lesson.domain.LessonContent
import com.toeicspace.android.feature.lesson.domain.LessonRepository
import com.toeicspace.android.feature.lesson.domain.LessonVideo
import com.toeicspace.android.feature.lesson.navigation.LessonDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Signed media links last five minutes. An attachment opened later than this after loading gets a
 * fresh link first, so the browser does not land on an "access denied" page.
 */
internal const val MEDIA_LINK_FRESH_MILLIS = 4 * 60_000L

/**
 * A video refused sooner than this after loading did not expire: something else is wrong, and
 * refetching would only loop.
 */
internal const val MEDIA_REFRESH_MIN_AGE_MILLIS = 30_000L

/** A lesson in curriculum order, as the outline and "Bài trước" / "Bài sau" see it. */
data class OutlineLesson(
    val id: String,
    val title: String,
    val moduleTitle: String,
    val durationSeconds: Int?,
    val isFreePreview: Boolean,
)

sealed interface LessonEvent {
    data class OpenUrl(
        val url: String,
    ) : LessonEvent
}

sealed interface LessonMessage {
    /** The video could not be played even with a fresh link. */
    data object VideoFailed : LessonMessage

    data class AttachmentFailed(
        val error: AppError,
    ) : LessonMessage
}

data class LessonUiState(
    val courseId: String,
    val lessonId: String,
    val preview: Boolean,
    val courseTitle: String? = null,
    /** Every published lesson of the course in teaching order, locked ones included. */
    val outline: List<OutlineLesson> = emptyList(),
    val isLoading: Boolean = true,
    val content: LessonContent? = null,
    val error: AppError? = null,
    val selectedVideoIndex: Int = 0,
    /** Fetching fresh links for an attachment the learner tapped. */
    val openingAttachmentId: String? = null,
    val event: LessonEvent? = null,
    val message: LessonMessage? = null,
) {
    /** What this viewer may open: everything once enrolled, the free previews before. */
    val accessible: List<OutlineLesson>
        get() = if (preview) outline.filter { it.isFreePreview } else outline

    /** A free preview was asked for, but this lesson is not one: it needs an enrollment. */
    val isLocked: Boolean
        get() =
            preview &&
                outline.isNotEmpty() &&
                outline.none { it.id == lessonId && it.isFreePreview }

    val current: OutlineLesson? get() = outline.firstOrNull { it.id == lessonId }

    val previous: OutlineLesson?
        get() =
            accessible.indexOfFirst { it.id == lessonId }.takeIf { it > 0 }?.let {
                accessible[
                    it -
                        1,
                ]
            }

    val next: OutlineLesson?
        get() =
            accessible
                .indexOfFirst { it.id == lessonId }
                .takeIf { it >= 0 && it < accessible.lastIndex }
                ?.let { accessible[it + 1] }

    val selectedVideo: LessonVideo? get() = content?.videos?.getOrNull(selectedVideoIndex)

    val isNotFound: Boolean get() = (error as? AppError.Server)?.status == HTTP_NOT_FOUND
}

private const val HTTP_NOT_FOUND = 404

@HiltViewModel
class LessonViewModel
    @Inject
    constructor(
        private val savedStateHandle: SavedStateHandle,
        private val courseRepository: CourseRepository,
        private val lessonRepository: LessonRepository,
        private val clock: Clock,
    ) : ViewModel() {
        private val _uiState =
            MutableStateFlow(
                LessonUiState(
                    courseId =
                        checkNotNull(savedStateHandle[LessonDestination.ARG_COURSE_ID]) {
                            "LessonDestination needs a courseId"
                        },
                    // The current lesson is kept in the handle, so "Bài sau" survives process death.
                    lessonId =
                        checkNotNull(savedStateHandle[LessonDestination.ARG_LESSON_ID]) {
                            "LessonDestination needs a lessonId"
                        },
                    preview = savedStateHandle[LessonDestination.ARG_PREVIEW] ?: false,
                ),
            )
        val uiState: StateFlow<LessonUiState> = _uiState.asStateFlow()

        private var fetchedAtMillis = 0L
        private var loadJob: Job? = null
        private var refreshJob: Job? = null

        init {
            load()
        }

        fun onRetry() {
            if (_uiState.value.isLoading) return
            load()
        }

        /** "Bài trước", "Bài sau" or a lesson in the outline. Locked lessons are ignored. */
        fun onLessonSelect(lessonId: String) {
            val state = _uiState.value
            if (lessonId == state.lessonId || state.accessible.none { it.id == lessonId }) return
            savedStateHandle[LessonDestination.ARG_LESSON_ID] = lessonId
            _uiState.update {
                it.copy(
                    lessonId = lessonId,
                    content = null,
                    selectedVideoIndex = 0,
                    openingAttachmentId = null,
                )
            }
            load()
        }

        fun onVideoSelect(index: Int) {
            val videos = _uiState.value.content?.videos ?: return
            if (index !in videos.indices) return
            _uiState.update { it.copy(selectedVideoIndex = index) }
        }

        /** The player was refused (401/403): the signed link expired, so fetch fresh ones. */
        fun onMediaExpired() {
            if (refreshJob?.isActive == true) return
            if (clock.nowMillis() - fetchedAtMillis < MEDIA_REFRESH_MIN_AGE_MILLIS) {
                _uiState.update { it.copy(message = LessonMessage.VideoFailed) }
                return
            }
            refreshJob = viewModelScope.launch { refreshContent() }
        }

        fun onAttachmentClick(attachmentId: String) {
            val state = _uiState.value
            val attachment =
                state.content?.attachments?.firstOrNull { it.id == attachmentId } ?: return
            if (state.openingAttachmentId != null) return
            if (clock.nowMillis() - fetchedAtMillis < MEDIA_LINK_FRESH_MILLIS) {
                _uiState.update { it.copy(event = LessonEvent.OpenUrl(attachment.url)) }
                return
            }
            _uiState.update { it.copy(openingAttachmentId = attachmentId) }
            refreshJob?.cancel()
            refreshJob =
                viewModelScope.launch {
                    val error = refreshContent()
                    _uiState.update { current ->
                        val fresh =
                            current.content?.attachments?.firstOrNull {
                                it.id ==
                                    attachmentId
                            }
                        current.copy(
                            openingAttachmentId = null,
                            event =
                                fresh?.takeIf { error == null }?.let {
                                    LessonEvent.OpenUrl(
                                        it.url,
                                    )
                                },
                            message = error?.let { LessonMessage.AttachmentFailed(it) },
                        )
                    }
                }
        }

        fun onEventHandled() {
            _uiState.update { it.copy(event = null) }
        }

        fun onMessageShown() {
            _uiState.update { it.copy(message = null) }
        }

        /** Fetches the current lesson again, keeping what is on screen; returns the error if any. */
        private suspend fun refreshContent(): AppError? {
            val state = _uiState.value
            return when (
                val result =
                    lessonRepository.getLesson(
                        state.courseId,
                        state.lessonId,
                        state.preview,
                    )
            ) {
                is AppResult.Success -> {
                    fetchedAtMillis = clock.nowMillis()
                    _uiState.update {
                        if (it.lessonId == state.lessonId) it.copy(content = result.data) else it
                    }
                    null
                }
                is AppResult.Failure -> result.error
            }
        }

        private fun load() {
            loadJob?.cancel()
            refreshJob?.cancel()
            _uiState.update { it.copy(isLoading = true, error = null) }
            loadJob =
                viewModelScope.launch {
                    if (_uiState.value.outline.isEmpty()) {
                        when (val course = courseRepository.getCourse(_uiState.value.courseId)) {
                            is AppResult.Success -> applyCourse(course.data)
                            is AppResult.Failure -> {
                                _uiState.update { it.copy(isLoading = false, error = course.error) }
                                return@launch
                            }
                        }
                    }
                    val state = _uiState.value
                    // Checked here too, not only by the server, so a locked lesson costs no request.
                    if (state.isLocked) {
                        _uiState.update { it.copy(isLoading = false) }
                        return@launch
                    }
                    when (
                        val result =
                            lessonRepository.getLesson(
                                state.courseId,
                                state.lessonId,
                                state.preview,
                            )
                    ) {
                        is AppResult.Success -> {
                            fetchedAtMillis = clock.nowMillis()
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    content = result.data,
                                    selectedVideoIndex = 0,
                                )
                            }
                        }
                        is AppResult.Failure ->
                            _uiState.update { it.copy(isLoading = false, error = result.error) }
                    }
                }
        }

        private fun applyCourse(course: CourseDetail) {
            _uiState.update {
                it.copy(
                    courseTitle = course.summary.title,
                    outline =
                        course.modules.flatMap { module ->
                            module.lessons.map { lesson ->
                                OutlineLesson(
                                    id = lesson.id,
                                    title = lesson.title,
                                    moduleTitle = module.title,
                                    durationSeconds = lesson.durationSeconds,
                                    isFreePreview = lesson.isFreePreview,
                                )
                            }
                        },
                )
            }
        }
    }
