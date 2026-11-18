package com.toeicspace.android.core.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.toeicspace.android.R
import com.toeicspace.android.core.common.AppError
import com.toeicspace.android.core.ui.TestTags
import com.toeicspace.android.core.ui.theme.Dimens
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme

/**
 * A load failed. Pass the [AppError] from the repository straight through: network and server
 * errors get their own mascot and copy (see [toErrorStateContent]).
 *
 * The Retry button shows when [onRetry] is set and retrying can help; for
 * [AppError.Unauthorized] it becomes "Sign in again" when [onSignInClick] is set. Override
 * [title]/[body] for screen-specific copy (e.g. "Chưa tải được khoá học") and add more buttons in
 * [extraActions] (e.g. "Back to catalog" on a 404).
 */
@Composable
fun ErrorState(
    error: AppError,
    onRetry: (() -> Unit)?,
    modifier: Modifier = Modifier,
    fullScreen: Boolean = true,
    title: String? = null,
    body: String? = null,
    onSignInClick: (() -> Unit)? = null,
    extraActions: @Composable ColumnScope.() -> Unit = {},
) {
    val content = error.toErrorStateContent()
    MascotMessage(
        mascotResId = content.mascotResId,
        title = title ?: stringResource(id = content.titleRes),
        body = body ?: stringResource(id = content.bodyRes),
        fullScreen = fullScreen,
        modifier = modifier.testTag(TestTags.STATE_ERROR),
    ) {
        when {
            content.action == ErrorAction.RETRY && onRetry != null ->
                RetryButton(onClick = onRetry)
            content.action == ErrorAction.SIGN_IN && onSignInClick != null ->
                PrimaryButton(
                    text = stringResource(id = R.string.state_action_sign_in_again),
                    onClick = onSignInClick,
                    modifier = Modifier.fillMaxWidth().testTag(TestTags.STATE_ACTION_SIGN_IN),
                )
        }
        extraActions()
    }
}

@Composable
private fun RetryButton(onClick: () -> Unit) {
    SecondaryButton(
        text = stringResource(id = R.string.common_action_retry),
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().testTag(TestTags.STATE_ACTION_RETRY),
        leadingIcon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_refresh),
                contentDescription = null,
                modifier = Modifier.size(Dimens.iconSm),
            )
        },
    )
}

@StatePreviews
@Composable
private fun ErrorStateNetworkPreview() {
    ToeicSpaceTheme { ErrorState(error = AppError.Network, onRetry = {}) }
}

@StatePreviews
@Composable
private fun ErrorStateServerPreview() {
    ToeicSpaceTheme {
        ErrorState(error = AppError.Server(code = null, status = 503), onRetry = {})
    }
}

@StatePreviews
@Composable
private fun ErrorStateUnauthorizedPreview() {
    ToeicSpaceTheme {
        ErrorState(error = AppError.Unauthorized, onRetry = {}, onSignInClick = {})
    }
}

@InlineStatePreviews
@Composable
private fun ErrorStateInlinePreview() {
    ToeicSpaceTheme {
        ErrorState(error = AppError.ServerUnreachable, onRetry = {}, fullScreen = false)
    }
}

@InlineStatePreviews
@Composable
private fun ErrorStateNotFoundInlinePreview() {
    ToeicSpaceTheme {
        ErrorState(
            error = AppError.Server(code = null, status = 404),
            onRetry = {},
            fullScreen = false,
        ) {
            SecondaryButton(
                text = "Xem khoá học khác",
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
