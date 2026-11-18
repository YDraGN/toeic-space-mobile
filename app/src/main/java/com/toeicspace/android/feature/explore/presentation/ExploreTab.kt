package com.toeicspace.android.feature.explore.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.toeicspace.android.core.ui.currentWindowWidth
import com.toeicspace.android.feature.course.presentation.catalog.CourseCatalogActions
import com.toeicspace.android.feature.course.presentation.catalog.CourseCatalogContent
import com.toeicspace.android.feature.course.presentation.catalog.CourseCatalogViewModel

/**
 * Open to everyone and the guest start tab: the public course catalog, which anyone can browse
 * before signing up.
 */
@Composable
internal fun ExploreTab(
    onCourseClick: (courseId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CourseCatalogViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.onResume() }
    val actions =
        remember(viewModel, onCourseClick) {
            CourseCatalogActions(
                onSearchQueryChange = viewModel::onSearchQueryChange,
                onSearchSubmit = viewModel::onSearchSubmit,
                onExamTypeSelect = viewModel::onExamTypeSelect,
                onLevelSelect = viewModel::onLevelSelect,
                onTargetScoreSelect = viewModel::onTargetScoreSelect,
                onClearFilters = viewModel::onClearFilters,
                onRetry = viewModel::onRetry,
                onRefresh = viewModel::onRefresh,
                onLoadMore = viewModel::onLoadMore,
                onRetryLoadMore = viewModel::onRetryLoadMore,
                onCourseClick = onCourseClick,
            )
        }
    CourseCatalogContent(
        state = state,
        actions = actions,
        windowWidth = currentWindowWidth(),
        modifier = modifier,
    )
}
