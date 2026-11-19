package com.toeicspace.android.feature.vocabulary.presentation.review

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
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
import com.toeicspace.android.core.ui.components.PrimaryButton
import com.toeicspace.android.core.ui.components.SecondaryButton
import com.toeicspace.android.core.ui.components.ToeicCard
import com.toeicspace.android.core.ui.components.ToeicIconButton
import com.toeicspace.android.core.ui.currentWindowWidth
import com.toeicspace.android.core.ui.theme.AppTextStyles
import com.toeicspace.android.core.ui.theme.BlushTone
import com.toeicspace.android.core.ui.theme.CardBorder
import com.toeicspace.android.core.ui.theme.HintText
import com.toeicspace.android.core.ui.theme.MintTone
import com.toeicspace.android.core.ui.theme.PillShape
import com.toeicspace.android.core.ui.theme.QuietFill
import com.toeicspace.android.core.ui.theme.SkyTone
import com.toeicspace.android.core.ui.theme.SoftPageGradientBrush
import com.toeicspace.android.core.ui.theme.Spacing
import com.toeicspace.android.core.ui.theme.TagTone
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme
import com.toeicspace.android.feature.vocabulary.domain.PartOfSpeech
import com.toeicspace.android.feature.vocabulary.domain.QuizOption
import com.toeicspace.android.feature.vocabulary.domain.QuizQuestion
import com.toeicspace.android.feature.vocabulary.domain.ReviewGrade
import com.toeicspace.android.feature.vocabulary.domain.ReviewResult
import com.toeicspace.android.feature.vocabulary.domain.ReviewSession
import com.toeicspace.android.feature.vocabulary.domain.ReviewSource
import com.toeicspace.android.feature.vocabulary.domain.VocabularyWord
import com.toeicspace.android.feature.vocabulary.presentation.components.ToneLabel
import com.toeicspace.android.feature.vocabulary.presentation.components.VocabErrorState
import com.toeicspace.android.feature.vocabulary.presentation.components.VocabOfflineNote
import com.toeicspace.android.feature.vocabulary.presentation.components.VocabularyTopBar
import com.toeicspace.android.feature.vocabulary.presentation.components.WordSpeaker
import com.toeicspace.android.feature.vocabulary.presentation.components.labelRes
import com.toeicspace.android.feature.vocabulary.presentation.components.rememberWordSpeaker

private val ColumnMaxWidth = 640.dp
private val ResultMascotSize = 140.dp
private const val GOOD_SCORE_PERCENT = 80
private const val PERCENT = 100

data class ReviewSessionActions(
    val onBackClick: () -> Unit,
    val onRetry: () -> Unit,
    val onOptionSelect: (wordId: String) -> Unit,
    val onContinue: () -> Unit,
    val onReviewWrongWords: () -> Unit,
)

@Composable
internal fun ReviewSessionRoute(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReviewSessionViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ReviewSessionScreen(
        state = state,
        actions =
            ReviewSessionActions(
                onBackClick = onBackClick,
                onRetry = viewModel::onRetry,
                onOptionSelect = viewModel::onOptionSelect,
                onContinue = viewModel::onContinue,
                onReviewWrongWords = viewModel::onReviewWrongWords,
            ),
        speaker = rememberWordSpeaker(),
        modifier = modifier,
    )
}

/**
 * One question at a time: a Vietnamese meaning and the words to choose from. Right or wrong shows
 * at once, then the learner moves on. Leaving keeps the session; it continues from the same card.
 * At the end: the score, the words answered wrong and a way to review just those.
 */
@Composable
internal fun ReviewSessionScreen(
    state: ReviewSessionUiState,
    actions: ReviewSessionActions,
    speaker: WordSpeaker,
    modifier: Modifier = Modifier,
    windowWidth: WindowWidth = currentWindowWidth(),
) {
    Scaffold(
        modifier = modifier.background(SoftPageGradientBrush),
        containerColor = Color.Transparent,
        topBar = {
            VocabularyTopBar(
                title = stringResource(state.source.titleRes()),
                onBackClick = actions.onBackClick,
            )
        },
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
            val result = state.result
            when {
                state.isLoading -> LoadingState()
                state.isEmpty ->
                    EmptyState(
                        title = stringResource(R.string.vocab_title_review_empty),
                        body = stringResource(R.string.vocab_body_review_empty),
                        mascotResId = R.drawable.oy2_success,
                        actionText = stringResource(R.string.vocab_action_back_to_vocabulary),
                        onAction = actions.onBackClick,
                    )
                error != null -> VocabErrorState(error = error, onRetry = actions.onRetry)
                result != null -> ResultContent(state, result, actions, windowWidth)
                state.isCompleting -> LoadingState()
                state.question != null -> QuestionContent(state, actions, speaker, windowWidth)
            }
        }
    }
}

private fun ReviewSource.titleRes(): Int =
    when (this) {
        ReviewSource.DUE -> R.string.vocab_title_review_due
        ReviewSource.MISTAKES -> R.string.vocab_title_review_mistakes
        ReviewSource.WORDS -> R.string.vocab_title_review_wrong_words
    }

@Composable
private fun QuestionContent(
    state: ReviewSessionUiState,
    actions: ReviewSessionActions,
    speaker: WordSpeaker,
    windowWidth: WindowWidth,
) {
    val question = state.question ?: return
    val header: @Composable () -> Unit = {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.smd)) {
            if (!state.isOnline) {
                VocabOfflineNote(messageRes = R.string.vocab_msg_review_offline)
            }
            ProgressRow(
                number = state.questionNumber,
                total = state.total,
                progress = state.progress,
            )
            PromptCard(question = question)
        }
    }
    val answers: @Composable () -> Unit = {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            question.options.forEach { option ->
                OptionButton(
                    option = option,
                    state = option.stateFor(question, state.feedback),
                    onClick = { actions.onOptionSelect(option.wordId) },
                )
            }
            state.feedback?.let { feedback ->
                FeedbackCard(
                    feedback = feedback,
                    word = state.questionWord,
                    isLast = state.session?.isFinished == true,
                    onSpeak = { state.questionWord?.let { speaker.speak(it.headword) } },
                    onContinue = actions.onContinue,
                )
            }
        }
    }
    if (windowWidth == WindowWidth.EXPANDED) {
        // Tablets in landscape: the meaning on the left, the choices beside it.
        Row(
            modifier =
                Modifier.fillMaxSize().padding(
                    horizontal = Spacing.lg,
                    vertical = Spacing.sm,
                ),
            horizontalArrangement = Arrangement.spacedBy(Spacing.lg),
        ) {
            Box(
                modifier =
                    Modifier
                        .weight(
                            1f,
                        ).fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
            ) {
                header()
            }
            Box(
                modifier =
                    Modifier
                        .weight(
                            1f,
                        ).fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
            ) {
                answers()
            }
        }
    } else {
        Column(
            modifier =
                Modifier
                    .widthIn(max = ColumnMaxWidth)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(horizontal = Spacing.md, vertical = Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            header()
            answers()
        }
    }
}

@Composable
private fun ProgressRow(
    number: Int,
    total: Int,
    progress: Float,
) {
    val label = stringResource(R.string.vocab_label_card_position, number, total)
    Row(
        horizontalArrangement = Arrangement.spacedBy(Spacing.smd),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.semantics(mergeDescendants = true) { contentDescription = label },
    ) {
        LinearProgressIndicator(
            progress = { progress },
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

@Composable
private fun PromptCard(question: QuizQuestion) {
    ToeicCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(Spacing.lg)) {
        Text(
            text = stringResource(R.string.vocab_label_pick_word),
            style = AppTextStyles.KickerLabel,
            color = HintText,
        )
        ToneLabel(text = stringResource(question.partOfSpeech.labelRes()), tone = SkyTone)
        Text(
            text = question.prompt,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

private enum class OptionState { IDLE, CORRECT, WRONG, DIMMED }

private fun QuizOption.stateFor(
    question: QuizQuestion,
    feedback: AnswerFeedback?,
): OptionState =
    when {
        feedback == null -> OptionState.IDLE
        wordId == question.wordId -> OptionState.CORRECT
        wordId == feedback.chosenWordId -> OptionState.WRONG
        else -> OptionState.DIMMED
    }

@Composable
private fun OptionButton(
    option: QuizOption,
    state: OptionState,
    onClick: () -> Unit,
) {
    val tone: TagTone? =
        when (state) {
            OptionState.CORRECT -> MintTone
            OptionState.WRONG -> BlushTone
            else -> null
        }
    val stateLabel =
        when (state) {
            OptionState.CORRECT -> stringResource(R.string.vocab_state_option_correct)
            OptionState.WRONG -> stringResource(R.string.vocab_state_option_wrong)
            else -> null
        }
    Surface(
        onClick = onClick,
        enabled = state == OptionState.IDLE,
        modifier =
            Modifier
                .fillMaxWidth()
                .testTag(TestTags.VOCAB_REVIEW_OPTION)
                .semantics { stateLabel?.let { contentDescription = "${option.headword}, $it" } },
        shape = MaterialTheme.shapes.small,
        color = tone?.container ?: MaterialTheme.colorScheme.surface,
        border = BorderStroke(if (tone != null) 2.dp else 1.dp, tone?.content ?: CardBorder),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.smd),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = option.headword,
                style = MaterialTheme.typography.titleMedium,
                color =
                    when (state) {
                        OptionState.DIMMED -> HintText
                        else -> tone?.content ?: MaterialTheme.colorScheme.onSurface
                    },
                modifier = Modifier.weight(1f),
            )
            if (tone != null) {
                Icon(
                    painter =
                        painterResource(
                            if (state ==
                                OptionState.CORRECT
                            ) {
                                R.drawable.ic_check
                            } else {
                                R.drawable.ic_x
                            },
                        ),
                    contentDescription = null,
                    tint = tone.content,
                )
            }
        }
    }
}

@Composable
private fun FeedbackCard(
    feedback: AnswerFeedback,
    word: VocabularyWord?,
    isLast: Boolean,
    onSpeak: () -> Unit,
    onContinue: () -> Unit,
) {
    val tone = if (feedback.isCorrect) MintTone else BlushTone
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(tone.container, MaterialTheme.shapes.small)
                .padding(Spacing.md)
                .semantics { liveRegion = LiveRegionMode.Polite },
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        Text(
            text =
                stringResource(
                    if (feedback.isCorrect) {
                        R.string.vocab_msg_answer_correct
                    } else {
                        R.string.vocab_msg_answer_wrong
                    },
                ),
            style = MaterialTheme.typography.titleMedium,
            color = tone.content,
        )
        if (word != null) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = word.headword,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    word.phonetic?.let {
                        Text(
                            text = it,
                            style = AppTextStyles.Caption,
                            color = HintText,
                        )
                    }
                }
                ToeicIconButton(
                    onClick = onSpeak,
                    contentDescription =
                        stringResource(
                            R.string.vocab_cd_play_audio,
                            word.headword,
                        ),
                ) {
                    Icon(painter = painterResource(R.drawable.ic_volume), contentDescription = null)
                }
            }
            word.example?.let {
                Text(
                    text = it,
                    style = AppTextStyles.ReadingBody,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        PrimaryButton(
            text =
                stringResource(
                    if (isLast) {
                        R.string.vocab_action_see_result
                    } else {
                        R.string.vocab_action_next_question
                    },
                ),
            onClick = onContinue,
            shape = PillShape,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun ResultContent(
    state: ReviewSessionUiState,
    result: ReviewResult,
    actions: ReviewSessionActions,
    windowWidth: WindowWidth,
) {
    val answered = result.remembered + result.needReview
    val percent = if (answered == 0) 0 else result.remembered * PERCENT / answered
    val score: @Composable () -> Unit = {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            if (state.resultWaitingForNetwork) {
                VocabOfflineNote(messageRes = R.string.vocab_msg_result_waiting)
            }
            Image(
                painter =
                    painterResource(
                        if (percent >=
                            GOOD_SCORE_PERCENT
                        ) {
                            R.drawable.oy2_cheer
                        } else {
                            R.drawable.oy2_determined
                        },
                    ),
                contentDescription = null,
                modifier = Modifier.size(ResultMascotSize),
            )
            Text(
                text = stringResource(R.string.vocab_title_review_result),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.vocab_label_score, result.remembered, answered),
                style = AppTextStyles.PriceLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text =
                    stringResource(
                        R.string.vocab_label_result_counts,
                        result.remembered,
                        result.needReview,
                    ),
                style = AppTextStyles.Caption,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            if (result.wordIds.isNotEmpty()) {
                PrimaryButton(
                    text = stringResource(R.string.vocab_action_review_wrong_words),
                    onClick = actions.onReviewWrongWords,
                    shape = PillShape,
                    enabled = !state.resultWaitingForNetwork,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            SecondaryButton(
                text = stringResource(R.string.vocab_action_back_to_vocabulary),
                onClick = actions.onBackClick,
                shape = PillShape,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
    val wrong: @Composable () -> Unit = { WrongWords(words = state.wrongWords) }
    if (windowWidth == WindowWidth.EXPANDED) {
        Row(
            modifier =
                Modifier.fillMaxSize().padding(
                    horizontal = Spacing.lg,
                    vertical = Spacing.sm,
                ),
            horizontalArrangement = Arrangement.spacedBy(Spacing.lg),
        ) {
            Box(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) { score() }
            Box(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) { wrong() }
        }
    } else {
        Column(
            modifier =
                Modifier
                    .widthIn(max = ColumnMaxWidth)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(horizontal = Spacing.md, vertical = Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            score()
            wrong()
        }
    }
}

@Composable
private fun WrongWords(words: List<VocabularyWord>) {
    if (words.isEmpty()) return
    ToeicCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.vocab_title_wrong_words, words.size),
            style = AppTextStyles.SectionTitle,
            color = MaterialTheme.colorScheme.onSurface,
        )
        words.forEach { word ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_x),
                    contentDescription = null,
                    tint = BlushTone.content,
                    modifier = Modifier.size(16.dp),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = word.headword,
                        style = AppTextStyles.ListItemTitle,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = word.meaningVi,
                        style = AppTextStyles.Caption,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

private val PreviewWordsById =
    listOf(
        VocabularyWord(
            "w1",
            "agenda",
            "/əˈdʒendə/",
            PartOfSpeech.NOUN,
            "chương trình nghị sự",
            null,
            "The agenda is ready.",
            null,
        ),
        VocabularyWord("w2", "attend", "/əˈtend/", PartOfSpeech.VERB, "tham dự", null, null, null),
        VocabularyWord(
            "w3",
            "budget",
            "/ˈbʌdʒɪt/",
            PartOfSpeech.NOUN,
            "ngân sách",
            null,
            null,
            null,
        ),
        VocabularyWord("w4", "venue", "/ˈvenjuː/", PartOfSpeech.NOUN, "địa điểm", null, null, null),
    ).associateBy { it.id }

private val PreviewQuestion =
    QuizQuestion(
        wordId = "w1",
        prompt = "chương trình nghị sự",
        partOfSpeech = PartOfSpeech.NOUN,
        options = PreviewWordsById.values.map { QuizOption(it.id, it.headword) },
    )

private val PreviewSession =
    ReviewSession(
        id = "r1",
        source = ReviewSource.DUE,
        queue = listOf("w1", "w2", "w3", "w4"),
        position = 0,
        results = emptyMap(),
        correctCount = 0,
        reviewCount = 0,
        words = PreviewWordsById,
        questions = PreviewWordsById.keys.associateWith { PreviewQuestion.copy(wordId = it) },
    )

private val PreviewActions = ReviewSessionActions({}, {}, {}, {}, {})
private val PreviewSpeaker = WordSpeaker { }

private val PreviewQuestionState = ReviewSessionUiState(isLoading = false, session = PreviewSession)

private val PreviewWrongState =
    PreviewQuestionState.copy(
        session = PreviewSession.answered("w1", ReviewGrade.FORGET),
        feedback = AnswerFeedback(question = PreviewQuestion, chosenWordId = "w3"),
        isOnline = false,
    )

private val PreviewResultState =
    PreviewQuestionState.copy(
        result = ReviewResult(remembered = 2, needReview = 2, wordIds = listOf("w1", "w3")),
    )

@Preview(widthDp = 390, heightDp = 800)
@Composable
private fun ReviewQuestionPhonePreview() {
    ToeicSpaceTheme {
        ReviewSessionScreen(
            PreviewQuestionState,
            PreviewActions,
            PreviewSpeaker,
            windowWidth = WindowWidth.COMPACT,
        )
    }
}

@Preview(widthDp = 390, heightDp = 900)
@Composable
private fun ReviewWrongAnswerPhonePreview() {
    ToeicSpaceTheme {
        ReviewSessionScreen(
            PreviewWrongState,
            PreviewActions,
            PreviewSpeaker,
            windowWidth = WindowWidth.COMPACT,
        )
    }
}

@Preview(widthDp = 1280, heightDp = 800)
@Composable
private fun ReviewQuestionTabletPreview() {
    ToeicSpaceTheme {
        ReviewSessionScreen(
            PreviewWrongState,
            PreviewActions,
            PreviewSpeaker,
            windowWidth = WindowWidth.EXPANDED,
        )
    }
}

@Preview(widthDp = 390, heightDp = 900)
@Composable
private fun ReviewResultPhonePreview() {
    ToeicSpaceTheme {
        ReviewSessionScreen(
            PreviewResultState,
            PreviewActions,
            PreviewSpeaker,
            windowWidth = WindowWidth.COMPACT,
        )
    }
}

@Preview(widthDp = 1280, heightDp = 800)
@Composable
private fun ReviewResultTabletPreview() {
    ToeicSpaceTheme {
        ReviewSessionScreen(
            PreviewResultState.copy(resultWaitingForNetwork = true),
            PreviewActions,
            PreviewSpeaker,
            windowWidth = WindowWidth.EXPANDED,
        )
    }
}

@Preview(widthDp = 390, heightDp = 700)
@Composable
private fun ReviewEmptyPreview() {
    ToeicSpaceTheme {
        ReviewSessionScreen(
            ReviewSessionUiState(isLoading = false, isEmpty = true),
            PreviewActions,
            PreviewSpeaker,
            windowWidth = WindowWidth.COMPACT,
        )
    }
}

@Preview(widthDp = 900, heightDp = 700)
@Composable
private fun ReviewErrorTabletPreview() {
    ToeicSpaceTheme {
        ReviewSessionScreen(
            ReviewSessionUiState(isLoading = false, error = AppError.Network),
            PreviewActions,
            PreviewSpeaker,
            windowWidth = WindowWidth.MEDIUM,
        )
    }
}

@Preview(widthDp = 390, heightDp = 700)
@Composable
private fun ReviewLoadingPreview() {
    ToeicSpaceTheme {
        ReviewSessionScreen(
            ReviewSessionUiState(),
            PreviewActions,
            PreviewSpeaker,
            windowWidth = WindowWidth.COMPACT,
        )
    }
}
