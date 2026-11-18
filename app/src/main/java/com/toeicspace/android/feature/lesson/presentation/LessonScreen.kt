package com.toeicspace.android.feature.lesson.presentation

import android.content.ActivityNotFoundException
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import com.toeicspace.android.R
import com.toeicspace.android.core.ui.TestTags
import com.toeicspace.android.core.ui.WindowWidth
import com.toeicspace.android.core.ui.components.MascotMessage
import com.toeicspace.android.core.ui.components.PrimaryButton
import com.toeicspace.android.core.ui.components.SecondaryButton
import com.toeicspace.android.core.ui.currentWindowWidth
import com.toeicspace.android.core.ui.theme.AppTextStyles
import com.toeicspace.android.core.ui.theme.CardBorder
import com.toeicspace.android.core.ui.theme.HintText
import com.toeicspace.android.core.ui.theme.PillShape
import com.toeicspace.android.core.ui.theme.SoftPageGradientBrush
import com.toeicspace.android.core.ui.theme.Spacing
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme
import com.toeicspace.android.core.ui.theme.VideoBackdrop
import com.toeicspace.android.core.ui.theme.softShadow
import com.toeicspace.android.feature.enrollment.presentation.LearningLoading
import com.toeicspace.android.feature.enrollment.presentation.LearningRetry
import com.toeicspace.android.feature.enrollment.presentation.LearningTopBar
import com.toeicspace.android.feature.enrollment.presentation.learningMessageRes
import com.toeicspace.android.feature.lesson.domain.LessonAttachment
import com.toeicspace.android.feature.lesson.domain.LessonContent
import com.toeicspace.android.feature.lesson.domain.LessonVideo
import com.toeicspace.android.feature.lesson.presentation.player.FullscreenEffect
import com.toeicspace.android.feature.lesson.presentation.player.LessonPlayerViewModel
import com.toeicspace.android.feature.lesson.presentation.player.LessonVideoPlayer

private val SinglePaneMaxWidth = 760.dp
private val OutlinePaneWidth = 360.dp
private val NavBarShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)

data class LessonActions(
    val onBackClick: () -> Unit = {},
    val onRetry: () -> Unit = {},
    val onLessonSelect: (lessonId: String) -> Unit = {},
    val onVideoSelect: (index: Int) -> Unit = {},
    val onAttachmentClick: (attachmentId: String) -> Unit = {},
)

@Composable
internal fun LessonRoute(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LessonViewModel = hiltViewModel(),
    playerViewModel: LessonPlayerViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = LocalActivity.current
    val snackbarHostState = remember { SnackbarHostState() }

    val video = state.selectedVideo
    LaunchedEffect(video?.id, video?.url) { playerViewModel.setVideo(video?.id, video?.url) }

    // The player only tells it was refused; fetching the fresh link is the lesson's job.
    val expiredCount by playerViewModel.expiredCount.collectAsStateWithLifecycle()
    LaunchedEffect(expiredCount) { if (expiredCount > 0) viewModel.onMediaExpired() }

    // Leaving the app pauses; rotating (the activity is only being recreated) does not.
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        if (activity?.isChangingConfigurations != true) playerViewModel.pause()
    }

    val event = state.event
    val noBrowserText = stringResource(R.string.lesson_msg_no_browser)
    LaunchedEffect(event) {
        if (event is LessonEvent.OpenUrl) {
            if (!context.openInCustomTab(event.url)) snackbarHostState.showSnackbar(noBrowserText)
            viewModel.onEventHandled()
        }
    }

    val message = state.message
    val messageText =
        when (message) {
            null -> null
            LessonMessage.VideoFailed -> stringResource(R.string.lesson_msg_video_failed)
            is LessonMessage.AttachmentFailed -> stringResource(message.error.learningMessageRes())
        }
    LaunchedEffect(message) {
        if (messageText != null) {
            snackbarHostState.currentSnackbarData?.dismiss()
            viewModel.onMessageShown()
            snackbarHostState.showSnackbar(messageText)
        }
    }

    LessonScreen(
        state = state,
        actions =
            LessonActions(
                onBackClick = onBackClick,
                onRetry = viewModel::onRetry,
                onLessonSelect = viewModel::onLessonSelect,
                onVideoSelect = viewModel::onVideoSelect,
                onAttachmentClick = viewModel::onAttachmentClick,
            ),
        player = playerViewModel.player,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

/** False when no browser can show the link. */
private fun Context.openInCustomTab(url: String): Boolean =
    try {
        CustomTabsIntent
            .Builder()
            .setShowTitle(true)
            .build()
            .launchUrl(this, url.toUri())
        true
    } catch (_: ActivityNotFoundException) {
        false
    }

/**
 * A lesson: video on top, then the title, the text and the files. Phones end with the course
 * outline and pin "Bài trước" / "Bài sau" to the bottom; wide windows keep the outline beside the
 * lesson. Fullscreen shows the video alone, in landscape.
 */
@Composable
internal fun LessonScreen(
    state: LessonUiState,
    actions: LessonActions,
    modifier: Modifier = Modifier,
    player: Player? = null,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    var fullscreen by rememberSaveable { mutableStateOf(false) }
    val content = state.content
    val showFullscreen = fullscreen && player != null && state.selectedVideo != null

    FullscreenEffect(fullscreen = showFullscreen)
    BackHandler(enabled = showFullscreen) { fullscreen = false }

    if (player != null && showFullscreen) {
        Box(modifier = modifier.fillMaxSize().background(VideoBackdrop)) {
            LessonVideoPlayer(
                player = player,
                fullscreen = true,
                onFullscreenChange = { fullscreen = it },
                modifier = Modifier.fillMaxSize(),
            )
        }
        return
    }

    val twoPane = currentWindowWidth() == WindowWidth.EXPANDED
    Scaffold(
        modifier = modifier.background(SoftPageGradientBrush),
        containerColor = Color.Transparent,
        topBar = {
            LearningTopBar(
                title = state.courseTitle ?: stringResource(R.string.lesson_title_default),
                onBackClick = actions.onBackClick,
            )
        },
        bottomBar = {
            if (content != null &&
                !twoPane
            ) {
                LessonNavBar(state = state, onLessonSelect = actions.onLessonSelect)
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        innerPadding,
                    ).consumeWindowInsets(innerPadding),
            contentAlignment = Alignment.TopCenter,
        ) {
            val error = state.error
            when {
                state.isLocked -> LockedMessage(onBackClick = actions.onBackClick)
                content != null && twoPane ->
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = Spacing.lg),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.lg),
                    ) {
                        LessonBody(
                            state = state,
                            content = content,
                            actions = actions,
                            player = player,
                            onFullscreen = { fullscreen = true },
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            showOutline = false,
                        )
                        Column(
                            modifier =
                                Modifier
                                    .width(OutlinePaneWidth)
                                    .fillMaxHeight()
                                    .verticalScroll(rememberScrollState())
                                    .padding(top = Spacing.sm, bottom = Spacing.xl),
                            verticalArrangement = Arrangement.spacedBy(Spacing.md),
                        ) {
                            LessonNavButtons(state = state, onLessonSelect = actions.onLessonSelect)
                            Outline(state = state, actions = actions)
                        }
                    }
                content != null ->
                    LessonBody(
                        state = state,
                        content = content,
                        actions = actions,
                        player = player,
                        onFullscreen = { fullscreen = true },
                        modifier = Modifier.widthIn(max = SinglePaneMaxWidth).fillMaxSize(),
                        showOutline = true,
                    )
                state.isLoading -> LearningLoading()
                error != null ->
                    if (state.isNotFound) {
                        MascotMessage(
                            mascotResId = R.drawable.oy2_404,
                            title = stringResource(R.string.lesson_title_not_found),
                            body = stringResource(R.string.lesson_body_not_found),
                        ) {
                            SecondaryButton(
                                text = stringResource(R.string.common_action_back),
                                onClick = actions.onBackClick,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    } else {
                        LearningRetry(
                            title = stringResource(R.string.lesson_title_error),
                            body = stringResource(error.learningMessageRes()),
                            onRetry = actions.onRetry,
                        )
                    }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LessonBody(
    state: LessonUiState,
    content: LessonContent,
    actions: LessonActions,
    player: Player?,
    onFullscreen: () -> Unit,
    showOutline: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = if (showOutline) Spacing.md else 0.dp, vertical = Spacing.sm),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        if (content.videos.isNotEmpty()) {
            if (player != null) {
                LessonVideoPlayer(
                    player = player,
                    fullscreen = false,
                    onFullscreenChange = { if (it) onFullscreen() },
                    shape = MaterialTheme.shapes.medium,
                )
            }
            if (content.videos.size > 1) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {
                    content.videos.indices.forEach { index ->
                        FilterChip(
                            selected = index == state.selectedVideoIndex,
                            onClick = { actions.onVideoSelect(index) },
                            label = {
                                Text(
                                    stringResource(
                                        R.string.lesson_label_video_part,
                                        index + 1,
                                    ),
                                )
                            },
                            shape = PillShape,
                        )
                    }
                }
            }
        }
        LessonHeader(state = state, content = content)
        content.textContent?.let { LessonTextSection(markdown = it) }
        if (content.videos.isEmpty() &&
            content.textContent == null &&
            content.attachments.isEmpty()
        ) {
            Text(
                text = stringResource(R.string.lesson_body_empty),
                style = AppTextStyles.HelperText,
                color = HintText,
            )
        }
        if (content.attachments.isNotEmpty()) {
            AttachmentsSection(
                attachments = content.attachments,
                openingAttachmentId = state.openingAttachmentId,
                onAttachmentClick = actions.onAttachmentClick,
            )
        }
        if (showOutline && state.outline.isNotEmpty()) Outline(state = state, actions = actions)
    }
}

@Composable
private fun LessonHeader(
    state: LessonUiState,
    content: LessonContent,
) {
    val accessible = state.accessible
    val position = accessible.indexOfFirst { it.id == state.lessonId }
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        val moduleTitle = state.current?.moduleTitle
        val overline =
            listOfNotNull(
                moduleTitle,
                if (position >= 0) {
                    stringResource(R.string.lesson_label_position, position + 1, accessible.size)
                } else {
                    null
                },
                if (state.preview) stringResource(R.string.course_label_free_preview) else null,
            ).joinToString(" · ")
        if (overline.isNotEmpty()) {
            Text(text = overline, style = AppTextStyles.HelperText, color = HintText)
        }
        Text(
            text = content.title,
            style = AppTextStyles.CourseTitleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.semantics { heading() },
        )
    }
}

@Composable
private fun Outline(
    state: LessonUiState,
    actions: LessonActions,
) {
    LessonOutlineSection(
        outline = state.outline,
        currentLessonId = state.lessonId,
        accessibleIds = remember(state.accessible) { state.accessible.mapTo(HashSet()) { it.id } },
        onLessonClick = actions.onLessonSelect,
    )
}

@Composable
private fun LessonNavBar(
    state: LessonUiState,
    onLessonSelect: (String) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth().softShadow(NavBarShape),
        shape = NavBarShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, CardBorder),
    ) {
        LessonNavButtons(
            state = state,
            onLessonSelect = onLessonSelect,
            modifier =
                Modifier
                    .navigationBarsPadding()
                    .padding(horizontal = Spacing.lg, vertical = Spacing.smd),
        )
    }
}

/** "Bài trước" and "Bài sau", following the curriculum among the lessons this viewer can open. */
@Composable
private fun LessonNavButtons(
    state: LessonUiState,
    onLessonSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val previous = state.previous
    val next = state.next
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(Spacing.smd)) {
        SecondaryButton(
            text = stringResource(R.string.lesson_action_previous),
            onClick = { previous?.let { onLessonSelect(it.id) } },
            enabled = previous != null,
            shape = PillShape,
            modifier = Modifier.weight(1f).testTag(TestTags.LESSON_PREVIOUS),
        )
        PrimaryButton(
            text = stringResource(R.string.lesson_action_next),
            onClick = { next?.let { onLessonSelect(it.id) } },
            enabled = next != null,
            shape = PillShape,
            modifier = Modifier.weight(1f).testTag(TestTags.LESSON_NEXT),
        )
    }
}

@Composable
private fun LockedMessage(onBackClick: () -> Unit) {
    MascotMessage(
        mascotResId = R.drawable.oy2_locked,
        title = stringResource(R.string.lesson_title_locked),
        body = stringResource(R.string.lesson_body_locked),
    ) {
        SecondaryButton(
            text = stringResource(R.string.lesson_action_back_to_course),
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

private val PreviewOutline =
    listOf(
        OutlineLesson("l1", "Mô tả tranh: người và vật", "Chương 1 · Làm quen Part 1–2", 750, true),
        OutlineLesson("l2", "Câu hỏi WH- thường gặp", "Chương 1 · Làm quen Part 1–2", 1080, true),
        OutlineLesson(
            "l3",
            "Câu hỏi đuôi và câu đề nghị",
            "Chương 1 · Làm quen Part 1–2",
            1335,
            false,
        ),
        OutlineLesson("l4", "Đoán ý từ câu hỏi", "Chương 2 · Hội thoại Part 3–4", 2400, false),
    )

private val PreviewContent =
    LessonContent(
        id = "l2",
        title = "Câu hỏi WH- thường gặp",
        textContent =
            "## Mục tiêu\nNhận ra **từ để hỏi** ngay ở đầu câu.\n\n" +
                "- *Who* hỏi người\n- *When* hỏi thời gian\n- *Where* hỏi nơi chốn\n\n" +
                "> Mẹo: câu trả lời đúng hiếm khi lặp lại từ trong câu hỏi.",
        videos = listOf(LessonVideo("v1", "part2.mp4", 600, "https://example.com/v.mp4")),
        attachments =
            listOf(
                LessonAttachment(
                    "a1",
                    "Part2-wh-questions.pdf",
                    "application/pdf",
                    482_000,
                    "https://example.com/a.pdf",
                ),
            ),
    )

private val PreviewState =
    LessonUiState(
        courseId = "c1",
        lessonId = "l2",
        preview = false,
        courseTitle = "TOEIC 650+ Listening & Reading",
        outline = PreviewOutline,
        isLoading = false,
        content = PreviewContent,
    )

@Preview(widthDp = 390, heightDp = 1200)
@Composable
private fun LessonPhonePreview() {
    ToeicSpaceTheme { LessonScreen(state = PreviewState, actions = LessonActions()) }
}

@Preview(widthDp = 1280, heightDp = 800)
@Composable
private fun LessonTabletPreview() {
    ToeicSpaceTheme { LessonScreen(state = PreviewState, actions = LessonActions()) }
}

@Preview(widthDp = 390, heightDp = 700)
@Composable
private fun LessonLockedPreview() {
    ToeicSpaceTheme {
        LessonScreen(
            state = PreviewState.copy(lessonId = "l3", preview = true, content = null),
            actions = LessonActions(),
        )
    }
}
