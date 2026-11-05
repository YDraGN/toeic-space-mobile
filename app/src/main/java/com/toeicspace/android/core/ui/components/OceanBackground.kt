package com.toeicspace.android.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.tooling.preview.Preview
import com.toeicspace.android.core.ui.theme.ShellGradientBrush
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme
import com.toeicspace.android.core.ui.theme.WaveFoam

/** Where each wave crest sits, as a fraction of the height, and how strongly it shows. */
private val Waves = listOf(0.36f to 0.32f, 0.6f to 0.2f, 0.82f to 0.14f)

/** Sky-to-sea gradient with a few faint wave bands, behind the main shell. */
fun Modifier.oceanBackground(): Modifier =
    background(ShellGradientBrush)
        .drawBehind {
            Waves.forEach { (crest, alpha) -> drawWave(top = size.height * crest, alpha = alpha) }
        }

private fun DrawScope.drawWave(
    top: Float,
    alpha: Float,
) {
    val w = size.width
    val amplitude = w * 0.045f
    val bottom = top + amplitude * 2.5f
    val path =
        Path().apply {
            moveTo(0f, top + amplitude * 0.4f)
            cubicTo(w * 0.12f, top - amplitude, w * 0.25f, top - amplitude, w * 0.37f, top)
            cubicTo(w * 0.5f, top + amplitude, w * 0.62f, top + amplitude, w * 0.75f, top)
            cubicTo(
                w * 0.85f,
                top - amplitude,
                w * 0.93f,
                top - amplitude,
                w,
                top - amplitude * 0.3f,
            )
            // A band, not a fill to the bottom, so the deep sea keeps its color.
            lineTo(w, bottom)
            lineTo(0f, bottom)
            close()
        }
    drawPath(
        path = path,
        brush =
            Brush.verticalGradient(
                colors = listOf(WaveFoam.copy(alpha = alpha), Color.Transparent),
                startY = top - amplitude,
                endY = bottom,
            ),
    )
}

@Preview(heightDp = 640)
@Composable
private fun OceanBackgroundPreview() {
    ToeicSpaceTheme {
        Box(modifier = Modifier.fillMaxSize().oceanBackground())
    }
}
