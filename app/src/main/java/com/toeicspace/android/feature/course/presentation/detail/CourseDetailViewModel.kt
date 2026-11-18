package com.toeicspace.android.feature.course.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toeicspace.android.core.common.AppError
import com.toeicspace.android.core.common.AppResult
import com.toeicspace.android.core.data.repository.SessionRepository
import com.toeicspace.android.core.model.SessionState
import com.toeicspace.android.feature.course.domain.CourseDetail
import com.toeicspace.android.feature.course.domain.CourseRepository
import com.toeicspace.android.feature.course.navigation.CourseDetailDestination
import com.toeicspace.android.feature.enrollment.domain.EnrollmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Whether the viewer owns this course. Guests are always [NOT_ENROLLED]. */
enum class EnrollmentStatus {
    /** Signed in, and the answer from the server has not arrived yet. */
    CHECKING,
    NOT_ENROLLED,
    ENROLLED,
}

/** The one big button under the price. */
enum class CoursePrimaryAction {
    /** Checking the enrollment: the button waits instead of offering the wrong thing. */
    CHECKING,
    ENROLL,
    BUY,
    START,
    SOLD_OUT,
}

/** Navigation the screen has to carry out once, then report with `onEventHandled`. */
sealed interface CourseDetailEvent {
    /** A guest asked to enroll or buy: sign in, then come back here. */
    data object SignInRequired : CourseDetailEvent

    data class OpenCheckout(
        val courseId: String,
    ) : CourseDetailEvent

    data class OpenLesson(
        val courseId: String,
        val lessonId: String,
        /** A free preview opened before enrolling. */
        val preview: Boolean,
    ) : CourseDetailEvent
}

/** A dialog over the page; it stays until the learner closes it with `onDialogDismiss`. */
sealed interface CourseDetailDialog {
    /** The summary to check before enrolling in a free course. */
    data object ConfirmEnroll : CourseDetailDialog

    /** No seat left, whether it was full on arrival or filled up while enrolling. */
    data object SoldOut : CourseDetailDialog
}

/** Snackbar messages, shown once then cleared with `onMessageShown`. */
sealed interface CourseDetailMessage {
    data object Enrolled : CourseDetailMessage

    data object NoLessons : CourseDetailMessage

    data object LessonLocked : CourseDetailMessage

    data class Failed(
        val error: AppError,
    ) : CourseDetailMessage
}

data class CourseDetailUiState(
    val isLoading: Boolean = true,
    val course: CourseDetail? = null,
    val error: AppError? = null,
    /** Chapters showing their lessons. The first one opens when the course loads. */
    val expandedModuleIds: Set<String> = emptySet(),
    /** Null until the session has been read from local storage. */
    val isSignedIn: Boolean? = null,
    val enrollment: EnrollmentStatus = EnrollmentStatus.CHECKING,
    val isEnrolling: Boolean = false,
    /** Enrolling failed because the last seat went after the page loaded. */
    val becameFull: Boolean = false,
    val event: CourseDetailEvent? = null,
    val message: CourseDetailMessage? = null,
    val dialog: CourseDetailDialog? = null,
) {
    /** Content answers 404 for a course that does not exist or is not published. */
    val isNotFound: Boolean get() = (error as? AppError.Server)?.status == HTTP_NOT_FOUND

    val isEnrolled: Boolean get() = enrollment == EnrollmentStatus.ENROLLED

    /** No seat left; someone already enrolled keeps theirs. */
    val isSoldOut: Boolean
        get() = becameFull || (course?.summary?.remainingSeats ?: 1) <= 0

    val primaryAction: CoursePrimaryAction
        get() =
            when {
                isEnrolled -> CoursePrimaryAction.START
                enrollment == EnrollmentStatus.CHECKING -> CoursePrimaryAction.CHECKING
                isSoldOut -> CoursePrimaryAction.SOLD_OUT
                course?.summary?.price?.isFree == true -> CoursePrimaryAction.ENROLL
                else -> CoursePrimaryAction.BUY
            }
}

private const val HTTP_NOT_FOUND = 404

@HiltViewModel
class CourseDetailViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val courseRepository: CourseRepository,
        private val enrollmentRepository: EnrollmentRepository,
        sessionRepository: SessionRepository,
    ) : ViewModel() {
        // Read by key, not toRoute(), so plain JVM tests can build the handle from a map.
        private val courseId: String =
            checkNotNull(savedStateHandle[CourseDetailDestination.ARG_COURSE_ID]) {
                "CourseDetailDestination needs a courseId"
            }

        private val _uiState = MutableStateFlow(CourseDetailUiState())
        val uiState: StateFlow<CourseDetailUiState> = _uiState.asStateFlow()

        private var enrollmentJob: Job? = null

        init {
            load()
            // A guest who signs in from this page comes back to it, so the page follows the session
            // instead of reading it once.
            viewModelScope.launch {
                sessionRepository.session
                    .filterIsInstance<SessionState.Ready>()
                    .map { it.user?.id }
                    .distinctUntilChanged()
                    .collect { userId ->
                        if (userId == null) {
                            enrollmentJob?.cancel()
                            _uiState.update {
                                it.copy(
                                    isSignedIn = false,
                                    enrollment = EnrollmentStatus.NOT_ENROLLED,
                                )
                            }
                        } else {
                            _uiState.update {
                                it.copy(isSignedIn = true, enrollment = EnrollmentStatus.CHECKING)
                            }
                            checkEnrollment()
                        }
                    }
            }
        }

        fun onRetry() {
            if (_uiState.value.isLoading) return
            load()
        }

        /** Back from checkout (or another app), the course may have been bought meanwhile. */
        fun onResume() {
            val state = _uiState.value
            if (state.isSignedIn == true && state.enrollment == EnrollmentStatus.NOT_ENROLLED) {
                checkEnrollment()
            }
        }

        fun onModuleToggle(moduleId: String) {
            _uiState.update { state ->
                val expanded = state.expandedModuleIds
                state.copy(
                    expandedModuleIds =
                        if (moduleId in expanded) expanded - moduleId else expanded + moduleId,
                )
            }
        }

        fun onPrimaryClick() {
            val state = _uiState.value
            val course = state.course ?: return
            if (state.isEnrolling) return
            when (state.primaryAction) {
                CoursePrimaryAction.CHECKING -> Unit
                CoursePrimaryAction.SOLD_OUT ->
                    _uiState.update { it.copy(dialog = CourseDetailDialog.SoldOut) }
                CoursePrimaryAction.START -> openFirstLesson(course)
                CoursePrimaryAction.ENROLL ->
                    if (state.isSignedIn == true) {
                        _uiState.update { it.copy(dialog = CourseDetailDialog.ConfirmEnroll) }
                    } else {
                        send(CourseDetailEvent.SignInRequired)
                    }
                CoursePrimaryAction.BUY ->
                    send(
                        if (state.isSignedIn == true) {
                            CourseDetailEvent.OpenCheckout(courseId)
                        } else {
                            CourseDetailEvent.SignInRequired
                        },
                    )
            }
        }

        /** Enrolled learners open any lesson; everyone else only the free previews. */
        fun onLessonClick(lessonId: String) {
            val state = _uiState.value
            val lesson =
                state.course
                    ?.modules
                    ?.flatMap { it.lessons }
                    ?.firstOrNull { it.id == lessonId } ?: return
            when {
                state.isEnrolled ->
                    send(CourseDetailEvent.OpenLesson(courseId, lessonId, preview = false))
                lesson.isFreePreview ->
                    send(CourseDetailEvent.OpenLesson(courseId, lessonId, preview = true))
                else -> _uiState.update { it.copy(message = CourseDetailMessage.LessonLocked) }
            }
        }

        /** "Xác nhận đăng ký" in the summary dialog; the dialog stays open while the request runs. */
        fun onConfirmEnroll() {
            val state = _uiState.value
            if (state.dialog != CourseDetailDialog.ConfirmEnroll || state.isEnrolling) return
            enroll()
        }

        /** Closing a dialog; the enroll summary can't be closed while its request is running. */
        fun onDialogDismiss() {
            if (_uiState.value.isEnrolling) return
            _uiState.update { it.copy(dialog = null) }
        }

        fun onEventHandled() {
            _uiState.update { it.copy(event = null) }
        }

        fun onMessageShown() {
            _uiState.update { it.copy(message = null) }
        }

        private fun send(event: CourseDetailEvent) {
            _uiState.update { it.copy(event = event) }
        }

        private fun openFirstLesson(course: CourseDetail) {
            val first = course.modules.firstNotNullOfOrNull { it.lessons.firstOrNull() }
            if (first == null) {
                _uiState.update { it.copy(message = CourseDetailMessage.NoLessons) }
            } else {
                send(CourseDetailEvent.OpenLesson(courseId, first.id, preview = false))
            }
        }

        private fun checkEnrollment() {
            enrollmentJob?.cancel()
            enrollmentJob =
                viewModelScope.launch {
                    val result = enrollmentRepository.getEnrollment(courseId)
                    _uiState.update {
                        it.copy(
                            enrollment =
                                when {
                                    result is AppResult.Success && result.data != null ->
                                        EnrollmentStatus.ENROLLED
                                    // Unknown counts as not enrolled: enrolling again is harmless
                                    // (the server returns the existing enrollment) and the learner
                                    // is not stuck behind a spinner.
                                    else -> EnrollmentStatus.NOT_ENROLLED
                                },
                        )
                    }
                }
        }

        private fun enroll() {
            _uiState.update { it.copy(isEnrolling = true) }
            viewModelScope.launch {
                when (val result = enrollmentRepository.enroll(courseId)) {
                    is AppResult.Success ->
                        _uiState.update {
                            it.copy(
                                isEnrolling = false,
                                enrollment = EnrollmentStatus.ENROLLED,
                                message = CourseDetailMessage.Enrolled,
                                dialog = null,
                            )
                        }
                    is AppResult.Failure -> {
                        val full =
                            (result.error as? AppError.Server)?.code ==
                                EnrollmentRepository.ERROR_COURSE_FULL
                        _uiState.update {
                            // The last seat went while the summary was open: say so in the same
                            // place, rather than a snackbar behind the dialog.
                            it.copy(
                                isEnrolling = false,
                                becameFull = it.becameFull || full,
                                dialog = if (full) CourseDetailDialog.SoldOut else null,
                                message =
                                    if (full) {
                                        null
                                    } else {
                                        CourseDetailMessage.Failed(
                                            result.error,
                                        )
                                    },
                            )
                        }
                    }
                }
            }
        }

        private fun load() {
            _uiState.update { it.copy(isLoading = true, error = null) }
            viewModelScope.launch {
                when (val result = courseRepository.getCourse(courseId)) {
                    is AppResult.Success ->
                        _uiState.update { state ->
                            val course = result.data
                            state.copy(
                                isLoading = false,
                                course = course,
                                expandedModuleIds =
                                    state.expandedModuleIds.ifEmpty {
                                        setOfNotNull(course.modules.firstOrNull()?.id)
                                    },
                            )
                        }
                    is AppResult.Failure ->
                        _uiState.update { it.copy(isLoading = false, error = result.error) }
                }
            }
        }
    }
