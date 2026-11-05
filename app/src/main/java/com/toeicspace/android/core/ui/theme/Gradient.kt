package com.toeicspace.android.core.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Background of the main shell: sky at the top settling into deep sea at the bottom. Lighter than the
 * full-screen splash/auth gradient so ink text stays readable over most of the height.
 */
val ShellGradientBrush =
    Brush.verticalGradient(
        0f to BrandBackground,
        0.28f to Color(0xFFC4EEFF),
        0.6f to SkySoft,
        0.86f to SeaTurquoise,
        1f to SeaDeep,
    )

/**
 * Hero cards: dark at the top-left, where the white text starts, fading to sky at the bottom-right,
 * where the mascot sits.
 */
val HeroGradientBrush =
    Brush.linearGradient(
        0f to NightSea,
        0.45f to SeaDeep,
        0.8f to SeaTurquoise,
        1f to SkySoft,
    )

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
