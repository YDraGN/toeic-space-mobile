package com.toeicspace.android.feature.course.presentation.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toeicspace.android.R
import com.toeicspace.android.core.common.AppResult
import com.toeicspace.android.core.data.repository.SessionRepository
import com.toeicspace.android.core.model.SessionState
import com.toeicspace.android.core.model.UserRole
import com.toeicspace.android.core.ui.theme.AppTextStyles
import com.toeicspace.android.core.ui.theme.SkyTone
import com.toeicspace.android.core.ui.theme.Spacing
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme
import com.toeicspace.android.feature.course.domain.CourseFilter
import com.toeicspace.android.feature.course.domain.CourseRepository
import com.toeicspace.android.feature.course.domain.CourseSummary
import com.toeicspace.android.feature.course.navigation.CourseDetailDestination
import com.toeicspace.android.feature.course.presentation.components.CourseTileCard
import com.toeicspace.android.feature.course.presentation.components.PreviewCourses
import com.toeicspace.android.feature.enrollment.domain.EnrollmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

/** At most this many suggestions, like the web's `RelatedCourses`. */
internal const val RELATED_COURSES_LIMIT = 3

/** Phone tiles keep this width in the horizontal row, so the next tile peeks in. */
private val CompactCardWidth = 260.dp

/**
 * Published courses other than [currentId] that the learner doesn't own, in catalog order, capped
 * at [limit]. Mirrors the filtering in the web's `RelatedCourses.tsx`.
 */
internal fun pickRelatedCourses(
    courses: List<CourseSummary>,
    currentId: String,
    ownedIds: Set<String>,
    limit: Int = RELATED_COURSES_LIMIT,
): List<CourseSummary> =
    courses
        .asSequence()
        .filter { it.id != currentId && it.id !in ownedIds }
        .take(limit)
        .toList()

/**
 * "Khoá học bạn có thể khám phá" under a course. It loads on its own, apart from the course, and
 * any failure just leaves the list empty, so it never gets in the way of the page.
 */
@HiltViewModel
class RelatedCoursesViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val courseRepository: CourseRepository,
        private val enrollmentRepository: EnrollmentRepository,
        sessionRepository: SessionRepository,
    ) : ViewModel() {
        // Read by key, like CourseDetailViewModel, so plain JVM tests can build the handle.
        private val courseId: String =
            checkNotNull(savedStateHandle[CourseDetailDestination.ARG_COURSE_ID]) {
                "CourseDetailDestination needs a courseId"
            }

        private val _courses = MutableStateFlow<List<CourseSummary>>(emptyList())

        /** The suggestions; empty (and the section hidden) until they load, or when there are none. */
        val courses: StateFlow<List<CourseSummary>> = _courses.asStateFlow()

        private var loadJob: Job? = null

        init {
            // The owned courses depend on who is signed in, so a sign-in on this page reloads them.
            viewModelScope.launch {
                sessionRepository.session
                    .filterIsInstance<SessionState.Ready>()
                    .map { session -> session.user?.takeIf { it.role == UserRole.User }?.id }
                    .distinctUntilChanged()
                    .collect { studentId -> load(isStudent = studentId != null) }
            }
        }

        private fun load(isStudent: Boolean) {
            loadJob?.cancel()
            loadJob =
                viewModelScope.launch {
                    val owned =
                        if (isStudent) {
                            when (val result = enrollmentRepository.getOwnedCourseIds()) {
                                is AppResult.Success -> result.data
                                // Without the owned ids a suggestion could be a course already bought.
                                is AppResult.Failure -> {
                                    _courses.value = emptyList()
                                    return@launch
                                }
                            }
                        } else {
                            emptySet()
                        }
                    _courses.value =
                        when (val result = courseRepository.getCourses(CourseFilter(), page = 1)) {
                            is AppResult.Success ->
                                pickRelatedCourses(
                                    result.data.items,
                                    courseId,
                                    owned,
                                )
                            is AppResult.Failure -> emptyList()
                        }
                }
        }
    }

/**
 * The section as the last item of the course page. Phones (and [wide] = false) get a horizontal
 * row of cards; the tablet main column gets the three cards side by side.
 */
internal fun LazyListScope.relatedCourses(
    courses: List<CourseSummary>,
    wide: Boolean,
    onCourseClick: (String) -> Unit,
) {
    if (courses.isEmpty()) return
    item(key = "related") {
        RelatedCoursesSection(courses = courses, wide = wide, onCourseClick = onCourseClick)
    }
}

@Composable
internal fun RelatedCoursesSection(
    courses: List<CourseSummary>,
    wide: Boolean,
    onCourseClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(top = Spacing.sm),
        verticalArrangement = Arrangement.spacedBy(Spacing.smd),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.smd),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SectionIcon(iconRes = R.drawable.ic_book_open, tone = SkyTone)
            Text(
                text = stringResource(R.string.course_title_related),
                style = AppTextStyles.SectionTitle,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f).semantics { heading() },
            )
        }
        if (wide) {
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.smd)) {
                courses.forEach { course ->
                    CourseTileCard(
                        course = course,
                        onClick = { onCourseClick(course.id) },
                        modifier = Modifier.weight(1f),
                    )
                }
                // Fewer than three keep their width instead of stretching across the column.
                repeat(
                    RELATED_COURSES_LIMIT - courses.size,
                ) { Spacer(modifier = Modifier.weight(1f)) }
            }
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.smd)) {
                items(items = courses, key = { it.id }) { course ->
                    CourseTileCard(
                        course = course,
                        onClick = { onCourseClick(course.id) },
                        modifier = Modifier.width(CompactCardWidth),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390)
@Composable
private fun RelatedCoursesPhonePreview() {
    ToeicSpaceTheme {
        RelatedCoursesSection(
            courses = PreviewCourses.take(RELATED_COURSES_LIMIT),
            wide = false,
            onCourseClick = {},
            modifier = Modifier.padding(Spacing.md),
        )
    }
}

@Preview(showBackground = true, widthDp = 900)
@Composable
private fun RelatedCoursesTabletPreview() {
    ToeicSpaceTheme {
        RelatedCoursesSection(
            courses = PreviewCourses.take(RELATED_COURSES_LIMIT),
            wide = true,
            onCourseClick = {},
            modifier = Modifier.padding(Spacing.lg),
        )
    }
}
