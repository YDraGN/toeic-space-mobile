package com.toeicspace.android.feature.classes.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.toeicspace.android.R
import com.toeicspace.android.core.common.AppError
import com.toeicspace.android.core.ui.WindowWidth
import com.toeicspace.android.core.ui.components.ErrorState
import com.toeicspace.android.core.ui.components.LoadingState
import com.toeicspace.android.core.ui.components.OfflineBanner
import com.toeicspace.android.core.ui.components.SecondaryButton
import com.toeicspace.android.core.ui.components.ToeicCard
import com.toeicspace.android.core.ui.components.ToeicListDivider
import com.toeicspace.android.core.ui.components.ToeicListRow
import com.toeicspace.android.core.ui.components.ToeicListSection
import com.toeicspace.android.core.ui.currentWindowWidth
import com.toeicspace.android.core.ui.theme.AppTextStyles
import com.toeicspace.android.core.ui.theme.BlushTone
import com.toeicspace.android.core.ui.theme.LilacTone
import com.toeicspace.android.core.ui.theme.MintTone
import com.toeicspace.android.core.ui.theme.SkyTone
import com.toeicspace.android.core.ui.theme.SoftPageGradientBrush
import com.toeicspace.android.core.ui.theme.Spacing
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme
import com.toeicspace.android.feature.classes.domain.StudentClass
import com.toeicspace.android.feature.classes.domain.StudentClassStatus
import com.toeicspace.android.feature.classes.presentation.components.ClassCodeText
import com.toeicspace.android.feature.classes.presentation.components.ClassStatusTag
import com.toeicspace.android.feature.classes.presentation.components.ClassesTopBar
import com.toeicspace.android.feature.classes.presentation.components.PreviewClasses
import com.toeicspace.android.feature.classes.presentation.components.classDateText
import com.toeicspace.android.feature.classes.presentation.components.classMessageRes
import com.toeicspace.android.feature.classes.presentation.components.staleBannerError
import com.toeicspace.android.feature.classes.presentation.components.teacherText

private val SidePaneWidth = 360.dp
private val NoteIconSize = 16.dp

data class MyClassDetailActions(
    val onBackClick: () -> Unit = {},
    val onRefresh: () -> Unit = {},
    val onRetry: () -> Unit = {},
)

@Composable
internal fun MyClassDetailRoute(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MyClassDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    MyClassDetailScreen(
        state = state,
        actions =
            MyClassDetailActions(
                onBackClick = onBackClick,
                onRefresh = viewModel::onRefresh,
                onRetry = viewModel::onRetry,
            ),
        windowWidth = currentWindowWidth(),
        modifier = modifier,
    )
}

/**
 * One class: code, name and status on top, then the teacher and dates and the description. Tablets
 * in landscape put the facts in a side column. Classroom has no schedule or sessions yet, so there
 * is no timetable section.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MyClassDetailScreen(
    state: MyClassDetailUiState,
    actions: MyClassDetailActions,
    windowWidth: WindowWidth,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.background(SoftPageGradientBrush),
        containerColor = Color.Transparent,
        topBar = {
            ClassesTopBar(
                title = stringResource(R.string.my_classes_title_detail),
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
            val studentClass = state.studentClass
            val error = state.error
            when {
                studentClass != null && windowWidth == WindowWidth.EXPANDED ->
                    TwoPaneContent(studentClass = studentClass, state = state, actions = actions)
                studentClass != null ->
                    SinglePaneContent(
                        studentClass = studentClass,
                        state = state,
                        actions = actions,
                        wide = windowWidth.isAtLeastMedium,
                    )
                state.isLoading ->
                    LoadingState(message = stringResource(R.string.my_classes_label_loading))
                error != null && state.isNotFound ->
                    ErrorState(
                        error = error,
                        onRetry = null,
                        title = stringResource(R.string.my_classes_title_not_found),
                        body = stringResource(error.classMessageRes()),
                    ) {
                        SecondaryButton(
                            text = stringResource(R.string.my_classes_action_back_to_list),
                            onClick = actions.onBackClick,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                error != null ->
                    ErrorState(
                        error = error,
                        onRetry = actions.onRetry,
                        title = stringResource(R.string.my_classes_title_error),
                        body = stringResource(error.classMessageRes()),
                    )
            }
        }
    }
}

@Composable
private fun SinglePaneContent(
    studentClass: StudentClass,
    state: MyClassDetailUiState,
    actions: MyClassDetailActions,
    wide: Boolean,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding =
            PaddingValues(
                start = if (wide) Spacing.lg else Spacing.md,
                end = if (wide) Spacing.lg else Spacing.md,
                top = Spacing.sm,
                bottom = Spacing.xl,
            ),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        staleBanner(state = state, onRetry = actions.onRetry)
        item(key = "hero") { HeroCard(studentClass = studentClass) }
        item(key = "facts") { FactsSection(studentClass = studentClass) }
        item(key = "description") { DescriptionCard(description = studentClass.description) }
    }
}

/** Tablets in landscape: the class on the left, its facts in a column on the right. */
@Composable
private fun TwoPaneContent(
    studentClass: StudentClass,
    state: MyClassDetailUiState,
    actions: MyClassDetailActions,
) {
    Row(
        modifier = Modifier.fillMaxSize().padding(horizontal = Spacing.lg),
        horizontalArrangement = Arrangement.spacedBy(Spacing.lg),
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            contentPadding = PaddingValues(top = Spacing.sm, bottom = Spacing.xl),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            staleBanner(state = state, onRetry = actions.onRetry)
            item(key = "hero") { HeroCard(studentClass = studentClass) }
            item(key = "description") { DescriptionCard(description = studentClass.description) }
        }
        LazyColumn(
            modifier = Modifier.width(SidePaneWidth).fillMaxHeight(),
            contentPadding = PaddingValues(top = Spacing.sm, bottom = Spacing.xl),
        ) {
            item(key = "facts") { FactsSection(studentClass = studentClass) }
        }
    }
}

private fun LazyListScope.staleBanner(
    state: MyClassDetailUiState,
    onRetry: () -> Unit,
) {
    val error = state.error
    if (state.isStale && error != null) {
        item(key = "stale") { OfflineBanner(error = error.staleBannerError, onRetry = onRetry) }
    }
}

@Composable
private fun HeroCard(studentClass: StudentClass) {
    ToeicCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(Spacing.lg)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ClassCodeText(code = studentClass.code, modifier = Modifier.weight(1f))
            studentClass.status?.let { ClassStatusTag(status = it) }
        }
        Text(
            text = studentClass.name,
            style = AppTextStyles.CourseTitleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (studentClass.isReadOnly) ReadOnlyNote(status = studentClass.status)
    }
}

/** Completed and archived classes are kept for reference only, as the web page says. */
@Composable
private fun ReadOnlyNote(status: StudentClassStatus?) {
    val tone = if (status == StudentClassStatus.ARCHIVED) LilacTone else SkyTone
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(tone.container, MaterialTheme.shapes.small)
                .padding(horizontal = Spacing.smd, vertical = Spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_lock),
            contentDescription = null,
            tint = tone.content,
            modifier = Modifier.size(NoteIconSize),
        )
        Text(
            text =
                stringResource(
                    if (status == StudentClassStatus.ARCHIVED) {
                        R.string.my_classes_note_archived
                    } else {
                        R.string.my_classes_note_completed
                    },
                ),
            style = AppTextStyles.Caption,
            color = tone.content,
        )
    }
}

@Composable
private fun FactsSection(studentClass: StudentClass) {
    ToeicListSection(title = stringResource(R.string.my_classes_section_info)) {
        ToeicListRow(
            iconRes = R.drawable.ic_account,
            overline = stringResource(R.string.my_classes_label_teacher),
            label = studentClass.teacherText(),
        )
        ToeicListDivider()
        ToeicListRow(
            iconRes = R.drawable.ic_calendar,
            overline = stringResource(R.string.my_classes_label_start),
            label = studentClass.startDate.classDateText(),
            iconBackground = MintTone.container,
            iconTint = MintTone.content,
        )
        ToeicListDivider()
        ToeicListRow(
            iconRes = R.drawable.ic_clock,
            overline = stringResource(R.string.my_classes_label_end),
            label = studentClass.endDate.classDateText(),
            iconBackground = BlushTone.container,
            iconTint = BlushTone.content,
        )
    }
}

@Composable
private fun DescriptionCard(description: String?) {
    ToeicCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.my_classes_label_description),
            style = AppTextStyles.SectionTitle,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = description ?: stringResource(R.string.my_classes_value_no_description),
            style = AppTextStyles.ReadingBody,
            color =
                if (description != null) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
        )
    }
}

private val LoadedState =
    MyClassDetailUiState(studentClass = PreviewClasses.first(), isLoading = false)

private val CompletedState =
    MyClassDetailUiState(
        studentClass = PreviewClasses[2],
        isLoading = false,
        error = AppError.Network,
    )

private val NotFoundState =
    MyClassDetailUiState(
        isLoading = false,
        error = AppError.Server(code = "CLASS_NOT_FOUND", status = 404),
    )

@Preview(widthDp = 390, heightDp = 860)
@Composable
private fun MyClassDetailPhonePreview() {
    ToeicSpaceTheme {
        MyClassDetailScreen(LoadedState, MyClassDetailActions(), WindowWidth.COMPACT)
    }
}

@Preview(widthDp = 1280, heightDp = 800)
@Composable
private fun MyClassDetailTabletPreview() {
    ToeicSpaceTheme {
        MyClassDetailScreen(LoadedState, MyClassDetailActions(), WindowWidth.EXPANDED)
    }
}

@Preview(widthDp = 390, heightDp = 860)
@Composable
private fun MyClassDetailOfflinePhonePreview() {
    ToeicSpaceTheme {
        MyClassDetailScreen(CompletedState, MyClassDetailActions(), WindowWidth.COMPACT)
    }
}

@Preview(widthDp = 1280, heightDp = 800)
@Composable
private fun MyClassDetailOfflineTabletPreview() {
    ToeicSpaceTheme {
        MyClassDetailScreen(CompletedState, MyClassDetailActions(), WindowWidth.EXPANDED)
    }
}

@Preview(widthDp = 390, heightDp = 700)
@Composable
private fun MyClassDetailLoadingPhonePreview() {
    ToeicSpaceTheme {
        MyClassDetailScreen(MyClassDetailUiState(), MyClassDetailActions(), WindowWidth.COMPACT)
    }
}

@Preview(widthDp = 1280, heightDp = 800)
@Composable
private fun MyClassDetailLoadingTabletPreview() {
    ToeicSpaceTheme {
        MyClassDetailScreen(MyClassDetailUiState(), MyClassDetailActions(), WindowWidth.EXPANDED)
    }
}

@Preview(widthDp = 390, heightDp = 700)
@Composable
private fun MyClassDetailNotFoundPhonePreview() {
    ToeicSpaceTheme {
        MyClassDetailScreen(NotFoundState, MyClassDetailActions(), WindowWidth.COMPACT)
    }
}

@Preview(widthDp = 1280, heightDp = 800)
@Composable
private fun MyClassDetailErrorTabletPreview() {
    ToeicSpaceTheme {
        MyClassDetailScreen(
            MyClassDetailUiState(isLoading = false, error = AppError.ServerUnreachable),
            MyClassDetailActions(),
            WindowWidth.EXPANDED,
        )
    }
}
