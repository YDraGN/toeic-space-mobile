package com.toeicspace.android.feature.vocabulary.presentation.set

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
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
import com.toeicspace.android.core.ui.components.OfflineBanner
import com.toeicspace.android.core.ui.components.PrimaryButton
import com.toeicspace.android.core.ui.components.SecondaryButton
import com.toeicspace.android.core.ui.components.ToeicCard
import com.toeicspace.android.core.ui.components.ToeicIconButton
import com.toeicspace.android.core.ui.currentWindowWidth
import com.toeicspace.android.core.ui.theme.AppTextStyles
import com.toeicspace.android.core.ui.theme.CardBorder
import com.toeicspace.android.core.ui.theme.HintText
import com.toeicspace.android.core.ui.theme.PillShape
import com.toeicspace.android.core.ui.theme.SoftPageGradientBrush
import com.toeicspace.android.core.ui.theme.Spacing
import com.toeicspace.android.core.ui.theme.TagTone
import com.toeicspace.android.core.ui.theme.TagTones
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme
import com.toeicspace.android.core.ui.theme.softShadow
import com.toeicspace.android.feature.vocabulary.domain.PartOfSpeech
import com.toeicspace.android.feature.vocabulary.domain.VocabularySetDetail
import com.toeicspace.android.feature.vocabulary.domain.VocabularyWord
import com.toeicspace.android.feature.vocabulary.presentation.components.ToneLabel
import com.toeicspace.android.feature.vocabulary.presentation.components.VocabErrorState
import com.toeicspace.android.feature.vocabulary.presentation.components.VocabularyTopBar
import com.toeicspace.android.feature.vocabulary.presentation.components.WordSpeaker
import com.toeicspace.android.feature.vocabulary.presentation.components.labelRes
import com.toeicspace.android.feature.vocabulary.presentation.components.rememberWordSpeaker

private val MinWordCardWidth = 300.dp
private val StudyBarShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)

data class VocabularySetActions(
    val onBackClick: () -> Unit,
    val onRetry: () -> Unit,
    /** Opens the flashcards on the card at the given index. */
    val onStudyClick: (setId: String, startIndex: Int) -> Unit,
    val onOpenSet: (setId: String) -> Unit,
)

@Composable
internal fun VocabularySetRoute(
    onBackClick: () -> Unit,
    onStudyClick: (setId: String, startIndex: Int) -> Unit,
    onOpenSet: (setId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VocabularySetViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    VocabularySetScreen(
        state = state,
        actions =
            VocabularySetActions(
                onBackClick = onBackClick,
                onRetry = viewModel::onRetry,
                onStudyClick = onStudyClick,
                onOpenSet = onOpenSet,
            ),
        speaker = rememberWordSpeaker(),
        modifier = modifier,
    )
}

/**
 * Every word of one set with its meaning, and the way into the flashcards: a button pinned at the
 * bottom, or tapping a word to start on it. Wider windows lay the words out in columns.
 */
@Composable
internal fun VocabularySetScreen(
    state: VocabularySetUiState,
    actions: VocabularySetActions,
    speaker: WordSpeaker,
    modifier: Modifier = Modifier,
    windowWidth: WindowWidth = currentWindowWidth(),
) {
    val set = state.set
    Scaffold(
        modifier = modifier.background(SoftPageGradientBrush),
        containerColor = Color.Transparent,
        topBar = {
            VocabularyTopBar(
                title = set?.title ?: stringResource(R.string.vocab_title_set),
                onBackClick = actions.onBackClick,
            )
        },
        bottomBar = {
            if (set != null && set.words.isNotEmpty()) {
                StudyBar(onStudyClick = { actions.onStudyClick(set.id, 0) })
            }
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
            when {
                set != null && set.words.isEmpty() ->
                    EmptyState(
                        title = stringResource(R.string.vocab_title_empty_set),
                        body = stringResource(R.string.vocab_body_empty_set),
                    )
                set != null -> WordList(set, state.fromCache, actions, speaker, windowWidth)
                state.isLoading -> LoadingState()
                error != null -> VocabErrorState(error = error, onRetry = actions.onRetry)
            }
        }
    }
}

@Composable
private fun WordList(
    set: VocabularySetDetail,
    fromCache: Boolean,
    actions: VocabularySetActions,
    speaker: WordSpeaker,
    windowWidth: WindowWidth,
) {
    val horizontal = if (windowWidth.isAtLeastMedium) Spacing.lg else Spacing.md
    LazyVerticalGrid(
        columns =
            if (windowWidth.isAtLeastMedium) {
                GridCells.Adaptive(
                    MinWordCardWidth,
                )
            } else {
                GridCells.Fixed(1)
            },
        contentPadding = PaddingValues(horizontal = horizontal, vertical = Spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(Spacing.smd),
        verticalArrangement = Arrangement.spacedBy(Spacing.smd),
        modifier = Modifier.fillMaxSize(),
    ) {
        item(key = "summary", span = { GridItemSpan(maxLineSpan) }) {
            SetSummary(set = set, onOpenSet = actions.onOpenSet)
        }
        if (fromCache) {
            item(key = "offline", span = { GridItemSpan(maxLineSpan) }) {
                OfflineBanner(onRetry = actions.onRetry)
            }
        }
        itemsIndexed(items = set.words, key = { _, word -> word.id }) { index, word ->
            WordRow(
                word = word,
                index = index,
                onClick = { actions.onStudyClick(set.id, index) },
                onSpeak = { speaker.speak(word.headword) },
            )
        }
    }
}

@Composable
private fun SetSummary(
    set: VocabularySetDetail,
    onOpenSet: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        if (set.stageTitle.isNotBlank()) {
            Text(
                text = set.stageTitle.uppercase(),
                style = AppTextStyles.KickerLabel,
                color = HintText,
            )
        }
        Text(
            text = stringResource(R.string.vocab_label_word_count, set.words.size),
            style = AppTextStyles.Caption,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (set.previousSetId != null || set.nextSetId != null) {
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                set.previousSetId?.let { id ->
                    SecondaryButton(
                        text = stringResource(R.string.vocab_action_previous_set),
                        onClick = { onOpenSet(id) },
                        shape = PillShape,
                        leadingIcon = {
                            Icon(
                                painterResource(R.drawable.ic_chevron_left),
                                contentDescription = null,
                            )
                        },
                    )
                }
                set.nextSetId?.let { id ->
                    SecondaryButton(
                        text = stringResource(R.string.vocab_action_next_set),
                        onClick = { onOpenSet(id) },
                        shape = PillShape,
                        trailingIcon = {
                            Icon(
                                painterResource(R.drawable.ic_chevron_right),
                                contentDescription = null,
                            )
                        },
                    )
                }
            }
        }
    }
}

/** Parts of speech keep one tone each, so nouns and verbs can be told apart while scanning. */
private fun PartOfSpeech.tone(): TagTone = TagTones[ordinal % TagTones.size]

@Composable
private fun WordRow(
    word: VocabularyWord,
    index: Int,
    onClick: () -> Unit,
    onSpeak: () -> Unit,
) {
    ToeicCard(modifier = Modifier.fillMaxWidth(), onClick = onClick, itemSpacing = Spacing.xs) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Number, word and part of speech take all the room left of the audio button, so the
            // button sits at the same place on every card.
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.vocab_label_word_index, index + 1),
                    style = AppTextStyles.HelperText,
                    color = HintText,
                )
                Text(
                    text = word.headword,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                ToneLabel(
                    text = stringResource(word.partOfSpeech.labelRes()),
                    tone = word.partOfSpeech.tone(),
                )
            }
            ToeicIconButton(
                onClick = onSpeak,
                contentDescription = stringResource(R.string.vocab_cd_play_audio, word.headword),
            ) {
                Icon(painter = painterResource(R.drawable.ic_volume), contentDescription = null)
            }
        }
        word.phonetic?.let {
            Text(text = it, style = AppTextStyles.Caption, color = HintText)
        }
        Text(
            text = word.meaningVi,
            style = AppTextStyles.ReadingBody,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun StudyBar(onStudyClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().softShadow(StudyBarShape),
        shape = StudyBarShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, CardBorder),
    ) {
        Box(
            modifier =
                Modifier
                    .navigationBarsPadding()
                    .padding(horizontal = Spacing.lg, vertical = Spacing.smd),
            contentAlignment = Alignment.Center,
        ) {
            PrimaryButton(
                text = stringResource(R.string.vocab_action_study_flashcards),
                onClick = onStudyClick,
                shape = PillShape,
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_layers),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                },
                modifier = Modifier.fillMaxWidth().testTag(TestTags.VOCAB_STUDY_FLASHCARDS),
            )
        }
    }
}

internal val PreviewWords =
    listOf(
        VocabularyWord(
            id = "w1",
            headword = "agenda",
            phonetic = "/əˈdʒendə/",
            partOfSpeech = PartOfSpeech.NOUN,
            meaningVi = "chương trình nghị sự",
            definitionEn = "a list of matters to be discussed at a meeting",
            example = "The first item on the agenda is the budget.",
            exampleVi = "Mục đầu tiên trong chương trình họp là ngân sách.",
        ),
        VocabularyWord(
            id = "w2",
            headword = "attend",
            phonetic = "/əˈtend/",
            partOfSpeech = PartOfSpeech.VERB,
            meaningVi = "tham dự",
            definitionEn = null,
            example = "All staff must attend the safety training.",
            exampleVi = "Toàn bộ nhân viên phải tham dự buổi tập huấn an toàn.",
        ),
        VocabularyWord(
            id = "w3",
            headword = "punctual",
            phonetic = "/ˈpʌŋktʃuəl/",
            partOfSpeech = PartOfSpeech.ADJECTIVE,
            meaningVi = "đúng giờ",
            definitionEn = null,
            example = null,
            exampleVi = null,
        ),
    )

internal val PreviewSetDetail =
    VocabularySetDetail(
        id = "set1",
        stageId = "s1",
        stageTitle = "Khởi động",
        title = "Bài 01",
        order = 1,
        previousSetId = null,
        nextSetId = "set2",
        words = PreviewWords,
    )

private val PreviewActions =
    VocabularySetActions(onBackClick = {}, onRetry = {}, onStudyClick = { _, _ -> }, onOpenSet = {})

private val PreviewSpeaker = WordSpeaker { }

@Preview(widthDp = 390, heightDp = 800)
@Composable
private fun VocabularySetPhonePreview() {
    ToeicSpaceTheme {
        VocabularySetScreen(
            state = VocabularySetUiState(isLoading = false, set = PreviewSetDetail),
            actions = PreviewActions,
            speaker = PreviewSpeaker,
            windowWidth = WindowWidth.COMPACT,
        )
    }
}

@Preview(widthDp = 1280, heightDp = 800)
@Composable
private fun VocabularySetTabletPreview() {
    ToeicSpaceTheme {
        VocabularySetScreen(
            state =
                VocabularySetUiState(
                    isLoading = false,
                    set = PreviewSetDetail,
                    fromCache = true,
                ),
            actions = PreviewActions,
            speaker = PreviewSpeaker,
            windowWidth = WindowWidth.EXPANDED,
        )
    }
}

@Preview(widthDp = 390, heightDp = 700)
@Composable
private fun VocabularySetLoadingPreview() {
    ToeicSpaceTheme {
        VocabularySetScreen(
            state = VocabularySetUiState(),
            actions = PreviewActions,
            speaker = PreviewSpeaker,
            windowWidth = WindowWidth.COMPACT,
        )
    }
}

@Preview(widthDp = 390, heightDp = 700)
@Composable
private fun VocabularySetNotFoundPreview() {
    ToeicSpaceTheme {
        VocabularySetScreen(
            state =
                VocabularySetUiState(
                    isLoading = false,
                    error = AppError.Server(code = null, status = 404),
                ),
            actions = PreviewActions,
            speaker = PreviewSpeaker,
            windowWidth = WindowWidth.COMPACT,
        )
    }
}

@Preview(widthDp = 900, heightDp = 700)
@Composable
private fun VocabularySetEmptyTabletPreview() {
    ToeicSpaceTheme {
        VocabularySetScreen(
            state =
                VocabularySetUiState(
                    isLoading = false,
                    set = PreviewSetDetail.copy(words = emptyList()),
                ),
            actions = PreviewActions,
            speaker = PreviewSpeaker,
            windowWidth = WindowWidth.MEDIUM,
        )
    }
}
