package com.toeicspace.android.feature.vocabulary.presentation.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.toeicspace.android.R
import com.toeicspace.android.core.common.AppError
import com.toeicspace.android.core.ui.WindowWidth
import com.toeicspace.android.core.ui.components.EmptyState
import com.toeicspace.android.core.ui.components.LoadingState
import com.toeicspace.android.core.ui.components.OfflineBanner
import com.toeicspace.android.core.ui.components.ToeicCard
import com.toeicspace.android.core.ui.currentWindowWidth
import com.toeicspace.android.core.ui.theme.AppTextStyles
import com.toeicspace.android.core.ui.theme.CardBorder
import com.toeicspace.android.core.ui.theme.Dimens
import com.toeicspace.android.core.ui.theme.HintText
import com.toeicspace.android.core.ui.theme.QuietFill
import com.toeicspace.android.core.ui.theme.Spacing
import com.toeicspace.android.core.ui.theme.TagTone
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme
import com.toeicspace.android.feature.vocabulary.domain.SetProgress
import com.toeicspace.android.feature.vocabulary.domain.StageProgress
import com.toeicspace.android.feature.vocabulary.domain.VocabularySet
import com.toeicspace.android.feature.vocabulary.domain.VocabularyStage
import com.toeicspace.android.feature.vocabulary.presentation.components.VocabErrorState
import com.toeicspace.android.feature.vocabulary.presentation.components.stageMascot
import com.toeicspace.android.feature.vocabulary.presentation.components.stageTone

private val StageCardWidth = 220.dp
private val StageMascotSize = 56.dp
private val StagePaneWidth = 320.dp
private val MinSetCardWidth = 320.dp
private val SetNumberSize = 48.dp

data class VocabularyHomeActions(
    val onRetry: () -> Unit,
    val onRefresh: () -> Unit,
    val onStageSelect: (stageId: String) -> Unit,
    val onRetrySets: () -> Unit,
    val onSetClick: (setId: String) -> Unit,
)

/**
 * The "Từ vựng" tab: the roadmap stages, then the sets of the chosen stage with their word count
 * and, for a signed-in learner, how many words they have learned. [header] sits above the roadmap
 * (the review card); it gets the window width so it can lay itself out.
 */
@Composable
fun VocabularyTab(
    onSetClick: (setId: String) -> Unit,
    modifier: Modifier = Modifier,
    header: @Composable (WindowWidth) -> Unit = {},
    viewModel: VocabularyHomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    VocabularyHomeContent(
        state = state,
        actions =
            VocabularyHomeActions(
                onRetry = viewModel::onRetry,
                onRefresh = viewModel::onRefresh,
                onStageSelect = viewModel::onStageSelect,
                onRetrySets = viewModel::onRetrySets,
                onSetClick = onSetClick,
            ),
        windowWidth = currentWindowWidth(),
        header = header,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun VocabularyHomeContent(
    state: VocabularyHomeUiState,
    actions: VocabularyHomeActions,
    windowWidth: WindowWidth,
    modifier: Modifier = Modifier,
    header: @Composable (WindowWidth) -> Unit = {},
) {
    val error = state.error
    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = actions.onRefresh,
        modifier = modifier.fillMaxSize(),
    ) {
        when {
            state.stages.isEmpty() && state.isLoading -> LoadingState()
            state.stages.isEmpty() && error != null ->
                VocabErrorState(error = error, onRetry = actions.onRetry)
            state.stages.isEmpty() ->
                EmptyState(
                    title = stringResource(R.string.vocab_title_empty_roadmap),
                    body = stringResource(R.string.vocab_body_empty_roadmap),
                    mascotResId = R.drawable.oy2_waiting,
                )
            windowWidth == WindowWidth.EXPANDED -> TwoPaneRoadmap(state, actions, header)
            else -> SingleColumnRoadmap(state, actions, windowWidth, header)
        }
    }
}

@Composable
private fun SingleColumnRoadmap(
    state: VocabularyHomeUiState,
    actions: VocabularyHomeActions,
    windowWidth: WindowWidth,
    header: @Composable (WindowWidth) -> Unit,
) {
    LazyVerticalGrid(
        columns =
            if (windowWidth.isAtLeastMedium) {
                GridCells.Adaptive(
                    MinSetCardWidth,
                )
            } else {
                GridCells.Fixed(1)
            },
        contentPadding = PaddingValues(horizontal = Spacing.md, vertical = Spacing.smd),
        horizontalArrangement = Arrangement.spacedBy(Spacing.smd),
        verticalArrangement = Arrangement.spacedBy(Spacing.smd),
        modifier = Modifier.fillMaxSize(),
    ) {
        fullWidthItem("header") { header(windowWidth) }
        fullWidthItem("title") { RoadmapTitle() }
        if (state.isOffline) {
            fullWidthItem("offline") { OfflineBanner(onRetry = actions.onRefresh) }
        }
        fullWidthItem("stages") {
            val rowState = rememberLazyListState()
            ScrollToSelectedStage(state = state, listState = rowState, itemsBefore = 0)
            LazyRow(
                state = rowState,
                horizontalArrangement = Arrangement.spacedBy(Spacing.smd),
                contentPadding = PaddingValues(vertical = Spacing.xs),
            ) {
                items(items = state.stages, key = { it.id }) { stage ->
                    StageCard(
                        stage = stage,
                        selected = stage.id == state.selectedStageId,
                        progress = state.stageProgress[stage.id]?.takeIf { state.isSignedIn },
                        onClick = { actions.onStageSelect(stage.id) },
                        modifier = Modifier.width(StageCardWidth),
                    )
                }
            }
        }
        setsBody(state = state, actions = actions)
    }
}

/** Tablets in landscape: stages in a side column, the chosen stage's sets in a grid beside it. */
@Composable
private fun TwoPaneRoadmap(
    state: VocabularyHomeUiState,
    actions: VocabularyHomeActions,
    header: @Composable (WindowWidth) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxSize().padding(horizontal = Spacing.lg),
        horizontalArrangement = Arrangement.spacedBy(Spacing.lg),
    ) {
        val paneState = rememberLazyListState()
        // The header and the title sit above the stages in this column.
        ScrollToSelectedStage(state = state, listState = paneState, itemsBefore = 2)
        LazyColumn(
            state = paneState,
            modifier = Modifier.width(StagePaneWidth).fillMaxHeight(),
            contentPadding = PaddingValues(vertical = Spacing.smd),
            verticalArrangement = Arrangement.spacedBy(Spacing.smd),
        ) {
            item(key = "header") { header(WindowWidth.EXPANDED) }
            item(key = "title") { RoadmapTitle() }
            items(items = state.stages, key = { it.id }) { stage ->
                StageCard(
                    stage = stage,
                    selected = stage.id == state.selectedStageId,
                    progress = state.stageProgress[stage.id]?.takeIf { state.isSignedIn },
                    onClick = { actions.onStageSelect(stage.id) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(MinSetCardWidth),
            contentPadding = PaddingValues(vertical = Spacing.smd),
            horizontalArrangement = Arrangement.spacedBy(Spacing.smd),
            verticalArrangement = Arrangement.spacedBy(Spacing.smd),
            modifier = Modifier.weight(1f).fillMaxHeight(),
        ) {
            if (state.isOffline) {
                fullWidthItem("offline") { OfflineBanner(onRetry = actions.onRefresh) }
            }
            setsBody(state = state, actions = actions)
        }
    }
}

/**
 * Brings the chosen stage card into view, also when coming back to the tab. On the first frame it
 * jumps; later choices glide. [itemsBefore] counts the list items above the first stage.
 */
@Composable
private fun ScrollToSelectedStage(
    state: VocabularyHomeUiState,
    listState: LazyListState,
    itemsBefore: Int,
) {
    val index = state.stages.indexOfFirst { it.id == state.selectedStageId }
    LaunchedEffect(index) {
        if (index < 0) return@LaunchedEffect
        val target = index + itemsBefore
        val visible = listState.layoutInfo.visibleItemsInfo
        val fullyVisible =
            visible.any {
                it.index == target &&
                    it.offset >= listState.layoutInfo.viewportStartOffset &&
                    it.offset + it.size <= listState.layoutInfo.viewportEndOffset
            }
        when {
            fullyVisible -> Unit
            visible.isEmpty() -> listState.scrollToItem(target)
            else -> listState.animateScrollToItem(target)
        }
    }
}

private fun LazyGridScope.fullWidthItem(
    key: String,
    content: @Composable () -> Unit,
) = item(key = key, span = { GridItemSpan(maxLineSpan) }) { content() }

private fun LazyGridScope.setsBody(
    state: VocabularyHomeUiState,
    actions: VocabularyHomeActions,
) {
    val stage = state.selectedStage ?: return
    val tone = stageTone(stage.order)
    fullWidthItem("stage-${stage.id}") { StageIntro(stage = stage) }
    val sets = state.selectedSets ?: StageSetsState()
    val error = sets.error
    when {
        sets.sets.isEmpty() && sets.isLoading ->
            fullWidthItem("sets-loading") {
                LoadingState(fullScreen = false)
            }
        sets.sets.isEmpty() && error != null ->
            fullWidthItem("sets-error") {
                VocabErrorState(error = error, onRetry = actions.onRetrySets, fullScreen = false)
            }
        sets.sets.isEmpty() ->
            fullWidthItem("sets-empty") {
                EmptyState(
                    title = stringResource(R.string.vocab_title_empty_stage),
                    body = stringResource(R.string.vocab_body_empty_stage),
                    fullScreen = false,
                )
            }
        else ->
            items(items = sets.sets, key = { it.id }) { set ->
                SetCard(
                    set = set,
                    tone = tone,
                    progress = state.progress[set.id],
                    showProgress = state.isSignedIn,
                    onClick = { actions.onSetClick(set.id) },
                )
            }
    }
}

@Composable
private fun RoadmapTitle() {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.space2)) {
        Text(
            text = stringResource(R.string.vocab_title_roadmap),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = stringResource(R.string.vocab_body_roadmap),
            style = AppTextStyles.Caption,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * One roadmap stage: number, name, TOEIC target and size, with its own mascot. A signed-in learner
 * also sees how many of its lessons they have finished.
 */
@Composable
private fun StageCard(
    stage: VocabularyStage,
    selected: Boolean,
    progress: StageProgress?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tone = stageTone(stage.order)
    Surface(
        onClick = onClick,
        modifier = modifier.semantics { this.selected = selected },
        shape = MaterialTheme.shapes.small,
        color = if (selected) tone.container else MaterialTheme.colorScheme.surface,
        border =
            BorderStroke(
                if (selected) Dimens.strokeThin else Dimens.borderHairline,
                if (selected) tone.content else CardBorder,
            ),
    ) {
        Column(
            modifier = Modifier.padding(Spacing.smd),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                ) {
                    Text(
                        text = stringResource(R.string.vocab_label_stage_number, stage.order),
                        style = AppTextStyles.KickerLabel,
                        color = tone.content,
                    )
                    Text(
                        text = stage.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    stage.toeicTarget?.let {
                        Text(
                            text = stringResource(R.string.vocab_label_toeic_target, it),
                            style = AppTextStyles.Caption,
                            color = tone.content,
                        )
                    }
                }
                Image(
                    painter = painterResource(stageMascot(stage.order)),
                    contentDescription = null,
                    modifier = Modifier.size(StageMascotSize),
                )
            }
            Text(
                text =
                    stringResource(
                        R.string.vocab_label_stage_counts,
                        stage.setCount,
                        stage.wordCount,
                    ),
                style = AppTextStyles.Caption,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            progress?.let { StageProgressRow(progress = it, tone = tone) }
        }
    }
}

/** "3/18 bài" under a stage: lessons where every word has been studied at least once. */
@Composable
private fun StageProgressRow(
    progress: StageProgress,
    tone: TagTone,
) {
    val label =
        stringResource(
            R.string.vocab_label_stage_sets_done,
            progress.setsCompleted,
            progress.setCount,
        )
    Row(
        modifier = Modifier.semantics(mergeDescendants = true) { contentDescription = label },
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LinearProgressIndicator(
            progress = { progress.fraction },
            color = tone.content,
            trackColor = QuietFill,
            modifier = Modifier.weight(1f),
            drawStopIndicator = {},
        )
        Text(text = label, style = AppTextStyles.HelperText, color = tone.content)
    }
}

@Composable
private fun StageIntro(stage: VocabularyStage) {
    Column(
        modifier = Modifier.padding(top = Spacing.sm),
        verticalArrangement = Arrangement.spacedBy(Spacing.space2),
    ) {
        Text(
            text = stringResource(R.string.vocab_title_stage_sets, stage.order, stage.title),
            style = AppTextStyles.SectionTitle,
            color = MaterialTheme.colorScheme.onBackground,
        )
        stage.description?.let {
            Text(
                text = it,
                style = AppTextStyles.ReadingBody,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SetCard(
    set: VocabularySet,
    tone: TagTone,
    progress: SetProgress?,
    showProgress: Boolean,
    onClick: () -> Unit,
) {
    val learned = progress?.learned ?: 0
    val total = progress?.wordCount?.takeIf { it > 0 } ?: set.wordCount
    val description =
        if (showProgress) {
            stringResource(R.string.vocab_cd_set_progress, set.title, set.wordCount, learned)
        } else {
            stringResource(R.string.vocab_cd_set, set.title, set.wordCount)
        }
    ToeicCard(
        modifier =
            Modifier.fillMaxWidth().semantics(mergeDescendants = true) {
                contentDescription = description
                role = Role.Button
            },
        onClick = onClick,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.smd),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(SetNumberSize)
                        .background(tone.container, MaterialTheme.shapes.small),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.vocab_label_set_number, set.order),
                    style = MaterialTheme.typography.titleMedium,
                    color = tone.content,
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.space2),
            ) {
                Text(
                    text = set.title,
                    style = AppTextStyles.ListItemTitle,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_layers),
                        contentDescription = null,
                        tint = tone.content,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = stringResource(R.string.vocab_label_word_count, set.wordCount),
                        style = AppTextStyles.Caption,
                        color = tone.content,
                    )
                }
                set.previewText()?.let {
                    Text(
                        text = it,
                        style = AppTextStyles.HelperText,
                        color = HintText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Icon(
                painter = painterResource(R.drawable.ic_chevron_right),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (showProgress) {
            SetProgressRow(learned = learned, total = total, tone = tone)
        }
    }
}

@Composable
private fun SetProgressRow(
    learned: Int,
    total: Int,
    tone: TagTone,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LinearProgressIndicator(
            progress = { if (total == 0) 0f else learned.toFloat() / total },
            color = tone.content,
            trackColor = QuietFill,
            modifier = Modifier.weight(1f),
            drawStopIndicator = {},
        )
        Text(
            text =
                if (learned == 0) {
                    stringResource(R.string.vocab_label_not_started)
                } else {
                    stringResource(R.string.vocab_label_learned_count, learned, total)
                },
            style = AppTextStyles.HelperText,
            color = if (learned == 0) HintText else tone.content,
        )
    }
}

/** "abandon → zone", the web's "first → last" line, or the first words when there is no last. */
@Composable
private fun VocabularySet.previewText(): String? {
    val first = previewWords.firstOrNull() ?: return null
    val last = lastWord
    return if (last != null && last != first) {
        stringResource(R.string.vocab_label_set_range, first, last)
    } else {
        previewWords.joinToString(separator = ", ")
    }
}

internal val PreviewStages =
    listOf(
        VocabularyStage("s1", "Khởi động", "Từ nền tảng cho người mới bắt đầu.", 1, 250, 18, 900),
        VocabularyStage("s2", "Phản xạ", null, 2, 450, 16, 800),
        VocabularyStage("s3", "Tăng tốc", null, 3, 650, 14, 700),
        VocabularyStage("s4", "Bứt phá", null, 4, 800, 26, 1300),
        VocabularyStage("s5", "Về đích", null, 5, 990, 26, 1254),
    )

internal val PreviewSets =
    (1..6).map { order ->
        VocabularySet(
            id = "set$order",
            stageId = "s1",
            title = "Bài 0$order",
            order = order,
            wordCount = 50,
            previewWords = listOf("agenda", "attend", "budget"),
            lastWord = "venue",
        )
    }

private val PreviewActions =
    VocabularyHomeActions(onRetry = {
    }, onRefresh = {}, onStageSelect = {}, onRetrySets = {}, onSetClick = {})

private val PreviewState =
    VocabularyHomeUiState(
        isLoading = false,
        stages = PreviewStages,
        selectedStageId = "s1",
        stageSets = mapOf("s1" to StageSetsState(isLoading = false, sets = PreviewSets)),
        progress =
            mapOf(
                "set1" to SetProgress("set1", 50, 20, 30, 4),
                "set2" to SetProgress("set2", 50, 12, 0, 0),
            ),
        stageProgress =
            mapOf(
                "s1" to StageProgress("s1", 900, 120, 80, 18, 5),
                "s2" to StageProgress("s2", 800, 10, 0, 16, 0),
            ),
        isSignedIn = true,
    )

@Preview(widthDp = 390, heightDp = 900, showBackground = true)
@Composable
private fun VocabularyHomePhonePreview() {
    ToeicSpaceTheme {
        VocabularyHomeContent(
            state = PreviewState,
            actions = PreviewActions,
            windowWidth = WindowWidth.COMPACT,
        )
    }
}

@Preview(widthDp = 1280, heightDp = 800, showBackground = true)
@Composable
private fun VocabularyHomeTabletPreview() {
    ToeicSpaceTheme {
        VocabularyHomeContent(
            state = PreviewState,
            actions = PreviewActions,
            windowWidth = WindowWidth.EXPANDED,
        )
    }
}

@Preview(widthDp = 390, heightDp = 700, showBackground = true)
@Composable
private fun VocabularyHomeGuestOfflinePreview() {
    ToeicSpaceTheme {
        VocabularyHomeContent(
            state = PreviewState.copy(isSignedIn = false, progress = emptyMap(), fromCache = true),
            actions = PreviewActions,
            windowWidth = WindowWidth.COMPACT,
        )
    }
}

@Preview(widthDp = 390, heightDp = 700, showBackground = true)
@Composable
private fun VocabularyHomeLoadingPreview() {
    ToeicSpaceTheme {
        VocabularyHomeContent(
            state = VocabularyHomeUiState(),
            actions = PreviewActions,
            windowWidth = WindowWidth.COMPACT,
        )
    }
}

@Preview(widthDp = 390, heightDp = 700, showBackground = true)
@Composable
private fun VocabularyHomeErrorPreview() {
    ToeicSpaceTheme {
        VocabularyHomeContent(
            state = VocabularyHomeUiState(isLoading = false, error = AppError.Network),
            actions = PreviewActions,
            windowWidth = WindowWidth.COMPACT,
        )
    }
}

@Preview(widthDp = 900, heightDp = 700, showBackground = true)
@Composable
private fun VocabularyHomeEmptyTabletPreview() {
    ToeicSpaceTheme {
        VocabularyHomeContent(
            state = VocabularyHomeUiState(isLoading = false),
            actions = PreviewActions,
            windowWidth = WindowWidth.MEDIUM,
        )
    }
}
