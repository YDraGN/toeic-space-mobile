package com.toeicspace.android.feature.vocabulary.presentation.flashcard

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.toeicspace.android.R
import com.toeicspace.android.core.common.AppError
import com.toeicspace.android.core.ui.TestTags
import com.toeicspace.android.core.ui.WindowWidth
import com.toeicspace.android.core.ui.components.EmptyState
import com.toeicspace.android.core.ui.components.LoadingState
import com.toeicspace.android.core.ui.components.MascotView
import com.toeicspace.android.core.ui.components.OfflineBanner
import com.toeicspace.android.core.ui.components.PrimaryButton
import com.toeicspace.android.core.ui.components.SecondaryButton
import com.toeicspace.android.core.ui.components.ToeicCard
import com.toeicspace.android.core.ui.components.ToeicIconButton
import com.toeicspace.android.core.ui.currentWindowWidth
import com.toeicspace.android.core.ui.theme.AppTextStyles
import com.toeicspace.android.core.ui.theme.BlushTone
import com.toeicspace.android.core.ui.theme.CardBorder
import com.toeicspace.android.core.ui.theme.CoralBadge
import com.toeicspace.android.core.ui.theme.Dimens
import com.toeicspace.android.core.ui.theme.HintText
import com.toeicspace.android.core.ui.theme.InkSoft
import com.toeicspace.android.core.ui.theme.LilacTone
import com.toeicspace.android.core.ui.theme.MintTone
import com.toeicspace.android.core.ui.theme.PeachTone
import com.toeicspace.android.core.ui.theme.PearlPaper
import com.toeicspace.android.core.ui.theme.PillShape
import com.toeicspace.android.core.ui.theme.PlayfairDisplay
import com.toeicspace.android.core.ui.theme.QuietFill
import com.toeicspace.android.core.ui.theme.SkyTone
import com.toeicspace.android.core.ui.theme.Spacing
import com.toeicspace.android.core.ui.theme.TagTone
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme
import com.toeicspace.android.core.ui.theme.softShadow
import com.toeicspace.android.feature.vocabulary.domain.FlashcardMark
import com.toeicspace.android.feature.vocabulary.domain.VocabularyWord
import com.toeicspace.android.feature.vocabulary.presentation.components.ToneLabel
import com.toeicspace.android.feature.vocabulary.presentation.components.VocabErrorState
import com.toeicspace.android.feature.vocabulary.presentation.components.VocabularyTopBar
import com.toeicspace.android.feature.vocabulary.presentation.components.WordSpeaker
import com.toeicspace.android.feature.vocabulary.presentation.components.labelRes
import com.toeicspace.android.feature.vocabulary.presentation.components.rememberWordSpeaker
import com.toeicspace.android.feature.vocabulary.presentation.set.PreviewWords
import kotlinx.coroutines.launch

private val CardMaxWidth = 560.dp
private val WordListPaneWidth = 320.dp
private const val FLIP_DURATION_MS = 400
private const val HALF_TURN = 180f
private const val QUARTER_TURN = 90f
private const val CAMERA_DISTANCE = 12f

/** A drag past this share of the card's width sorts it; less springs it back. */
private const val SWIPE_THRESHOLD = 0.35f
private const val FLY_OUT_FACTOR = 1.5f
private const val FLY_OUT_MS = 220
private const val MAX_TILT_DEGREES = 12f
private const val STAMP_TILT_DEGREES = 12f
private const val NEXT_CARD_SCALE = 0.94f
private val NextCardDrop = 14.dp

// The notebook page: faint ink rules and a coral margin, like the journal cards on Home.
private val RuleSpacing = 32.dp
private val RuleTop = 72.dp
private val MarginLineX = 28.dp
private val RuleStroke = 1.dp
private val MarginStroke = 1.5.dp
private const val RULE_ALPHA = 0.08f
private const val MARGIN_ALPHA = 0.55f
private val WashiWidth = 104.dp
private val WashiHeight = 28.dp
private const val WASHI_TILT_DEGREES = -4f
private val MarkDotSize = 8.dp

data class FlashcardActions(
    val onBackClick: () -> Unit,
    val onRetry: () -> Unit,
    val onFlip: () -> Unit,
    val onMark: (FlashcardMark) -> Unit,
    val onPrevious: () -> Unit,
    val onCardSelect: (index: Int) -> Unit,
    val onRestart: () -> Unit,
    val onStudyLearning: () -> Unit,
)

@Composable
internal fun FlashcardRoute(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FlashcardViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    FlashcardScreen(
        state = state,
        actions =
            FlashcardActions(
                onBackClick = onBackClick,
                onRetry = viewModel::onRetry,
                onFlip = viewModel::onFlip,
                onMark = viewModel::onMark,
                onPrevious = viewModel::onPrevious,
                onCardSelect = viewModel::onCardSelect,
                onRestart = viewModel::onRestart,
                onStudyLearning = viewModel::onStudyLearning,
            ),
        speaker = rememberWordSpeaker(),
        modifier = modifier,
    )
}

/**
 * One notebook card at a time on the sign-in background: the word, its phonetics and an audio
 * button on the front; tap to flip to the meaning and an example. Swipe right when you know the
 * word, left when you are still learning it (or use the buttons); the next card waits underneath.
 * After the last card the round's summary offers the cards still being learned. Tablets in
 * landscape list the words of the set, with their marks, beside the card.
 */
@Composable
internal fun FlashcardScreen(
    state: FlashcardUiState,
    actions: FlashcardActions,
    speaker: WordSpeaker,
    modifier: Modifier = Modifier,
    windowWidth: WindowWidth = currentWindowWidth(),
) {
    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.img_splash_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                VocabularyTopBar(
                    title =
                        state.setTitle.ifBlank {
                            stringResource(
                                R.string.vocab_title_flashcards,
                            )
                        },
                    onBackClick = actions.onBackClick,
                )
            },
        ) { innerPadding ->
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .consumeWindowInsets(innerPadding),
                contentAlignment = Alignment.TopCenter,
            ) {
                val error = state.error
                when {
                    state.isLoading -> LoadingState()
                    error != null -> VocabErrorState(error = error, onRetry = actions.onRetry)
                    state.isEmpty ->
                        EmptyState(
                            title = stringResource(R.string.vocab_title_empty_set),
                            body = stringResource(R.string.vocab_body_empty_set),
                        )
                    windowWidth == WindowWidth.EXPANDED ->
                        Row(
                            modifier = Modifier.fillMaxSize().padding(horizontal = Spacing.lg),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.lg),
                        ) {
                            StudyColumn(state, actions, speaker, Modifier.weight(1f))
                            WordListPane(
                                words = state.words,
                                marks = state.marks,
                                currentIndex = if (state.isFinished) -1 else state.index,
                                onCardSelect = actions.onCardSelect,
                                modifier = Modifier.width(WordListPaneWidth).fillMaxHeight(),
                            )
                        }
                    else ->
                        StudyColumn(state, actions, speaker, Modifier.widthIn(max = CardMaxWidth))
                }
            }
        }
    }
}

@Composable
private fun StudyColumn(
    state: FlashcardUiState,
    actions: FlashcardActions,
    speaker: WordSpeaker,
    modifier: Modifier = Modifier,
) {
    // A button press asks the top card to fly off the same way a swipe does.
    var requestedMark by remember { mutableStateOf<FlashcardMark?>(null) }
    Column(
        modifier =
            modifier
                .fillMaxHeight()
                .navigationBarsPadding()
                .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        if (state.fromCache) OfflineBanner()
        ProgressHeader(state = state, onPrevious = actions.onPrevious)
        if (state.isFinished) {
            RoundSummary(state = state, actions = actions, modifier = Modifier.weight(1f))
        } else {
            CardStack(
                state = state,
                actions = actions,
                speaker = speaker,
                requestedMark = requestedMark,
                onRequestHandled = { requestedMark = null },
                modifier = Modifier.weight(1f),
            )
            Text(
                text = stringResource(R.string.vocab_hint_swipe),
                style = AppTextStyles.Caption,
                color = HintText,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            MarkControls(
                isFlipped = state.isFlipped,
                enabled = requestedMark == null,
                onMark = { requestedMark = it },
                onFlip = actions.onFlip,
            )
        }
    }
}

@Composable
private fun ProgressHeader(
    state: FlashcardUiState,
    onPrevious: () -> Unit,
) {
    val label = stringResource(R.string.vocab_label_card_position, state.position, state.words.size)
    Row(
        horizontalArrangement = Arrangement.spacedBy(Spacing.smd),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ToeicIconButton(
            onClick = onPrevious,
            enabled = state.hasPrevious,
            contentDescription = stringResource(R.string.vocab_cd_previous_card),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_chevron_left),
                contentDescription = null,
            )
        }
        Row(
            modifier =
                Modifier
                    .weight(1f)
                    .semantics(mergeDescendants = true) { contentDescription = label },
            horizontalArrangement = Arrangement.spacedBy(Spacing.smd),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LinearProgressIndicator(
                progress = { state.progress },
                modifier = Modifier.weight(1f),
                trackColor = QuietFill,
                drawStopIndicator = {},
            )
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

/** The top card, swipeable, with the next card peeking out underneath. */
@Composable
private fun CardStack(
    state: FlashcardUiState,
    actions: FlashcardActions,
    speaker: WordSpeaker,
    requestedMark: FlashcardMark?,
    onRequestHandled: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val word = state.current ?: return
    Box(modifier = modifier.fillMaxWidth()) {
        state.upcoming?.let { next ->
            NotebookPage(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(top = Spacing.smd)
                        .graphicsLayer {
                            scaleX = NEXT_CARD_SCALE
                            scaleY = NEXT_CARD_SCALE
                            translationY = NextCardDrop.toPx()
                        },
            ) {
                Text(
                    text = next.headword,
                    style = headwordStyle(),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        }
        // A new key per word gives every card its own drag offset and flip.
        key(word.id) {
            SwipeCard(
                word = word,
                mark = state.marks[word.id],
                flipped = state.isFlipped,
                requestedMark = requestedMark,
                onRequestHandled = onRequestHandled,
                onFlip = actions.onFlip,
                onMark = actions.onMark,
                onSpeak = { speaker.speak(word.headword) },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun SwipeCard(
    word: VocabularyWord,
    mark: FlashcardMark?,
    flipped: Boolean,
    requestedMark: FlashcardMark?,
    onRequestHandled: () -> Unit,
    onFlip: () -> Unit,
    onMark: (FlashcardMark) -> Unit,
    onSpeak: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    var widthPx by remember { mutableFloatStateOf(0f) }
    val currentOnMark by rememberUpdatedState(onMark)

    suspend fun flyOff(sorted: FlashcardMark) {
        val target = widthPx * FLY_OUT_FACTOR * if (sorted == FlashcardMark.KNOWN) 1f else -1f
        offsetX.animateTo(target, tween(durationMillis = FLY_OUT_MS))
        currentOnMark(sorted)
    }

    LaunchedEffect(requestedMark) {
        val sorted = requestedMark ?: return@LaunchedEffect
        flyOff(sorted)
        onRequestHandled()
    }

    val knownLabel = stringResource(R.string.vocab_action_mark_known)
    val learningLabel = stringResource(R.string.vocab_action_mark_learning)
    val threshold = widthPx * SWIPE_THRESHOLD
    Box(
        modifier =
            modifier
                .onSizeChanged { widthPx = it.width.toFloat() }
                .graphicsLayer {
                    translationX = offsetX.value
                    rotationZ =
                        if (widthPx == 0f) 0f else offsetX.value / widthPx * MAX_TILT_DEGREES
                }.pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            val x = offsetX.value
                            scope.launch {
                                when {
                                    x > threshold -> flyOff(FlashcardMark.KNOWN)
                                    x < -threshold -> flyOff(FlashcardMark.LEARNING)
                                    else -> offsetX.animateTo(0f, spring())
                                }
                            }
                        },
                        onDragCancel = { scope.launch { offsetX.animateTo(0f, spring()) } },
                    ) { change, dragAmount ->
                        change.consume()
                        scope.launch { offsetX.snapTo(offsetX.value + dragAmount) }
                    }
                }.semantics {
                    customActions =
                        listOf(
                            CustomAccessibilityAction(knownLabel) {
                                currentOnMark(FlashcardMark.KNOWN)
                                true
                            },
                            CustomAccessibilityAction(learningLabel) {
                                currentOnMark(FlashcardMark.LEARNING)
                                true
                            },
                        )
                },
    ) {
        FlipCard(
            word = word,
            mark = mark,
            flipped = flipped,
            onFlip = onFlip,
            onSpeak = onSpeak,
            modifier = Modifier.fillMaxSize().padding(top = Spacing.smd),
        )
        Image(
            painter = painterResource(R.drawable.img_washi_tape),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier =
                Modifier
                    .align(Alignment.TopCenter)
                    .size(width = WashiWidth, height = WashiHeight)
                    .rotate(WASHI_TILT_DEGREES),
        )
        val drag = if (threshold == 0f) 0f else (offsetX.value / threshold).coerceIn(-1f, 1f)
        if (drag > 0f) {
            SwipeStamp(
                text = knownLabel,
                tone = MintTone,
                alpha = drag,
                tilt = -STAMP_TILT_DEGREES,
                modifier = Modifier.align(Alignment.TopStart),
            )
        } else if (drag < 0f) {
            SwipeStamp(
                text = learningLabel,
                tone = BlushTone,
                alpha = -drag,
                tilt = STAMP_TILT_DEGREES,
                modifier = Modifier.align(Alignment.TopEnd),
            )
        }
    }
}

/** "Đã thuộc" / "Chưa thuộc" stamped on the card as it is dragged, clearer the further it goes. */
@Composable
private fun SwipeStamp(
    text: String,
    tone: TagTone,
    alpha: Float,
    tilt: Float,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = tone.content,
        modifier =
            modifier
                .padding(top = Spacing.xl, start = Spacing.lg, end = Spacing.lg)
                .graphicsLayer { this.alpha = alpha }
                .rotate(tilt)
                .background(tone.container, MaterialTheme.shapes.small)
                .border(Dimens.strokeThin, tone.content, MaterialTheme.shapes.small)
                .padding(horizontal = Spacing.smd, vertical = Spacing.xs),
    )
}

@Composable
private fun FlipCard(
    word: VocabularyWord,
    mark: FlashcardMark?,
    flipped: Boolean,
    onFlip: () -> Unit,
    onSpeak: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rotation by animateFloatAsState(
        targetValue = if (flipped) HALF_TURN else 0f,
        animationSpec = tween(durationMillis = FLIP_DURATION_MS),
        label = "FlashcardFlip",
    )
    val showBack = rotation > QUARTER_TURN
    val flipLabel = stringResource(R.string.vocab_cd_flip_card)
    val sideLabel =
        stringResource(
            if (flipped) R.string.vocab_state_card_back else R.string.vocab_state_card_front,
        )
    NotebookPage(
        modifier =
            modifier
                .graphicsLayer {
                    rotationY = rotation
                    cameraDistance = CAMERA_DISTANCE * density
                }.testTag(TestTags.VOCAB_FLASHCARD)
                .semantics { stateDescription = sideLabel }
                .clickable(onClickLabel = flipLabel, role = Role.Button, onClick = onFlip),
    ) {
        Box(
            // The back is drawn mirrored by the turn, so it is turned once more to read normally.
            modifier =
                Modifier.fillMaxSize().graphicsLayer {
                    rotationY = if (showBack) HALF_TURN else 0f
                },
            contentAlignment = Alignment.Center,
        ) {
            if (showBack) CardBack(word) else CardFront(word, mark, onSpeak)
        }
    }
}

/** A cream notebook page: faint rules, a coral margin line and a soft shadow. */
@Composable
private fun NotebookPage(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val shape = MaterialTheme.shapes.medium
    val ruleColor = InkSoft.copy(alpha = RULE_ALPHA)
    val marginColor = CoralBadge.copy(alpha = MARGIN_ALPHA)
    Surface(
        modifier = modifier.softShadow(shape),
        shape = shape,
        color = PearlPaper,
        border = BorderStroke(Dimens.borderHairline, CardBorder),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .drawBehind {
                        val step = RuleSpacing.toPx()
                        var y = RuleTop.toPx()
                        while (y < size.height) {
                            drawLine(
                                color = ruleColor,
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = RuleStroke.toPx(),
                            )
                            y += step
                        }
                        val x = MarginLineX.toPx()
                        drawLine(
                            color = marginColor,
                            start = Offset(x, 0f),
                            end = Offset(x, size.height),
                            strokeWidth = MarginStroke.toPx(),
                        )
                    },
            content = content,
        )
    }
}

@Composable
private fun headwordStyle() =
    MaterialTheme.typography.displaySmall.copy(
        fontFamily = PlayfairDisplay,
        fontWeight = FontWeight.SemiBold,
    )

@Composable
private fun CardFront(
    word: VocabularyWord,
    mark: FlashcardMark?,
    onSpeak: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(
                    start = Spacing.xl + Spacing.sm,
                    end = Spacing.lg,
                    top = Spacing.xl,
                    bottom = Spacing.lg,
                ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.smd),
    ) {
        Text(
            text = stringResource(R.string.vocab_label_new_word),
            style = AppTextStyles.HeroTagline,
            color = LilacTone.content,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ToneLabel(text = stringResource(word.partOfSpeech.labelRes()), tone = SkyTone)
            mark?.let { MarkLabel(mark = it) }
        }
        Text(
            text = word.headword,
            style = headwordStyle(),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        word.phonetic?.let {
            Text(text = it, style = MaterialTheme.typography.bodyLarge, color = HintText)
        }
        SecondaryButton(
            text = stringResource(R.string.vocab_action_listen),
            onClick = onSpeak,
            shape = PillShape,
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_volume),
                    contentDescription = null,
                    modifier = Modifier.size(Dimens.iconSm),
                )
            },
        )
        Text(
            text = stringResource(R.string.vocab_hint_tap_to_flip),
            style = AppTextStyles.Caption,
            color = HintText,
        )
    }
}

/** What the learner said last time about this card. */
@Composable
private fun MarkLabel(mark: FlashcardMark) {
    ToneLabel(
        text =
            stringResource(
                if (mark == FlashcardMark.KNOWN) {
                    R.string.vocab_label_marked_known
                } else {
                    R.string.vocab_label_marked_learning
                },
            ),
        tone = mark.tone,
    )
}

private val FlashcardMark.tone: TagTone
    get() = if (this == FlashcardMark.KNOWN) MintTone else BlushTone

@Composable
private fun CardBack(word: VocabularyWord) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(
                    start = Spacing.xl + Spacing.sm,
                    end = Spacing.lg,
                    top = Spacing.xl,
                    bottom = Spacing.lg,
                ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.smd),
    ) {
        Text(
            text = stringResource(R.string.vocab_label_meaning_script),
            style = AppTextStyles.HeroTagline,
            color = LilacTone.content,
        )
        Text(
            text = word.headword,
            style = MaterialTheme.typography.titleMedium,
            color = LilacTone.content,
        )
        Text(
            text = word.meaningVi,
            style = MaterialTheme.typography.headlineMedium.copy(fontFamily = PlayfairDisplay),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        word.definitionEn?.let {
            Text(
                text = it,
                style = AppTextStyles.ReadingBody,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        word.example?.let { example ->
            // A sticky note on the page.
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(PeachTone.container, MaterialTheme.shapes.small)
                        .padding(Spacing.smd),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                Text(
                    text = stringResource(R.string.vocab_label_example),
                    style = AppTextStyles.KickerLabel,
                    color = PeachTone.content,
                )
                Text(
                    text = example,
                    style = AppTextStyles.ReadingBody,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                word.exampleVi?.let {
                    Text(text = it, style = AppTextStyles.Caption, color = HintText)
                }
            }
        }
    }
}

/** Not yet / flip / known: the same as a swipe, for anyone who would rather tap. */
@Composable
private fun MarkControls(
    isFlipped: Boolean,
    enabled: Boolean,
    onMark: (FlashcardMark) -> Unit,
    onFlip: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SecondaryButton(
            text = stringResource(R.string.vocab_action_mark_learning),
            onClick = { onMark(FlashcardMark.LEARNING) },
            enabled = enabled,
            shape = PillShape,
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_x),
                    contentDescription = null,
                    modifier = Modifier.size(Dimens.iconSm),
                )
            },
            modifier = Modifier.weight(1f),
        )
        ToeicIconButton(
            onClick = onFlip,
            contentDescription =
                stringResource(
                    if (isFlipped) R.string.vocab_action_show_word else R.string.vocab_action_flip,
                ),
        ) {
            Icon(painter = painterResource(R.drawable.ic_rotate_cw), contentDescription = null)
        }
        PrimaryButton(
            text = stringResource(R.string.vocab_action_mark_known),
            onClick = { onMark(FlashcardMark.KNOWN) },
            enabled = enabled,
            shape = PillShape,
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_check),
                    contentDescription = null,
                    modifier = Modifier.size(Dimens.iconSm),
                )
            },
            modifier = Modifier.weight(1f).testTag(TestTags.VOCAB_FLASHCARD_NEXT),
        )
    }
}

/** After the last card: how the round went, and what to study next. */
@Composable
private fun RoundSummary(
    state: FlashcardUiState,
    actions: FlashcardActions,
    modifier: Modifier = Modifier,
) {
    val learning = state.learningWords.size
    NotebookPage(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        start = Spacing.xl + Spacing.sm,
                        end = Spacing.lg,
                        top = Spacing.lg,
                        bottom = Spacing.lg,
                    ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.smd, Alignment.CenterVertically),
        ) {
            MascotView(
                mascotResId =
                    if (learning ==
                        0
                    ) {
                        R.drawable.oy2_cheer
                    } else {
                        R.drawable.oy2_determined
                    },
                size = Dimens.mascotSm,
            )
            Text(
                text = stringResource(R.string.vocab_title_round_done),
                style = AppTextStyles.Editorial,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                ToneLabel(
                    text = stringResource(R.string.vocab_label_round_known, state.knownCount),
                    tone = MintTone,
                )
                ToneLabel(
                    text = stringResource(R.string.vocab_label_round_learning, state.learningCount),
                    tone = BlushTone,
                )
            }
            if (!state.isSignedIn) {
                Text(
                    text = stringResource(R.string.vocab_msg_marks_guest),
                    style = AppTextStyles.Caption,
                    color = HintText,
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(modifier = Modifier.size(Spacing.xs))
            if (learning > 0) {
                PrimaryButton(
                    text = stringResource(R.string.vocab_action_study_learning, learning),
                    onClick = actions.onStudyLearning,
                    shape = PillShape,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            SecondaryButton(
                text = stringResource(R.string.vocab_action_restart_all),
                onClick = actions.onRestart,
                shape = PillShape,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun WordListPane(
    words: List<VocabularyWord>,
    marks: Map<String, FlashcardMark>,
    currentIndex: Int,
    onCardSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    LaunchedEffect(currentIndex) {
        if (currentIndex >= 0) listState.animateScrollToItem(currentIndex)
    }
    ToeicCard(
        modifier = modifier.padding(vertical = Spacing.sm),
        contentPadding = PaddingValues(Spacing.sm),
    ) {
        Text(
            text = stringResource(R.string.vocab_title_words_in_set, words.size),
            style = AppTextStyles.SectionTitle,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xs),
        )
        LazyColumn(state = listState, verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            itemsIndexed(items = words, key = { _, word -> word.id }) { index, word ->
                val current = index == currentIndex
                val mark = marks[word.id]
                val markText =
                    mark?.let {
                        stringResource(
                            if (it == FlashcardMark.KNOWN) {
                                R.string.vocab_label_marked_known
                            } else {
                                R.string.vocab_label_marked_learning
                            },
                        )
                    }
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(
                                if (current) SkyTone.container else Color.Transparent,
                                MaterialTheme.shapes.extraSmall,
                            ).clickable(role = Role.Button) { onCardSelect(index) }
                            .semantics(mergeDescendants = true) {
                                markText?.let { stateDescription = it }
                            }.padding(horizontal = Spacing.sm, vertical = Spacing.sm),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.vocab_label_word_index, index + 1),
                        style = AppTextStyles.HelperText,
                        color = if (current) SkyTone.content else HintText,
                    )
                    Text(
                        text = word.headword,
                        style = AppTextStyles.ListItemTitle,
                        color =
                            if (current) SkyTone.content else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    mark?.let {
                        Box(
                            modifier =
                                Modifier
                                    .size(MarkDotSize)
                                    .background(it.tone.content, CircleShape),
                        )
                    }
                }
            }
        }
    }
}

private val PreviewActions =
    FlashcardActions(
        onBackClick = {},
        onRetry = {},
        onFlip = {},
        onMark = {},
        onPrevious = {},
        onCardSelect = {},
        onRestart = {},
        onStudyLearning = {},
    )

private val PreviewState =
    FlashcardUiState(
        isLoading = false,
        setTitle = "Bài 01",
        allWords = PreviewWords,
        words = PreviewWords,
        index = 1,
        marks = mapOf(PreviewWords[0].id to FlashcardMark.KNOWN),
        isSignedIn = true,
    )

private val PreviewSpeaker = WordSpeaker { }

@Preview(widthDp = 390, heightDp = 800)
@Composable
private fun FlashcardFrontPhonePreview() {
    ToeicSpaceTheme {
        FlashcardScreen(
            PreviewState,
            PreviewActions,
            PreviewSpeaker,
            windowWidth = WindowWidth.COMPACT,
        )
    }
}

@Preview(widthDp = 390, heightDp = 800)
@Composable
private fun FlashcardBackPhonePreview() {
    ToeicSpaceTheme {
        FlashcardScreen(
            PreviewState.copy(index = 0, isFlipped = true),
            PreviewActions,
            PreviewSpeaker,
            windowWidth = WindowWidth.COMPACT,
        )
    }
}

@Preview(widthDp = 390, heightDp = 800)
@Composable
private fun FlashcardRoundDonePhonePreview() {
    ToeicSpaceTheme {
        FlashcardScreen(
            PreviewState.copy(
                isFinished = true,
                isSignedIn = false,
                marks =
                    mapOf(
                        PreviewWords[0].id to FlashcardMark.KNOWN,
                        PreviewWords[1].id to FlashcardMark.LEARNING,
                    ),
            ),
            PreviewActions,
            PreviewSpeaker,
            windowWidth = WindowWidth.COMPACT,
        )
    }
}

@Preview(widthDp = 1280, heightDp = 800)
@Composable
private fun FlashcardTabletPreview() {
    ToeicSpaceTheme {
        FlashcardScreen(
            PreviewState,
            PreviewActions,
            PreviewSpeaker,
            windowWidth = WindowWidth.EXPANDED,
        )
    }
}

@Preview(widthDp = 390, heightDp = 700)
@Composable
private fun FlashcardLoadingPreview() {
    ToeicSpaceTheme {
        FlashcardScreen(
            FlashcardUiState(),
            PreviewActions,
            PreviewSpeaker,
            windowWidth = WindowWidth.COMPACT,
        )
    }
}

@Preview(widthDp = 900, heightDp = 700)
@Composable
private fun FlashcardErrorTabletPreview() {
    ToeicSpaceTheme {
        FlashcardScreen(
            FlashcardUiState(isLoading = false, error = AppError.ServerUnreachable),
            PreviewActions,
            PreviewSpeaker,
            windowWidth = WindowWidth.MEDIUM,
        )
    }
}

@Preview(widthDp = 390, heightDp = 700)
@Composable
private fun FlashcardEmptyPreview() {
    ToeicSpaceTheme {
        FlashcardScreen(
            FlashcardUiState(isLoading = false),
            PreviewActions,
            PreviewSpeaker,
            windowWidth = WindowWidth.COMPACT,
        )
    }
}
