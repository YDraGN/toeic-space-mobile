package com.toeicspace.android.feature.main.presentation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.toeicspace.android.R
import com.toeicspace.android.core.ui.components.AppTopBar
import com.toeicspace.android.core.ui.components.PillButton
import com.toeicspace.android.core.ui.components.ToeicIconButton
import com.toeicspace.android.core.ui.components.UserAvatar
import com.toeicspace.android.core.ui.theme.BrandGradientBrush
import com.toeicspace.android.core.ui.theme.Spacing
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme

private val TopBarAvatarSize = 40.dp

/** Fixed header of the shell: avatar, greeting and actions. Guests get "Sign in" instead of the bell. */
@Composable
internal fun MainTopBar(
    viewer: Viewer,
    timeOfDay: TimeOfDay,
    onAvatarClick: () -> Unit,
    onSearchClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onSignInClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppTopBar(
        modifier = modifier,
        leadingContent = {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(Spacing.smd),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                UserAvatar(
                    onClick = onAvatarClick,
                    isGuest = viewer is Viewer.Guest,
                    size = TopBarAvatarSize,
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text =
                            when (viewer) {
                                Viewer.Guest -> stringResource(id = R.string.main_greeting_guest)
                                is Viewer.Member -> stringResource(id = timeOfDay.greetingRes)
                            },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text =
                            when (viewer) {
                                Viewer.Guest -> stringResource(id = R.string.main_subtitle_guest)
                                is Viewer.Member -> viewer.fullName
                            },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ToeicIconButton(
                    onClick = onSearchClick,
                    contentDescription = stringResource(id = R.string.common_cd_search),
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_search),
                        contentDescription = null,
                    )
                }
                when (viewer) {
                    Viewer.Guest ->
                        PillButton(
                            text = stringResource(id = R.string.auth_action_go_to_sign_in),
                            onClick = onSignInClick,
                            backgroundBrush = BrandGradientBrush,
                            contentColor = Color.White,
                        )
                    is Viewer.Member ->
                        ToeicIconButton(
                            onClick = onNotificationsClick,
                            contentDescription =
                                stringResource(
                                    id = R.string.common_cd_notifications,
                                ),
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_bell),
                                contentDescription = null,
                            )
                        }
                }
            }
        },
    )
}

@get:StringRes
private val TimeOfDay.greetingRes: Int
    get() =
        when (this) {
            TimeOfDay.MORNING -> R.string.main_greeting_morning
            TimeOfDay.AFTERNOON -> R.string.main_greeting_afternoon
            TimeOfDay.EVENING -> R.string.main_greeting_evening
        }

@Preview(showBackground = true, backgroundColor = 0xFFFAF9FF)
@Composable
private fun MainTopBarMemberPreview() {
    ToeicSpaceTheme {
        MainTopBar(
            viewer = Viewer.Member(fullName = "Nguyễn Minh Anh", email = "minhanh@example.com"),
            timeOfDay = TimeOfDay.MORNING,
            onAvatarClick = {},
            onSearchClick = {},
            onNotificationsClick = {},
            onSignInClick = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFAF9FF)
@Composable
private fun MainTopBarGuestPreview() {
    ToeicSpaceTheme {
        MainTopBar(
            viewer = Viewer.Guest,
            timeOfDay = TimeOfDay.EVENING,
            onAvatarClick = {},
            onSearchClick = {},
            onNotificationsClick = {},
            onSignInClick = {},
        )
    }
}
