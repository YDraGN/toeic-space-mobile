package com.toeicspace.android.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import com.toeicspace.android.R
import com.toeicspace.android.core.common.AppError
import com.toeicspace.android.core.ui.TestTags
import com.toeicspace.android.core.ui.theme.AppTextStyles
import com.toeicspace.android.core.ui.theme.Dimens
import com.toeicspace.android.core.ui.theme.Spacing
import com.toeicspace.android.core.ui.theme.ToeicSpaceShapes
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme

/** The app could not reach the server at all: no connection, or the server did not answer. */
val AppError.isConnectivityError: Boolean
    get() = this == AppError.Network || this == AppError.ServerUnreachable

/**
 * Slim banner above content loaded earlier, shown while the app is offline or the server is
 * unreachable: the learner keeps reading the last loaded data and knows it may be out of date.
 */
@Composable
fun OfflineBanner(
    modifier: Modifier = Modifier,
    error: AppError = AppError.Network,
    onRetry: (() -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme
    val text =
        stringResource(
            id =
                if (error == AppError.ServerUnreachable) {
                    R.string.state_offline_banner_server
                } else {
                    R.string.state_offline_banner_network
                },
        )
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(ToeicSpaceShapes.small)
                .background(colors.secondaryContainer)
                .padding(start = Spacing.smd, end = Spacing.xs)
                .padding(vertical = Spacing.xs)
                .testTag(TestTags.STATE_OFFLINE_BANNER)
                .semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite },
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_wifi_off),
            contentDescription = null,
            tint = colors.onSecondaryContainer,
            modifier = Modifier.size(Dimens.iconSm),
        )
        Text(
            text = text,
            style = AppTextStyles.Caption,
            color = colors.onSecondaryContainer,
            modifier = Modifier.weight(1f).padding(vertical = Spacing.xs),
        )
        if (onRetry != null) {
            TextButton(
                onClick = onRetry,
                modifier = Modifier.testTag(TestTags.STATE_ACTION_RETRY),
            ) {
                Text(
                    text = stringResource(id = R.string.common_action_retry),
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.primary,
                )
            }
        }
    }
}

/**
 * The usual load / error / content switch for a screen backed by cached data, so offline behaves
 * the same everywhere: with [data] it shows [content] (and [content] gets `isStale = true` when
 * the last refresh failed for lack of connection, so it can put an [OfflineBanner] on top);
 * without data it shows [LoadingState] while [isLoading], then [ErrorState] for [error], then
 * [empty].
 */
@Composable
fun <T : Any> CachedContent(
    data: T?,
    isLoading: Boolean,
    error: AppError?,
    onRetry: (() -> Unit)?,
    modifier: Modifier = Modifier,
    fullScreen: Boolean = true,
    empty: @Composable () -> Unit = { EmptyState(modifier = modifier, fullScreen = fullScreen) },
    content: @Composable (data: T, isStale: Boolean) -> Unit,
) {
    when {
        data != null -> content(data, error?.isConnectivityError == true)
        isLoading -> LoadingState(modifier = modifier, fullScreen = fullScreen)
        error != null ->
            ErrorState(
                error = error,
                onRetry = onRetry,
                modifier = modifier,
                fullScreen = fullScreen,
            )
        else -> empty()
    }
}

@InlineStatePreviews
@Composable
private fun OfflineBannerPreview() {
    ToeicSpaceTheme {
        Column(
            modifier = Modifier.padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            OfflineBanner(onRetry = {})
            OfflineBanner(error = AppError.ServerUnreachable)
        }
    }
}

@StatePreviews
@Composable
private fun CachedContentOfflineNoDataPreview() {
    ToeicSpaceTheme {
        CachedContent<String>(
            data = null,
            isLoading = false,
            error = AppError.Network,
            onRetry = {},
        ) { _, _ -> }
    }
}
