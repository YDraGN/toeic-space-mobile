package com.toeicspace.android.feature.enrollment.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.toeicspace.android.R
import com.toeicspace.android.core.ui.TestTags
import com.toeicspace.android.core.ui.WindowWidth
import com.toeicspace.android.core.ui.components.MascotMessage
import com.toeicspace.android.core.ui.components.SecondaryButton
import com.toeicspace.android.core.ui.components.ToeicCard
import com.toeicspace.android.core.ui.currentWindowWidth
import com.toeicspace.android.core.ui.theme.AppTextStyles
import com.toeicspace.android.core.ui.theme.HintText
import com.toeicspace.android.core.ui.theme.SecondaryButtonContent
import com.toeicspace.android.core.ui.theme.SoftPageGradientBrush
import com.toeicspace.android.core.ui.theme.Spacing
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme
import com.toeicspace.android.feature.course.presentation.catalog.CourseSearchField
import com.toeicspace.android.feature.course.presentation.components.CourseCover
import com.toeicspace.android.feature.enrollment.domain.MyCourse
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val CoverSize = 88.dp
private val MinCardWidth = 340.dp
private val ContentMaxWidth = 1200.dp
private val SmallIconSize = 14.dp
private val EnrolledDateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

/** Start the next page when this many items or fewer are left below the screen. */
private const val LOAD_MORE_THRESHOLD = 4

data class MyCoursesActions(
    val onBackClick: () -> Unit = {},
    val onSearchQueryChange: (String) -> Unit = {},
    val onSearchSubmit: () -> Unit = {},
    val onClearSearch: () -> Unit = {},
    val onRetry: () -> Unit = {},
    val onRefresh: () -> Unit = {},
    val onLoadMore: () -> Unit = {},
    val onRetryLoadMore: () -> Unit = {},
    val onCoverError: () -> Unit = {},
    val onCourseClick: (courseId: String) -> Unit = {},
)

@Composable
internal fun MyCoursesRoute(
    onBackClick: () -> Unit,
    onCourseClick: (courseId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MyCoursesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    MyCoursesScreen(
        state = state,
        actions =
            MyCoursesActions(
                onBackClick = onBackClick,
                onSearchQueryChange = viewModel::onSearchQueryChange,
                onSearchSubmit = viewModel::onSearchSubmit,
                onClearSearch = viewModel::onClearSearch,
                onRetry = viewModel::onRetry,
                onRefresh = viewModel::onRefresh,
                onLoadMore = viewModel::onLoadMore,
                onRetryLoadMore = viewModel::onRetryLoadMore,
                onCoverError = viewModel::onCoverError,
                onCourseClick = onCourseClick,
            ),
        modifier = modifier,
    )
}

/**
 * "Khoá học của tôi": the search box, then the courses the learner owns, in one column on phones
 * and as many as fit on tablets. Tapping a course opens its page, where every lesson is unlocked.
 */
@Composable
internal fun MyCoursesScreen(
    state: MyCoursesUiState,
    actions: MyCoursesActions,
    modifier: Modifier = Modifier,
) {
    val windowWidth = currentWindowWidth()
    Scaffold(
        modifier = modifier.background(SoftPageGradientBrush),
        containerColor = Color.Transparent,
        topBar = {
            LearningTopBar(
                title = stringResource(R.string.my_courses_title),
                onBackClick = actions.onBackClick,
            )
        },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CourseSearchField(
                query = state.searchQuery,
                onQueryChange = actions.onSearchQueryChange,
                onSearch = actions.onSearchSubmit,
                placeholder = stringResource(R.string.my_courses_search_hint),
                modifier =
                    Modifier
                        .widthIn(max = ContentMaxWidth)
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.md, vertical = Spacing.sm),
            )
            val error = state.error
            val bodyModifier = Modifier.weight(1f).widthIn(max = ContentMaxWidth)
            when {
                state.isLoading -> LearningLoading(modifier = bodyModifier)
                error != null ->
                    LearningRetry(
                        title = stringResource(R.string.my_courses_title_error),
                        body = stringResource(error.learningMessageRes()),
                        onRetry = actions.onRetry,
                        modifier = bodyModifier,
                    )
                state.isEmpty && state.isSearching ->
                    MascotMessage(
                        mascotResId = R.drawable.oy2_search,
                        title = stringResource(R.string.my_courses_title_no_match),
                        body = stringResource(R.string.my_courses_body_no_match),
                        modifier = bodyModifier,
                    ) {
                        SecondaryButton(
                            text = stringResource(R.string.my_courses_action_clear_search),
                            onClick = actions.onClearSearch,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                state.isEmpty ->
                    MascotMessage(
                        mascotResId = R.drawable.oy2_empty,
                        title = stringResource(R.string.my_courses_title_empty),
                        body = stringResource(R.string.my_courses_body_empty),
                        modifier = bodyModifier,
                    )
                else ->
                    MyCoursesGrid(
                        state = state,
                        actions = actions,
                        columns =
                            if (windowWidth == WindowWidth.COMPACT) {
                                GridCells.Fixed(1)
                            } else {
                                GridCells.Adaptive(MinCardWidth)
                            },
                        modifier = bodyModifier,
                    )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MyCoursesGrid(
    state: MyCoursesUiState,
    actions: MyCoursesActions,
    columns: GridCells,
    modifier: Modifier = Modifier,
) {
    val gridState = rememberLazyGridState()
    val onLoadMore by rememberUpdatedState(actions.onLoadMore)
    LaunchedEffect(gridState) {
        snapshotFlow {
            val info = gridState.layoutInfo
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            info.totalItemsCount > 0 && lastVisible >= info.totalItemsCount - LOAD_MORE_THRESHOLD
        }.distinctUntilChanged()
            .filter { it }
            .collect { onLoadMore() }
    }
    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = actions.onRefresh,
        modifier = modifier,
    ) {
        LazyVerticalGrid(
            columns = columns,
            state = gridState,
            contentPadding = PaddingValues(horizontal = Spacing.md, vertical = Spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(Spacing.smd),
            verticalArrangement = Arrangement.spacedBy(Spacing.smd),
            modifier = Modifier.fillMaxSize(),
        ) {
            fullWidthItem(key = "count") {
                Text(
                    text = stringResource(R.string.my_courses_label_count, state.totalCount),
                    style = AppTextStyles.Caption,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            items(items = state.courses, key = { it.courseId }) { course ->
                MyCourseCard(
                    course = course,
                    onClick = { actions.onCourseClick(course.courseId) },
                    onCoverError = actions.onCoverError,
                )
            }
            fullWidthItem(key = "footer") {
                MyCoursesFooter(state = state, onRetry = actions.onRetryLoadMore)
            }
        }
    }
}

private fun LazyGridScope.fullWidthItem(
    key: String,
    content: @Composable () -> Unit,
) = item(key = key, span = { GridItemSpan(maxLineSpan) }) { content() }

/** Cover, name, short description, when the learner enrolled, and the way back in. */
@Composable
private fun MyCourseCard(
    course: MyCourse,
    onClick: () -> Unit,
    onCoverError: () -> Unit,
) {
    ToeicCard(
        modifier = Modifier.fillMaxWidth().testTag(TestTags.MY_COURSES_ITEM),
        contentPadding = PaddingValues(Spacing.md),
        onClick = onClick,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
            CourseCover(
                imageUrl = course.thumbnailUrl,
                courseTitle = course.title,
                seed = course.courseId,
                modifier = Modifier.width(CoverSize),
                onImageError = onCoverError,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                Text(
                    text = course.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                course.shortDescription?.let {
                    Text(
                        text = it,
                        style = AppTextStyles.HelperText,
                        color = HintText,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                course.enrolledAt?.let { enrolledAt ->
                    IconLine(
                        iconRes = R.drawable.ic_calendar,
                        text =
                            stringResource(
                                R.string.my_courses_label_enrolled_at,
                                enrolledAt.formatDate(),
                            ),
                        color = HintText,
                    )
                }
                IconLine(
                    iconRes = R.drawable.ic_play,
                    text = stringResource(R.string.my_courses_action_continue),
                    color = SecondaryButtonContent,
                )
            }
        }
    }
}

@Composable
private fun IconLine(
    iconRes: Int,
    text: String,
    color: Color,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(SmallIconSize),
        )
        Text(text = text, style = AppTextStyles.Caption, color = color)
    }
}

private fun Instant.formatDate(): String =
    EnrolledDateFormat.withZone(ZoneId.systemDefault()).format(this)

@Composable
private fun MyCoursesFooter(
    state: MyCoursesUiState,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        when {
            state.isLoadingMore ->
                CircularProgressIndicator(
                    modifier = Modifier.size(Spacing.lg),
                    strokeWidth = Spacing.space2,
                )
            state.loadMoreError != null -> {
                FooterText(stringResource(R.string.my_courses_error_load_more))
                SecondaryButton(
                    text = stringResource(R.string.state_action_retry),
                    onClick = onRetry,
                )
            }
            state.endReached && state.courses.size > 1 ->
                FooterText(stringResource(R.string.my_courses_label_end))
        }
    }
}

@Composable
private fun FooterText(text: String) {
    Text(
        text = text,
        style = AppTextStyles.Caption,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
}

internal val PreviewMyCourses =
    listOf(
        MyCourse(
            courseId = "1",
            title = "TOEIC 650+ Listening & Reading",
            shortDescription = "Lộ trình 8 tuần cho Part 2–7, có video và mẹo tránh bẫy.",
            thumbnailUrl = null,
            enrolledAt = Instant.parse("2026-11-10T08:00:00Z"),
        ),
        MyCourse(
            courseId = "2",
            title = "Nền tảng ngữ pháp TOEIC",
            shortDescription = null,
            thumbnailUrl = null,
            enrolledAt = Instant.parse("2026-11-02T08:00:00Z"),
        ),
        MyCourse(
            courseId = "3",
            title = "Speaking & Writing cấp tốc",
            shortDescription = "Luyện 11 câu Speaking và 8 câu Writing theo đề thật.",
            thumbnailUrl = null,
            enrolledAt = null,
        ),
    )

private val PreviewLoaded =
    MyCoursesUiState(
        isLoading = false,
        courses = PreviewMyCourses,
        totalCount = 3,
        endReached = true,
    )

@Preview(widthDp = 390, heightDp = 800)
@Composable
private fun MyCoursesPhonePreview() {
    ToeicSpaceTheme { MyCoursesScreen(state = PreviewLoaded, actions = MyCoursesActions()) }
}

@Preview(widthDp = 1280, heightDp = 800)
@Composable
private fun MyCoursesTabletPreview() {
    ToeicSpaceTheme { MyCoursesScreen(state = PreviewLoaded, actions = MyCoursesActions()) }
}

@Preview(widthDp = 390, heightDp = 700)
@Composable
private fun MyCoursesEmptyPreview() {
    ToeicSpaceTheme {
        MyCoursesScreen(
            state = MyCoursesUiState(isLoading = false),
            actions = MyCoursesActions(),
        )
    }
}
