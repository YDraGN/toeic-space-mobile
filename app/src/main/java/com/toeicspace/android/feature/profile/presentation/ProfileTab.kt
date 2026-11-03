package com.toeicspace.android.feature.profile.presentation

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.toeicspace.android.R
import com.toeicspace.android.core.ui.components.SecondaryButton
import com.toeicspace.android.core.ui.components.SignInRequiredState
import com.toeicspace.android.core.ui.components.ToeicCard
import com.toeicspace.android.core.ui.components.UserAvatar
import com.toeicspace.android.core.ui.theme.AppTextStyles
import com.toeicspace.android.core.ui.theme.Spacing
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme
import com.toeicspace.android.feature.main.presentation.Viewer

private val ProfileAvatarSize = 88.dp
private val RowIconSize = 20.dp

/** Account info and sign out for members; a sign-in prompt for guests. Read from the local session. */
@Composable
internal fun ProfileTab(
    viewer: Viewer,
    onSignUpClick: () -> Unit,
    onSignInClick: () -> Unit,
    onChangePasswordClick: () -> Unit,
    onSignOutClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (viewer) {
        Viewer.Guest ->
            SignInRequiredState(
                title = stringResource(id = R.string.profile_title_guest),
                body = stringResource(id = R.string.profile_body_guest),
                onSignUpClick = onSignUpClick,
                onSignInClick = onSignInClick,
                mascotResId = R.drawable.oy2_warm,
                modifier = modifier,
            )
        is Viewer.Member ->
            MemberProfile(
                member = viewer,
                onChangePasswordClick = onChangePasswordClick,
                onSignOutClick = onSignOutClick,
                modifier = modifier,
            )
    }
}

@Composable
private fun MemberProfile(
    member: Viewer.Member,
    onChangePasswordClick: () -> Unit,
    onSignOutClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.md, vertical = Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // The whole tab is the profile, so the avatar does not need to open anything.
        UserAvatar(onClick = {}, size = ProfileAvatarSize)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = member.fullName,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )
            Text(
                text = member.email,
                style = AppTextStyles.Caption,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        ToeicCard(modifier = Modifier.fillMaxWidth(), onClick = onChangePasswordClick) {
            ProfileRow(
                iconRes = R.drawable.ic_eye_off,
                label = stringResource(id = R.string.profile_action_change_password),
            )
        }
        SecondaryButton(
            text = stringResource(id = R.string.profile_action_sign_out),
            onClick = onSignOutClick,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_log_out),
                    contentDescription = null,
                    modifier = Modifier.size(RowIconSize),
                )
            },
        )
    }
}

@Composable
private fun ProfileRow(
    @DrawableRes iconRes: Int,
    label: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.smd),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(RowIconSize),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Icon(
            painter = painterResource(id = R.drawable.ic_chevron_right),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(RowIconSize),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFAF9FF)
@Composable
private fun ProfileTabMemberPreview() {
    ToeicSpaceTheme {
        ProfileTab(
            viewer = Viewer.Member(fullName = "Nguyễn Minh Anh", email = "minhanh@example.com"),
            onSignUpClick = {},
            onSignInClick = {},
            onChangePasswordClick = {},
            onSignOutClick = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFAF9FF)
@Composable
private fun ProfileTabGuestPreview() {
    ToeicSpaceTheme {
        ProfileTab(
            viewer = Viewer.Guest,
            onSignUpClick = {},
            onSignInClick = {},
            onChangePasswordClick = {},
            onSignOutClick = {},
        )
    }
}
