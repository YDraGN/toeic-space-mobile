package com.toeicspace.android.core.ui.theme

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The landing page's "shadow-soft": wide, faint and tinted navy rather than grey. Colored shadows
 * need API 28, which is our minSdk.
 */
fun Modifier.softShadow(
    shape: Shape,
    elevation: Dp = 12.dp,
): Modifier =
    shadow(
        elevation = elevation,
        shape = shape,
        clip = false,
        ambientColor = OceanicInk.copy(alpha = 0.12f),
        spotColor = SeaDeep.copy(alpha = 0.35f),
    )
