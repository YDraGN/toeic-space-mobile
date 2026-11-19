package com.toeicspace.android.feature.vocabulary.presentation.components

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.toeicspace.android.R
import com.toeicspace.android.core.common.AppError
import com.toeicspace.android.core.ui.components.ErrorState
import com.toeicspace.android.core.ui.theme.AppTextStyles
import com.toeicspace.android.core.ui.theme.Dimens
import com.toeicspace.android.core.ui.theme.Spacing
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme

/*
 * The vocabulary screens use the shared states (PBL-105): LoadingState, EmptyState and
 * OfflineBanner directly, and ErrorState through [VocabErrorState] for the vocabulary copy.
 */

private const val HTTP_NOT_FOUND = 404

/**
 * The shared [ErrorState] with the vocabulary title. A 404 means the set or stage is gone, so it
 * says so and has no Retry.
 */
@Composable
internal fun VocabErrorState(
    error: AppError,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    fullScreen: Boolean = true,
    @StringRes titleRes: Int = R.string.vocab_title_error,
) {
    val notFound = (error as? AppError.Server)?.status == HTTP_NOT_FOUND
    ErrorState(
        error = error,
        onRetry = if (notFound) null else onRetry,
        modifier = modifier,
        fullScreen = fullScreen,
        title = stringResource(if (notFound) R.string.vocab_title_not_found else titleRes),
        body = if (notFound) stringResource(R.string.vocab_body_not_found) else null,
    )
}

/**
 * A note during a review that answers are kept on the device (offline, or the result still being
 * sent). Same look as the shared OfflineBanner, with review-specific text.
 */
@Composable
internal fun VocabOfflineNote(
    @StringRes messageRes: Int,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(colors.secondaryContainer, MaterialTheme.shapes.small)
                .padding(horizontal = Spacing.smd, vertical = Spacing.sm)
                .semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite },
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_wifi_off),
            contentDescription = null,
            tint = colors.onSecondaryContainer,
            modifier = Modifier.size(Dimens.iconSm),
        )
        Text(
            text = stringResource(messageRes),
            style = AppTextStyles.Caption,
            color = colors.onSecondaryContainer,
            modifier = Modifier.weight(1f),
        )
    }
}

@Preview(showBackground = true, widthDp = 390)
@Composable
private fun VocabOfflineNotePreview() {
    ToeicSpaceTheme {
        VocabOfflineNote(
            messageRes = R.string.vocab_msg_review_offline,
            modifier = Modifier.padding(Spacing.md),
        )
    }
}

@Preview(showBackground = true, widthDp = 390)
@Composable
private fun VocabInlineErrorPreview() {
    ToeicSpaceTheme { VocabErrorState(error = AppError.Network, onRetry = {}, fullScreen = false) }
}
