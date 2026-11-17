package com.toeicspace.android.feature.main.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.toeicspace.android.R
import com.toeicspace.android.core.ui.components.UserAvatar
import com.toeicspace.android.core.ui.components.selectedDroplet
import com.toeicspace.android.core.ui.theme.AppTextStyles
import com.toeicspace.android.core.ui.theme.Spacing
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme

/** Big on the collapsed rail, where the avatar stands alone. */
private val RailAvatarSize = 64.dp

/** Smaller on the open panel, so the name and email beside it have room. */
private val ExpandedRailAvatarSize = 40.dp
private val AccountShape = RoundedCornerShape(16.dp)

/**
 * Foot of the navigation rail, standing in for the Profile tab: the avatar alone when the rail is
 * collapsed, avatar with name and email (or the sign-in hint for guests) when it is expanded.
 */
@Composable
internal fun RailAccount(
    viewer: Viewer,
    isSelected: Boolean,
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val nameColor = if (isSelected) colors.onPrimary else colors.onSurface
    val detailColor = if (isSelected) colors.onPrimary else colors.onSurfaceVariant
    Row(
        modifier =
            modifier
                .then(if (expanded) Modifier.fillMaxWidth() else Modifier)
                // Same gradient as the selected tabs, so Profile reads as one of them.
                .selectedDroplet(
                    isSelected,
                    AccountShape,
                ).selectable(
                    selected = isSelected,
                    onClick = onClick,
                    role = Role.Tab,
                    interactionSource = null,
                    indication = null,
                ).padding(Spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(Spacing.smd),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        UserAvatar(
            onClick = onClick,
            imageUrl = (viewer as? Viewer.Member)?.avatarUrl,
            isGuest = viewer is Viewer.Guest,
            size = if (expanded) ExpandedRailAvatarSize else RailAvatarSize,
        )
        if (expanded) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text =
                        when (viewer) {
                            Viewer.Guest -> stringResource(id = R.string.main_greeting_guest)
                            is Viewer.Member -> viewer.fullName
                        },
                    style = MaterialTheme.typography.titleSmall,
                    color = nameColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text =
                        when (viewer) {
                            Viewer.Guest -> stringResource(id = R.string.auth_action_go_to_sign_in)
                            is Viewer.Member -> viewer.email
                        },
                    style = AppTextStyles.Caption,
                    color = detailColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 240)
@Composable
private fun RailAccountExpandedPreview() {
    ToeicSpaceTheme {
        RailAccount(
            viewer = Viewer.Member(fullName = "Nguyễn Minh Anh", email = "minhanh@example.com"),
            isSelected = true,
            expanded = true,
            onClick = {},
        )
    }
}
