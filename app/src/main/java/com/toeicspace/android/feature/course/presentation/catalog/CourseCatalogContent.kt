package com.toeicspace.android.feature.course.presentation.catalog

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.toeicspace.android.R
import com.toeicspace.android.core.ui.WindowWidth
import com.toeicspace.android.core.ui.components.PrimaryButton
import com.toeicspace.android.core.ui.components.SecondaryButton
import com.toeicspace.android.core.ui.components.ToeicCard
import com.toeicspace.android.core.ui.theme.AppTextStyles
import com.toeicspace.android.core.ui.theme.Spacing
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme
import com.toeicspace.android.feature.auth.presentation.common.messageRes
import com.toeicspace.android.feature.course.domain.CourseExamType
import com.toeicspace.android.feature.course.domain.CourseLevel
import com.toeicspace.android.feature.course.presentation.components.CourseCard
import com.toeicspace.android.feature.course.presentation.components.PreviewCourses
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

/** Everything the catalog can ask its host to do. */
data class CourseCatalogActions(
    val onSearchQueryChange: (String) -> Unit = {},
    val onSearchSubmit: () -> Unit = {},
    val onExamTypeSelect: (CourseExamType?) -> Unit = {},
    val onLevelSelect: (CourseLevel?) -> Unit = {},
    val onTargetScoreSelect: (Int?) -> Unit = {},
    val onClearFilters: () -> Unit = {},
    val onRetry: () -> Unit = {},
    val onRefresh: () -> Unit = {},
    val onLoadMore: () -> Unit = {},
    val onRetryLoadMore: () -> Unit = {},
    val onCourseClick: (String) -> Unit = {},
)

private val FilterPanelWidth = 280.dp
private val StateMascotSize = 128.dp

/** Start loading the next page when this many items or fewer are left below the screen. */
private const val LOAD_MORE_THRESHOLD = 4

/**
 * Phones: one column with the search box and exam chips on top, level and score in a sheet.
 * Medium windows: the same, in two columns. Expanded windows: every filter in a side panel and a
 * two-column grid beside it.
 */
@Composable
internal fun CourseCatalogContent(
    state: CourseCatalogUiState,
    actions: CourseCatalogActions,
    windowWidth: WindowWidth,
    modifier: Modifier = Modifier,
) {
    var showFilterSheet by rememberSaveable { mutableStateOf(false) }
    val sidePanel = windowWidth == WindowWidth.EXPANDED

    Row(
        modifier = modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        if (sidePanel) {
            ToeicCard(
                modifier =
                    Modifier
                        .padding(start = Spacing.md, top = Spacing.smd, bottom = Spacing.smd)
                        .width(FilterPanelWidth),
            ) {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(Spacing.smd),
                ) {
                    FilterHeader(
                        showClear = state.hasActiveFilter,
                        onClear = actions.onClearFilters,
                    )
                    CourseFilterOptions(
                        filter = state.filter,
                        onExamTypeSelect = actions.onExamTypeSelect,
                        onLevelSelect = actions.onLevelSelect,
                        onTargetScoreSelect = actions.onTargetScoreSelect,
                        showExamType = true,
                    )
                }
            }
        }
        CourseGrid(
            state = state,
            actions = actions,
            columns = if (windowWidth.isAtLeastMedium) 2 else 1,
            showFilterRow = !sidePanel,
            onFilterClick = { showFilterSheet = true },
            modifier = Modifier.weight(1f).fillMaxHeight(),
        )
    }

    if (showFilterSheet && !sidePanel) {
        FilterSheet(state = state, actions = actions, onDismiss = { showFilterSheet = false })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CourseGrid(
    state: CourseCatalogUiState,
    actions: CourseCatalogActions,
    columns: Int,
    showFilterRow: Boolean,
    onFilterClick: () -> Unit,
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
            columns = GridCells.Fixed(columns),
            state = gridState,
            contentPadding = PaddingValues(horizontal = Spacing.md, vertical = Spacing.smd),
            horizontalArrangement = Arrangement.spacedBy(Spacing.smd),
            verticalArrangement = Arrangement.spacedBy(Spacing.smd),
            modifier = Modifier.fillMaxSize(),
        ) {
            fullWidthItem(key = "search") {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CourseSearchField(
                        query = state.searchQuery,
                        onQueryChange = actions.onSearchQueryChange,
                        onSearch = actions.onSearchSubmit,
                        modifier = Modifier.weight(1f),
                    )
                    if (showFilterRow) {
                        CourseFilterButton(
                            activeCount = state.sheetFilterCount,
                            onClick = onFilterClick,
                        )
                    }
                }
            }
            if (showFilterRow) {
                fullWidthItem(key = "exam-types") {
                    ExamTypeChipRow(
                        selected = state.filter.examType,
                        onSelect = actions.onExamTypeSelect,
                    )
                }
            }
            catalogBody(state = state, actions = actions)
        }
    }
}

private fun LazyGridScope.fullWidthItem(
    key: String,
    content: @Composable () -> Unit,
) = item(key = key, span = { GridItemSpan(maxLineSpan) }) { content() }

private fun LazyGridScope.catalogBody(
    state: CourseCatalogUiState,
    actions: CourseCatalogActions,
) {
    val error = state.error
    when {
        state.isLoading ->
            fullWidthItem(key = "loading") {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.xxl),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
        error != null ->
            fullWidthItem(key = "error") {
                StateMessage(
                    mascotRes = R.drawable.oy2_error,
                    title = stringResource(R.string.course_title_error),
                    body = stringResource(error.messageRes()),
                ) {
                    SecondaryButton(
                        text = stringResource(R.string.course_action_retry),
                        onClick = actions.onRetry,
                    )
                }
            }
        state.isEmpty && state.hasActiveFilter ->
            fullWidthItem(key = "empty") {
                StateMessage(
                    mascotRes = R.drawable.oy2_search,
                    title = stringResource(R.string.course_title_empty_filtered),
                    body = stringResource(R.string.course_body_empty_filtered),
                ) {
                    SecondaryButton(
                        text = stringResource(R.string.course_action_clear_filters),
                        onClick = actions.onClearFilters,
                    )
                }
            }
        state.isEmpty ->
            fullWidthItem(key = "empty") {
                StateMessage(
                    mascotRes = R.drawable.oy2_waiting,
                    title = stringResource(R.string.course_title_empty),
                    body = stringResource(R.string.course_body_empty),
                )
            }
        else -> {
            fullWidthItem(key = "count") {
                Text(
                    text = stringResource(R.string.course_label_result_count, state.totalCount),
                    style = AppTextStyles.Caption,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            items(items = state.courses, key = { it.id }) { course ->
                CourseCard(course = course, onClick = { actions.onCourseClick(course.id) })
            }
            fullWidthItem(
                key = "footer",
            ) { CatalogFooter(state = state, onRetry = actions.onRetryLoadMore) }
        }
    }
}

@Composable
private fun CatalogFooter(
    state: CourseCatalogUiState,
    onRetry: () -> Unit,
) {
    val loadMoreError = state.loadMoreError
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        when {
            state.isLoadingMore -> {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                FooterText(stringResource(R.string.course_label_loading_more))
            }
            loadMoreError != null -> {
                FooterText(stringResource(R.string.course_error_load_more))
                SecondaryButton(
                    text = stringResource(R.string.course_action_retry),
                    onClick = onRetry,
                )
            }
            state.endReached -> FooterText(stringResource(R.string.course_label_end))
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

/** Mascot, title, body and optional actions for the loading-free states (error, empty). */
@Composable
private fun StateMessage(
    @DrawableRes mascotRes: Int,
    title: String,
    body: String,
    actions: @Composable () -> Unit = {},
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.xl, horizontal = Spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        Image(
            painter = painterResource(mascotRes),
            contentDescription = null,
            modifier = Modifier.size(StateMascotSize),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        actions()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterSheet(
    state: CourseCatalogUiState,
    actions: CourseCatalogActions,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier.padding(start = Spacing.lg, end = Spacing.lg, bottom = Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            FilterHeader(showClear = state.sheetFilterCount > 0, onClear = {
                actions.onLevelSelect(null)
                actions.onTargetScoreSelect(null)
            })
            CourseFilterOptions(
                filter = state.filter,
                onExamTypeSelect = actions.onExamTypeSelect,
                onLevelSelect = actions.onLevelSelect,
                onTargetScoreSelect = actions.onTargetScoreSelect,
            )
            PrimaryButton(
                text = stringResource(R.string.course_action_done),
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

private val LoadedState =
    CourseCatalogUiState(
        courses = PreviewCourses,
        totalCount = PreviewCourses.size,
        isLoading = false,
        endReached = true,
    )

@Preview(showBackground = true, backgroundColor = 0xFFE7FAFF, widthDp = 360, heightDp = 760)
@Composable
private fun CourseCatalogPhonePreview() {
    ToeicSpaceTheme {
        CourseCatalogContent(
            state = LoadedState,
            actions = CourseCatalogActions(),
            windowWidth = WindowWidth.COMPACT,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFE7FAFF, widthDp = 1000, heightDp = 700)
@Composable
private fun CourseCatalogTabletPreview() {
    ToeicSpaceTheme {
        CourseCatalogContent(
            state = LoadedState,
            actions = CourseCatalogActions(),
            windowWidth = WindowWidth.EXPANDED,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFE7FAFF, widthDp = 360, heightDp = 640)
@Composable
private fun CourseCatalogEmptyPreview() {
    ToeicSpaceTheme {
        CourseCatalogContent(
            state = CourseCatalogUiState(isLoading = false, searchQuery = "ielts"),
            actions = CourseCatalogActions(),
            windowWidth = WindowWidth.COMPACT,
        )
    }
}
