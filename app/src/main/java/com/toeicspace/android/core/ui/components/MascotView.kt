package com.toeicspace.android.core.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.toeicspace.android.R
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme

@Composable
fun MascotView(
    @DrawableRes mascotResId: Int,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    isFloating: Boolean = true,
    backgroundBrush: Brush? = null,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "MascotBobbing")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = -7f,
        targetValue = 7f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "MascotOffsetY",
    )
    val currentOffset = if (isFloating) offsetY.dp else 0.dp

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        backgroundBrush?.let { brush ->
            Box(
                modifier =
                    Modifier
                        .size(size * 2f)
                        .background(brush, CircleShape),
            )
        }
        Image(
            painter = painterResource(id = mascotResId),
            contentDescription = "TOEICSpace Mascot",
            modifier =
                Modifier
                    .size(size)
                    .offset(y = currentOffset),
        )
    }
}

@Preview(name = "Mascot - Default Base", showBackground = true, backgroundColor = 0xFFFAF9FF)
@Composable
private fun MascotViewBasePreview() {
    ToeicSpaceTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            MascotView(
                mascotResId = R.drawable.oy2_base,
                size = 140.dp,
            )
        }
    }
}

@Preview(
    name = "Mascot - With Glow Background",
    showBackground = true,
    backgroundColor = 0xFFFAF9FF,
)
@Composable
private fun MascotViewWithGlowPreview() {
    ToeicSpaceTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            MascotView(
                mascotResId = R.drawable.oy2_streak,
                size = 140.dp,
                backgroundBrush =
                    Brush.radialGradient(
                        colors =
                            listOf(
                                Color(0xFFFF9092).copy(alpha = 0.75f), // Hồng san hô rực rỡ ở tâm
                                Color(0xFF2087E6).copy(alpha = 0.45f), // Xanh đại dương chuyển tiếp
                                Color.Transparent, // Tỏa mờ dần ra ngoài
                            ),
                    ),
            )
        }
    }
}

@Preview(name = "Mascot - Emotion Set", showBackground = true, backgroundColor = 0xFFFAF9FF)
@Composable
private fun MascotViewEmotionSetPreview() {
    ToeicSpaceTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Mascot cổ vũ
            MascotView(
                mascotResId = R.drawable.oy2_cheer,
                size = 90.dp,
            )
            // Mascot hoàn thành bài học
            MascotView(
                mascotResId = R.drawable.oy2_success,
                size = 90.dp,
            )
            // Mascot danh sách trống
            MascotView(
                mascotResId = R.drawable.oy2_empty,
                size = 90.dp,
            )
        }
    }
}
