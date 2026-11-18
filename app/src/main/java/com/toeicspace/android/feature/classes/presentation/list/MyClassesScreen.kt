package com.toeicspace.android.feature.classes.presentation.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.toeicspace.android.R
import com.toeicspace.android.core.common.AppError
import com.toeicspace.android.core.ui.WindowWidth
import com.toeicspace.android.core.ui.components.EmptyState
import com.toeicspace.android.core.ui.components.ErrorState
import com.toeicspace.android.core.ui.components.LoadingState
import com.toeicspace.android.core.ui.components.OfflineBanner
import com.toeicspace.android.core.ui.currentWindowWidth
import com.toeicspace.android.core.ui.theme.AppTextStyles
import com.toeicspace.android.core.ui.theme.Spacing
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme
import com.toeicspace.android.feature.classes.presentation.components.ClassCard
import com.toeicspace.android.feature.classes.presentation.components.ClassesPageBackground
import com.toeicspace.android.feature.classes.presentation.components.ClassesTopBar
import com.toeicspace.android.feature.classes.presentation.components.PreviewClasses
import com.toeicspace.android.feature.classes.presentation.components.classMessageRes
import com.toeicspace.android.feature.classes.presentation.components.staleBannerError

/** Narrowest class card on wide windows; cards fill as many columns as fit above it. */
private val MinCardWidth = 340.dp

data class MyClassesActions(
    val onBackClick: () -> Unit = {},
    val onRefresh: () -> Unit = {},
    val onRetry: () -> Unit = {},
    val onClassClick: (classId: String) -> Unit = {},
)

@Composable
internal fun MyClassesRoute(
    onBackClick: () -> Unit,
    onClassClick: (classId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MyClassesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    MyClassesScreen(
        state = state,
        actions =
            MyClassesActions(
                onBackClick = onBackClick,
                onRefresh = viewModel::onRefresh,
                onRetry = viewModel::onRetry,
                onClassClick = onClassClick,
            ),
        windowWidth = currentWindowWidth(),
        modifier = modifier,
    )
}

/**
 * The classes the student is enrolled in. One column of cards on phones, as many columns as fit on
 * wider windows. Pull to refresh; when a refresh fails the list loaded before stays, with a banner.
 */
@Composable
internal fun MyClassesScreen(
    state: MyClassesUiState,
    actions: MyClassesActions,
    windowWidth: WindowWidth,
    modifier: Modifier = Modifier,
) {
    ClassesPageBackground(modifier = modifier) {
        MyClassesScaffold(state = state, actions = actions, windowWidth = windowWidth)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MyClassesScaffold(
    state: MyClassesUiState,
    actions: MyClassesActions,
    windowWidth: WindowWidth,
) {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            ClassesTopBar(
                title = stringResource(R.string.my_classes_title),
                onBackClick = actions.onBackClick,
            )
        },
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = actions.onRefresh,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding),
        ) {
            val classes = state.classes
            val error = state.error
            when {
                classes == null && state.isLoading ->
                    LoadingState(message = stringResource(R.string.my_classes_label_loading))
                classes == null && error != null ->
                    ErrorState(
                        error = error,
                        onRetry = actions.onRetry,
                        title = stringResource(R.string.my_classes_title_error),
                        body = stringResource(error.classMessageRes()),
                    )
                classes.isNullOrEmpty() ->
                    // The mascot scrolls by itself, so pull to refresh works here too.
                    Column(modifier = Modifier.fillMaxSize()) {
                        if (error != null) {
                            OfflineBanner(
                                error = error.staleBannerError,
                                onRetry = actions.onRetry,
                                modifier = Modifier.padding(Spacing.md),
                            )
                        }
                        EmptyState(
                            title = stringResource(R.string.my_classes_title_empty),
                            body = stringResource(R.string.my_classes_body_empty),
                            modifier = Modifier.weight(1f),
                        )
                    }
                else -> ClassGrid(state = state, actions = actions, windowWidth = windowWidth)
            }
        }
    }
}

@Composable
private fun ClassGrid(
    state: MyClassesUiState,
    actions: MyClassesActions,
    windowWidth: WindowWidth,
) {
    // 16dp on every width, the same gutter as the sign-in screens.
    val horizontalPadding = Spacing.md
    LazyVerticalGrid(
        columns =
            if (windowWidth.isAtLeastMedium) {
                GridCells.Adaptive(
                    MinCardWidth,
                )
            } else {
                GridCells.Fixed(1)
            },
        contentPadding =
            PaddingValues(
                start = horizontalPadding,
                end = horizontalPadding,
                top = Spacing.sm,
                bottom = Spacing.xl,
            ),
        horizontalArrangement = Arrangement.spacedBy(Spacing.smd),
        verticalArrangement = Arrangement.spacedBy(Spacing.smd),
        modifier = Modifier.fillMaxSize(),
    ) {
        fullWidthItem(key = "intro") {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                Text(
                    text = stringResource(R.string.my_classes_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text =
                        stringResource(
                            R.string.my_classes_label_count,
                            state.classes.orEmpty().size,
                        ),
                    style = AppTextStyles.Caption,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        val error = state.error
        if (state.isStale && error != null) {
            fullWidthItem(key = "stale") {
                OfflineBanner(error = error.staleBannerError, onRetry = actions.onRetry)
            }
        }
        items(items = state.classes.orEmpty(), key = { it.id }) { studentClass ->
            ClassCard(
                studentClass = studentClass,
                onClick = { actions.onClassClick(studentClass.id) },
            )
        }
    }
}

private fun LazyGridScope.fullWidthItem(
    key: String,
    content: @Composable () -> Unit,
) = item(key = key, span = { GridItemSpan(maxLineSpan) }) { content() }

private val LoadedState = MyClassesUiState(classes = PreviewClasses, isLoading = false)

@Preview(widthDp = 390, heightDp = 860)
@Composable
private fun MyClassesPhonePreview() {
    ToeicSpaceTheme {
        MyClassesScreen(
            state = LoadedState,
            actions = MyClassesActions(),
            windowWidth = WindowWidth.COMPACT,
        )
    }
}

@Preview(widthDp = 1280, heightDp = 800)
@Composable
private fun MyClassesTabletPreview() {
    ToeicSpaceTheme {
        MyClassesScreen(
            state = LoadedState,
            actions = MyClassesActions(),
            windowWidth = WindowWidth.EXPANDED,
        )
    }
}

@Preview(widthDp = 390, heightDp = 860)
@Composable
private fun MyClassesOfflinePhonePreview() {
    ToeicSpaceTheme {
        MyClassesScreen(
            state = LoadedState.copy(error = AppError.Network),
            actions = MyClassesActions(),
            windowWidth = WindowWidth.COMPACT,
        )
    }
}

@Preview(widthDp = 1280, heightDp = 800)
@Composable
private fun MyClassesOfflineTabletPreview() {
    ToeicSpaceTheme {
        MyClassesScreen(
            state = LoadedState.copy(error = AppError.Network),
            actions = MyClassesActions(),
            windowWidth = WindowWidth.EXPANDED,
        )
    }
}

@Preview(widthDp = 390, heightDp = 700)
@Composable
private fun MyClassesLoadingPhonePreview() {
    ToeicSpaceTheme {
        MyClassesScreen(
            state = MyClassesUiState(),
            actions = MyClassesActions(),
            windowWidth = WindowWidth.COMPACT,
        )
    }
}

@Preview(widthDp = 1280, heightDp = 800)
@Composable
private fun MyClassesLoadingTabletPreview() {
    ToeicSpaceTheme {
        MyClassesScreen(
            state = MyClassesUiState(),
            actions = MyClassesActions(),
            windowWidth = WindowWidth.EXPANDED,
        )
    }
}

@Preview(widthDp = 390, heightDp = 700)
@Composable
private fun MyClassesEmptyPhonePreview() {
    ToeicSpaceTheme {
        MyClassesScreen(
            state = MyClassesUiState(classes = emptyList(), isLoading = false),
            actions = MyClassesActions(),
            windowWidth = WindowWidth.COMPACT,
        )
    }
}

@Preview(widthDp = 1280, heightDp = 800)
@Composable
private fun MyClassesEmptyTabletPreview() {
    ToeicSpaceTheme {
        MyClassesScreen(
            state = MyClassesUiState(classes = emptyList(), isLoading = false),
            actions = MyClassesActions(),
            windowWidth = WindowWidth.EXPANDED,
        )
    }
}

@Preview(widthDp = 390, heightDp = 700)
@Composable
private fun MyClassesErrorPhonePreview() {
    ToeicSpaceTheme {
        MyClassesScreen(
            state = MyClassesUiState(isLoading = false, error = AppError.ServerUnreachable),
            actions = MyClassesActions(),
            windowWidth = WindowWidth.COMPACT,
        )
    }
}

@Preview(widthDp = 1280, heightDp = 800)
@Composable
private fun MyClassesErrorTabletPreview() {
    ToeicSpaceTheme {
        MyClassesScreen(
            state = MyClassesUiState(isLoading = false, error = AppError.Network),
            actions = MyClassesActions(),
            windowWidth = WindowWidth.EXPANDED,
        )
    }
}
