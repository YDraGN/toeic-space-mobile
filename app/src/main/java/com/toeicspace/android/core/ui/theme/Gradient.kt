package com.toeicspace.android.core.ui.theme

import androidx.compose.ui.graphics.Brush

val BrandGradientBrush =
    Brush.horizontalGradient(
        colors = listOf(PrimaryGradientStart, PrimaryGradientCenter, PrimaryGradientEnd),
    )

val DisabledBrandGradientBrush =
    Brush.horizontalGradient(
        colors =
            listOf(
                PrimaryGradientStart.copy(alpha = 0.5f),
                PrimaryGradientCenter.copy(alpha = 0.5f),
                PrimaryGradientEnd.copy(alpha = 0.5f),
            ),
    )
