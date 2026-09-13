package com.toeicspace.android.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.toeicspace.android.R
import com.toeicspace.android.core.ui.theme.OceanicInk
import com.toeicspace.android.core.ui.theme.PillShape
import com.toeicspace.android.core.ui.theme.SunlitGold
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
            hasActiveStreak -> SunlitGold
            isGuest -> Color(0xFFC1C6D4)
            else -> Color(0xFF1467C2)
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
            shadowElevation = 3.dp,
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize(),
            ) {
                when {
                    avatarPainter != null -> {
                        Image(
                            painter = avatarPainter,
                            contentDescription = "User Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }

                    else -> {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_account),
                            contentDescription = "Default Account",
                            tint = OceanicInk,
                            modifier = Modifier.size(size * 0.55f),
                        )
                    }
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
                color = SunlitGold,
                border = BorderStroke(1.5.dp, Color.White),
                shadowElevation = 2.dp,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "🔥",
                        fontSize = 8.sp,
                        lineHeight = 10.sp,
                    )
                    Spacer(modifier = Modifier.width(2.dp))
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
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 1. Có Streak 3 ngày
            UserAvatar(
                onClick = {},
                avatarPainter = painterResource(id = R.drawable.ic_launcher_foreground),
                streakCount = 3,
            )
            // 2. Không có Streak
            UserAvatar(
                onClick = {},
                avatarPainter = painterResource(id = R.drawable.ic_launcher_foreground),
                streakCount = null, // Hoặc 0
            )
            // 3. Tài khoản chưa có ảnh đại diện
            UserAvatar(
                onClick = {},
                streakCount = 7,
            )
            // 4. Khách vãng lai
            UserAvatar(
                onClick = {},
                isGuest = true,
            )
        }
    }
}
