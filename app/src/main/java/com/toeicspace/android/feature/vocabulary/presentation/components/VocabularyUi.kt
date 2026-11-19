package com.toeicspace.android.feature.vocabulary.presentation.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.toeicspace.android.R
import com.toeicspace.android.core.ui.components.AppTopBar
import com.toeicspace.android.core.ui.components.ToeicIconButton
import com.toeicspace.android.core.ui.theme.BlushTone
import com.toeicspace.android.core.ui.theme.LilacTone
import com.toeicspace.android.core.ui.theme.MintTone
import com.toeicspace.android.core.ui.theme.PeachTone
import com.toeicspace.android.core.ui.theme.SkyTone
import com.toeicspace.android.core.ui.theme.Spacing
import com.toeicspace.android.core.ui.theme.TagTone
import com.toeicspace.android.feature.vocabulary.domain.PartOfSpeech

/**
 * One tone per roadmap stage, climbing like the course level meter: calm mint for the first
 * stage, sky, lilac, peach, then blush for the last one. Lists of sets take their stage's tone,
 * so a stage reads as one color and the next stage as another.
 */
private val StageTones = listOf(MintTone, SkyTone, LilacTone, PeachTone, BlushTone)

/** [order] is the stage's 1-based position in the roadmap. */
internal fun stageTone(order: Int): TagTone = StageTones[Math.floorMod(order - 1, StageTones.size)]

/** Oysteic grows along the roadmap: says hello, reads, focuses, pushes on, then is proud. */
private val StageMascots =
    listOf(
        R.drawable.oy2_hello,
        R.drawable.oy2_reading,
        R.drawable.oy2_focus,
        R.drawable.oy2_determined,
        R.drawable.oy2_proud,
    )

/** The mascot on the card of the stage at [order] (1-based), cycling past the fifth. */
@DrawableRes
internal fun stageMascot(order: Int): Int =
    StageMascots[Math.floorMod(order - 1, StageMascots.size)]

@StringRes
internal fun PartOfSpeech.labelRes(): Int =
    when (this) {
        PartOfSpeech.NOUN -> R.string.vocab_pos_noun
        PartOfSpeech.VERB -> R.string.vocab_pos_verb
        PartOfSpeech.ADJECTIVE -> R.string.vocab_pos_adjective
        PartOfSpeech.ADVERB -> R.string.vocab_pos_adverb
        PartOfSpeech.PREPOSITION -> R.string.vocab_pos_preposition
        PartOfSpeech.CONJUNCTION -> R.string.vocab_pos_conjunction
        PartOfSpeech.PRONOUN -> R.string.vocab_pos_pronoun
        PartOfSpeech.DETERMINER -> R.string.vocab_pos_determiner
        PartOfSpeech.INTERJECTION -> R.string.vocab_pos_interjection
        PartOfSpeech.PHRASE -> R.string.vocab_pos_phrase
        PartOfSpeech.OTHER -> R.string.vocab_pos_other
    }

/** A small tinted label such as a part of speech or "Bài 03". */
@Composable
internal fun ToneLabel(
    text: String,
    tone: TagTone,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = tone.content,
        maxLines = 1,
        modifier =
            modifier
                .background(tone.container, MaterialTheme.shapes.extraSmall)
                .padding(horizontal = Spacing.sm, vertical = Spacing.space2),
    )
}

/** Back button and title for the full-screen vocabulary pages. */
@Composable
internal fun VocabularyTopBar(
    title: String,
    onBackClick: () -> Unit,
    trailing: @Composable () -> Unit = {},
) {
    AppTopBar(
        leadingContent = {
            ToeicIconButton(
                onClick = onBackClick,
                contentDescription = stringResource(id = R.string.common_action_back),
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_chevron_left),
                    contentDescription = null,
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f).padding(start = Spacing.smd),
            )
            trailing()
        },
    )
}
