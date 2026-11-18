package com.toeicspace.android.core.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.toeicspace.android.R
import com.toeicspace.android.core.ui.TestTags
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme

/**
 * A guest opened something that needs an account: offer sign up first, then sign in.
 * [extraActions] go below the two buttons (e.g. "Explore courses first" on the guest home).
 */
@Composable
fun SignInRequiredState(
    onSignUpClick: () -> Unit,
    onSignInClick: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = stringResource(id = R.string.state_sign_in_required_title),
    body: String = stringResource(id = R.string.state_sign_in_required_body),
    fullScreen: Boolean = true,
    @DrawableRes mascotResId: Int = R.drawable.oy2_locked,
    extraActions: @Composable ColumnScope.() -> Unit = {},
) {
    MascotMessage(
        mascotResId = mascotResId,
        title = title,
        body = body,
        fullScreen = fullScreen,
        modifier = modifier.testTag(TestTags.STATE_SIGN_IN_REQUIRED),
    ) {
        PrimaryButton(
            text = stringResource(id = R.string.common_action_sign_up_free),
            onClick = onSignUpClick,
            modifier = Modifier.fillMaxWidth().testTag(TestTags.STATE_ACTION_SIGN_UP),
        )
        SecondaryButton(
            text = stringResource(id = R.string.common_action_have_account),
            onClick = onSignInClick,
            modifier = Modifier.fillMaxWidth().testTag(TestTags.STATE_ACTION_SIGN_IN),
        )
        extraActions()
    }
}

@StatePreviews
@Composable
private fun SignInRequiredStatePreview() {
    ToeicSpaceTheme {
        SignInRequiredState(
            title = "Đăng nhập để luyện tập",
            body = "Tạo tài khoản miễn phí để lưu kết quả và theo dõi tiến bộ của bạn.",
            onSignUpClick = {},
            onSignInClick = {},
        )
    }
}

@InlineStatePreviews
@Composable
private fun SignInRequiredStateInlinePreview() {
    ToeicSpaceTheme {
        SignInRequiredState(onSignUpClick = {}, onSignInClick = {}, fullScreen = false)
    }
}
