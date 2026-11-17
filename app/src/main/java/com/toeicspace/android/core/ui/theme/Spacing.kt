package com.toeicspace.android.core.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// 1rem = 16dp
object Spacing {
    // --- T-shirt scale cơ bản ---
    val xs: Dp = 4.dp
    val sm: Dp = 8.dp
    val smd: Dp = 12.dp
    val md: Dp = 16.dp
    val lg: Dp = 24.dp
    val xl: Dp = 32.dp
    val xxl: Dp = 48.dp

    // --- Specific Spacings ---
    val space1: Dp = 1.dp
    val space2: Dp = 2.dp
    val space6: Dp = 6.dp
    val space12: Dp = 12.dp
    val space18: Dp = 18.dp
    val space22: Dp = 22.dp
    val space36: Dp = 36.dp
    val space40: Dp = 40.dp

    // --- Layout ---
    val gutter: Dp = 16.dp
    val margin: Dp = 16.dp
}

/** Fixed sizes that are not spacing: borders, icons, mascots and readable widths. */
object Dimens {
    // --- Borders & strokes ---
    val borderHairline: Dp = 1.dp
    val strokeThin: Dp = 2.dp

    // --- Icons & indicators ---
    val iconSm: Dp = 20.dp
    val iconMd: Dp = 24.dp
    val progressSm: Dp = 24.dp
    val progressMd: Dp = 40.dp

    // --- Mascot ---
    val mascotSm: Dp = 96.dp
    val mascotMd: Dp = 120.dp
    val mascotLg: Dp = 140.dp

    // --- Readable widths ---
    val messageMaxWidth: Dp = 360.dp
    val formMaxWidth: Dp = 480.dp
}
