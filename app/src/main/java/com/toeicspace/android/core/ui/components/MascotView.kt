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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.toeicspace.android.R
import com.toeicspace.android.core.ui.theme.CoralBadge
import com.toeicspace.android.core.ui.theme.PrimaryGradientCenter
import com.toeicspace.android.core.ui.theme.Spacing
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme

/**
 * Oysteic mascot with an optional floating animation and glow behind it.
 * The mascot is decorative by default; pass [contentDescription] when it carries meaning.
 */
@Composable
fun MascotView(
    @DrawableRes mascotResId: Int,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    isFloating: Boolean = true,
    backgroundBrush: Brush? = null,
    contentDescription: String? = null,
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

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        if (backgroundBrush != null) {
            Box(
                modifier =
                    Modifier
                        .size(size * 2f)
                        .background(backgroundBrush, CircleShape),
            )
        }
        Image(
            painter = painterResource(id = mascotResId),
            contentDescription = contentDescription,
            modifier =
                Modifier
                    .size(size)
                    .graphicsLayer {
                        // Read in the draw phase so the animation does not recompose.
                        translationY = if (isFloating) offsetY.dp.toPx() else 0f
                    },
        )
    }
}

@Preview(name = "Mascot - Default Base", showBackground = true, backgroundColor = 0xFFFAF9FF)
@Composable
private fun MascotViewBasePreview() {
    ToeicSpaceTheme {
        Box(modifier = Modifier.padding(Spacing.gutter)) {
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
        Box(modifier = Modifier.padding(Spacing.gutter)) {
            MascotView(
                mascotResId = R.drawable.oy2_streak,
                size = 140.dp,
                backgroundBrush =
                    Brush.radialGradient(
                        colors =
                            listOf(
                                CoralBadge.copy(alpha = 0.75f),
                                PrimaryGradientCenter.copy(alpha = 0.45f),
                                Color.Transparent,
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
            modifier = Modifier.padding(Spacing.gutter),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MascotView(mascotResId = R.drawable.oy2_cheer, size = 90.dp)
            MascotView(mascotResId = R.drawable.oy2_success, size = 90.dp)
            MascotView(mascotResId = R.drawable.oy2_empty, size = 90.dp)
        }
    }
}
