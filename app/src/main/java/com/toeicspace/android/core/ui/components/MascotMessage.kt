package com.toeicspace.android.core.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.toeicspace.android.R
import com.toeicspace.android.core.ui.theme.Spacing
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme

private val MessageMaxWidth = 360.dp
private val MessageMascotSize = 140.dp

/** Full-area message with a mascot, used by the shared states below. Scrolls on short screens. */
@Composable
fun MascotMessage(
    @DrawableRes mascotResId: Int,
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    actions: @Composable ColumnScope.() -> Unit = {},
) {
    // Centred when it fits, scrollable when the screen is short or the font is large.
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .heightIn(min = maxHeight)
                    .padding(horizontal = Spacing.lg, vertical = Spacing.xl),
            verticalArrangement = Arrangement.spacedBy(Spacing.md, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            MascotView(mascotResId = mascotResId, size = MessageMascotSize)
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = MessageMaxWidth),
            )
            Column(
                modifier =
                    Modifier
                        .widthIn(max = MessageMaxWidth)
                        .fillMaxWidth()
                        .padding(top = Spacing.sm),
                verticalArrangement = Arrangement.spacedBy(Spacing.smd),
                content = actions,
            )
        }
    }
}

/** A feature that has no backend yet. */
@Composable
fun ComingSoonState(
    title: String,
    modifier: Modifier = Modifier,
    body: String = stringResource(id = R.string.common_msg_coming_soon),
) {
    MascotMessage(
        mascotResId = R.drawable.oy2_maintenance,
        title = title,
        body = body,
        modifier = modifier,
    )
}

/** A guest opened something that needs an account: offer sign up first, then sign in. */
@Composable
fun SignInRequiredState(
    title: String,
    body: String,
    onSignUpClick: () -> Unit,
    onSignInClick: () -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes mascotResId: Int = R.drawable.oy2_locked,
) {
    MascotMessage(
        mascotResId = mascotResId,
        title = title,
        body = body,
        modifier = modifier,
    ) {
        PrimaryButton(
            text = stringResource(id = R.string.common_action_sign_up_free),
            onClick = onSignUpClick,
            modifier = Modifier.fillMaxWidth(),
        )
        SecondaryButton(
            text = stringResource(id = R.string.common_action_have_account),
            onClick = onSignInClick,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFAF9FF)
@Composable
private fun ComingSoonStatePreview() {
    ToeicSpaceTheme {
        ComingSoonState(title = "Luyện tập")
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFAF9FF)
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
