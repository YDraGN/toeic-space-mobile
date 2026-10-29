package com.toeicspace.android.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.toeicspace.android.R
import com.toeicspace.android.core.ui.theme.AvatarGuestBorder
import com.toeicspace.android.core.ui.theme.AvatarStreakBorder
import com.toeicspace.android.core.ui.theme.AvatarUserBorder
import com.toeicspace.android.core.ui.theme.OceanicInk
import com.toeicspace.android.core.ui.theme.PillShape
import com.toeicspace.android.core.ui.theme.Spacing
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme

@Composable
fun UserAvatar(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    avatarPainter: Painter? = null,
    streakCount: Int? = null,
    isGuest: Boolean = false,
    size: Dp = 48.dp,
) {
    val hasActiveStreak = streakCount != null && streakCount > 0
    val borderColor =
        when {
            hasActiveStreak -> AvatarStreakBorder
            isGuest -> AvatarGuestBorder
            else -> AvatarUserBorder
        }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            onClick = onClick,
            modifier = Modifier.size(size),
            shape = CircleShape,
            color = Color.White,
            border = BorderStroke(width = 1.5.dp, color = borderColor),
            shadowElevation = 2.dp,
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize(),
            ) {
                if (avatarPainter != null) {
                    Image(
                        painter = avatarPainter,
                        contentDescription = stringResource(id = R.string.common_cd_avatar),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_account),
                        contentDescription = stringResource(id = R.string.common_cd_default_avatar),
                        tint = OceanicInk,
                        modifier = Modifier.size(size * 0.55f),
                    )
                }
            }
        }

        if (hasActiveStreak) {
            Surface(
                modifier =
                    Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 6.dp, y = 3.dp),
                shape = PillShape,
                color = AvatarStreakBorder,
                border = BorderStroke(1.5.dp, Color.White),
                shadowElevation = 2.dp,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "🔥",
                        fontSize = 8.sp,
                        lineHeight = 10.sp,
                    )
                    Text(
                        text = streakCount.toString(),
                        color = OceanicInk,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 12.sp,
                    )
                }
            }
        }
    }
}

@Preview(name = "User Avatar Variants", showBackground = true, backgroundColor = 0xFFFAF9FF)
@Composable
private fun UserAvatarPreview() {
    ToeicSpaceTheme {
        Row(
            modifier = Modifier.padding(Spacing.gutter),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            UserAvatar(
                onClick = {},
                streakCount = 3,
            )
            UserAvatar(
                onClick = {},
                streakCount = null,
            )
            UserAvatar(
                onClick = {},
                isGuest = true,
            )
        }
    }
}
