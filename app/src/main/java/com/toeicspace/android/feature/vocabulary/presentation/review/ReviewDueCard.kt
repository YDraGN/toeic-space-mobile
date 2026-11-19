package com.toeicspace.android.feature.vocabulary.presentation.review

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.toeicspace.android.R
import com.toeicspace.android.core.common.AppError
import com.toeicspace.android.core.ui.TestTags
import com.toeicspace.android.core.ui.WindowWidth
import com.toeicspace.android.core.ui.components.PrimaryButton
import com.toeicspace.android.core.ui.components.SecondaryButton
import com.toeicspace.android.core.ui.theme.AppTextStyles
import com.toeicspace.android.core.ui.theme.HeroGradientBrush
import com.toeicspace.android.core.ui.theme.HeroWeekTint
import com.toeicspace.android.core.ui.theme.OnHero
import com.toeicspace.android.core.ui.theme.PillShape
import com.toeicspace.android.core.ui.theme.Spacing
import com.toeicspace.android.core.ui.theme.SunlitOnHero
import com.toeicspace.android.core.ui.theme.TaglineCream
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme
import com.toeicspace.android.core.ui.theme.softShadow
import com.toeicspace.android.feature.auth.presentation.common.messageRes
import com.toeicspace.android.feature.vocabulary.domain.ActiveReview
import com.toeicspace.android.feature.vocabulary.domain.ReviewSource
import com.toeicspace.android.feature.vocabulary.domain.ReviewSummary

private val MascotSize = 96.dp

data class ReviewDueActions(
    val onStartReview: (ReviewSource) -> Unit,
    val onSignInClick: () -> Unit,
    val onSignUpClick: () -> Unit,
    val onRetry: () -> Unit,
)

/**
 * The review card at the top of the "Từ vựng" tab. A signed-in learner sees how many words are due
 * today and starts (or continues) the review; a guest is invited to sign in, since reviews follow
 * each learner's own forgetting curve.
 */
@Composable
fun ReviewDueCard(
    windowWidth: WindowWidth,
    onStartReview: (ReviewSource) -> Unit,
    onSignInClick: () -> Unit,
    onSignUpClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReviewSummaryViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    // Back from a session the numbers have changed.
    LifecycleResumeEffect(viewModel) {
        viewModel.onRefresh()
        onPauseOrDispose { }
    }
    ReviewDueContent(
        state = state,
        actions = ReviewDueActions(onStartReview, onSignInClick, onSignUpClick, viewModel::onRetry),
        wide = windowWidth == WindowWidth.MEDIUM,
        modifier = modifier,
    )
}

@Composable
internal fun ReviewDueContent(
    state: ReviewSummaryUiState,
    actions: ReviewDueActions,
    modifier: Modifier = Modifier,
    wide: Boolean = false,
) {
    val shape = MaterialTheme.shapes.medium
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .softShadow(shape)
                .clip(shape)
                .background(HeroGradientBrush)
                .padding(Spacing.md),
    ) {
        when (state.isSignedIn) {
            null -> Unit
            false -> GuestInvite(actions = actions, wide = wide)
            true -> MemberSummary(state = state, actions = actions, wide = wide)
        }
    }
}

@Composable
private fun GuestInvite(
    actions: ReviewDueActions,
    wide: Boolean,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.smd)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.smd),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                Text(
                    text = stringResource(R.string.vocab_label_review_kicker),
                    style = AppTextStyles.KickerLabel,
                    color = TaglineCream,
                )
                Text(
                    text = stringResource(R.string.main_title_vocabulary_locked),
                    style = MaterialTheme.typography.titleMedium,
                    color = OnHero,
                )
                Text(
                    text = stringResource(R.string.main_body_vocabulary_locked),
                    style = AppTextStyles.Caption,
                    color = OnHero,
                )
            }
            Image(
                painter = painterResource(R.drawable.oy2_locked),
                contentDescription = null,
                modifier = Modifier.size(MascotSize),
            )
        }
        ButtonRow(wide = wide) {
            PrimaryButton(
                text = stringResource(R.string.common_action_sign_up_free),
                onClick = actions.onSignUpClick,
                shape = PillShape,
                modifier = it,
            )
            SecondaryButton(
                text = stringResource(R.string.common_action_have_account),
                onClick = actions.onSignInClick,
                shape = PillShape,
                modifier = it,
            )
        }
    }
}

@Composable
private fun MemberSummary(
    state: ReviewSummaryUiState,
    actions: ReviewDueActions,
    wide: Boolean,
) {
    val summary = state.summary
    val error = state.error
    when {
        summary != null ->
            DueSummary(
                summary = summary,
                state = state,
                actions = actions,
                wide = wide,
            )
        state.isLoading || error == null ->
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.lg),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = OnHero)
            }
        else ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(error.messageRes()),
                    style = AppTextStyles.Caption,
                    color = OnHero,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = actions.onRetry) {
                    Text(
                        text = stringResource(R.string.vocab_action_retry),
                        style = MaterialTheme.typography.labelLarge,
                        color = SunlitOnHero,
                    )
                }
            }
    }
}

@Composable
private fun DueSummary(
    summary: ReviewSummary,
    state: ReviewSummaryUiState,
    actions: ReviewDueActions,
    wide: Boolean,
) {
    val dueLabel = stringResource(R.string.vocab_cd_due_today, summary.due)
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.smd)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.smd),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier =
                    Modifier.weight(1f).semantics(mergeDescendants = true) {
                        contentDescription =
                            dueLabel
                    },
                verticalArrangement = Arrangement.spacedBy(Spacing.space2),
            ) {
                Text(
                    text = stringResource(R.string.vocab_label_review_kicker),
                    style = AppTextStyles.KickerLabel,
                    color = TaglineCream,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Text(
                        text = summary.due.toString(),
                        style = MaterialTheme.typography.displayLarge,
                        color = SunlitOnHero,
                    )
                    Text(
                        text = stringResource(R.string.vocab_label_due_words),
                        style = MaterialTheme.typography.titleMedium,
                        color = OnHero,
                        modifier = Modifier.padding(bottom = Spacing.xs),
                    )
                }
            }
            Image(
                painter =
                    painterResource(
                        if (summary.due >
                            0
                        ) {
                            R.drawable.oy2_reading
                        } else {
                            R.drawable.oy2_proud
                        },
                    ),
                contentDescription = null,
                modifier = Modifier.size(MascotSize),
            )
        }
        StatsStrip(summary = summary)
        if (state.fromCache || state.pendingAnswers > 0) {
            Text(
                text =
                    if (state.pendingAnswers > 0) {
                        stringResource(R.string.vocab_msg_pending_answers, state.pendingAnswers)
                    } else {
                        stringResource(R.string.vocab_msg_summary_offline)
                    },
                style = AppTextStyles.HelperText,
                color = TaglineCream,
            )
        }
        ReviewButtons(summary = summary, actions = actions, wide = wide)
    }
}

@Composable
private fun StatsStrip(summary: ReviewSummary) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(HeroWeekTint, MaterialTheme.shapes.small)
                .padding(horizontal = Spacing.smd, vertical = Spacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Stat(
            value =
                stringResource(
                    R.string.vocab_stat_reviewed_value,
                    summary.reviewedToday,
                    summary.dailyGoal,
                ),
            label = stringResource(R.string.vocab_stat_reviewed),
        )
        Stat(
            value = summary.streakDays.toString(),
            label = stringResource(R.string.vocab_stat_streak),
        )
        Stat(
            value = summary.mistakes.toString(),
            label = stringResource(R.string.vocab_stat_mistakes),
        )
    }
}

@Composable
private fun Stat(
    value: String,
    label: String,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleMedium, color = OnHero)
        Text(text = label, style = AppTextStyles.HelperText, color = TaglineCream)
    }
}

@Composable
private fun ReviewButtons(
    summary: ReviewSummary,
    actions: ReviewDueActions,
    wide: Boolean,
) {
    val active: ActiveReview? = summary.activeReview?.takeIf { it.position in 1 until it.total }
    ButtonRow(wide = wide) { buttonModifier ->
        when {
            active != null ->
                PrimaryButton(
                    text =
                        stringResource(
                            R.string.vocab_action_continue_review,
                            active.position,
                            active.total,
                        ),
                    onClick = { actions.onStartReview(ReviewSource.DUE) },
                    shape = PillShape,
                    modifier = buttonModifier.testTag(TestTags.VOCAB_REVIEW_START),
                )
            summary.due > 0 ->
                PrimaryButton(
                    text = stringResource(R.string.vocab_action_start_review),
                    onClick = { actions.onStartReview(ReviewSource.DUE) },
                    shape = PillShape,
                    modifier = buttonModifier.testTag(TestTags.VOCAB_REVIEW_START),
                )
            else ->
                Text(
                    text = stringResource(R.string.vocab_msg_nothing_due),
                    style = AppTextStyles.Caption,
                    color = OnHero,
                    modifier = buttonModifier.padding(vertical = Spacing.sm),
                )
        }
        if (summary.mistakes > 0) {
            SecondaryButton(
                text =
                    stringResource(
                        R.string.vocab_action_review_mistakes_count,
                        summary.mistakes,
                    ),
                onClick = { actions.onStartReview(ReviewSource.MISTAKES) },
                shape = PillShape,
                modifier = buttonModifier,
            )
        }
    }
}

/** Buttons side by side on a medium window (the card is wide there), stacked otherwise. */
@Composable
private fun ButtonRow(
    wide: Boolean,
    content: @Composable (Modifier) -> Unit,
) {
    if (wide) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) { content(Modifier.weight(1f)) }
    } else {
        Column(
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) { content(Modifier.fillMaxWidth()) }
    }
}

private val PreviewActions = ReviewDueActions({}, {}, {}, {})

internal val PreviewSummary =
    ReviewSummary(
        due = 24,
        reviewedToday = 8,
        dailyGoal = 20,
        streakDays = 5,
        mistakes = 6,
        learned = 320,
        activeReview = ActiveReview(id = "r1", source = ReviewSource.DUE, position = 7, total = 20),
    )

@Preview(widthDp = 390, showBackground = true)
@Composable
private fun ReviewDueMemberPreview() {
    ToeicSpaceTheme {
        ReviewDueContent(
            state =
                ReviewSummaryUiState(
                    isSignedIn = true,
                    summary = PreviewSummary,
                    pendingAnswers = 2,
                ),
            actions = PreviewActions,
            modifier = Modifier.padding(Spacing.md),
        )
    }
}

@Preview(widthDp = 760, showBackground = true)
@Composable
private fun ReviewDueTabletPreview() {
    ToeicSpaceTheme {
        ReviewDueContent(
            state =
                ReviewSummaryUiState(
                    isSignedIn = true,
                    summary = PreviewSummary.copy(due = 0, activeReview = null),
                    fromCache = true,
                ),
            actions = PreviewActions,
            wide = true,
            modifier = Modifier.padding(Spacing.md),
        )
    }
}

@Preview(widthDp = 390, showBackground = true)
@Composable
private fun ReviewDueGuestPreview() {
    ToeicSpaceTheme {
        ReviewDueContent(
            state = ReviewSummaryUiState(isSignedIn = false),
            actions = PreviewActions,
            modifier = Modifier.padding(Spacing.md),
        )
    }
}

@Preview(widthDp = 390, showBackground = true)
@Composable
private fun ReviewDueErrorPreview() {
    ToeicSpaceTheme {
        ReviewDueContent(
            state = ReviewSummaryUiState(isSignedIn = true, error = AppError.ServerUnreachable),
            actions = PreviewActions,
            modifier = Modifier.padding(Spacing.md),
        )
    }
}
