package com.toeicspace.android.core.ui.theme

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

val OceanicInk = Color(0xFF0A2045)
val SunlitGold = Color(0xFFE3C23B)
val CoralBadge = Color(0xFFFF9092)
val SeaTurquoise = Color(0xFF22B5E1)
val JournalPaper = Color(0xFFFDFCF5)

val PrimaryGradientStart = Color(0xFF1467C2)
val PrimaryGradientCenter = Color(0xFF2087E6)
val PrimaryGradientEnd = Color(0xFFFF9092)

val SubtleBorder = Color(0xFFD6E3FF)
val PrimaryBorder = PrimaryGradientStart
val HighlightBorder = SunlitGold

val SecondaryButtonBackground = Color(0xFFF1F3FF)
val SecondaryButtonContent = PrimaryBorder
val SecondaryButtonBorder = PrimaryBorder.copy(alpha = 0.25f)

val PillButtonBackground = Color.White.copy(alpha = 0.85f)
val PillButtonBorder = SubtleBorder

val IconButtonBackground = Color.White.copy(alpha = 0.85f)
val IconButtonBorder = SubtleBorder
val IconButtonSelectedBackground = Color(0xFFE9EDFF)
val IconButtonSelectedContent = PrimaryBorder
val IconButtonSelectedBorder = PrimaryBorder.copy(alpha = 0.3f)

val AvatarGuestBorder = SubtleBorder
val AvatarUserBorder = PrimaryBorder
val AvatarStreakBorder = HighlightBorder

val NavDropletGradientStart = Color(0xFFEBF3FF)
val NavDropletGradientEnd = SubtleBorder
val NavDropletBorder = SubtleBorder

val ToeicSpaceLightColorScheme =
    lightColorScheme(
        primary = PrimaryGradientStart,
        onPrimary = Color.White,
        primaryContainer = Color(0xFFE9EDFF),
        onPrimaryContainer = OceanicInk,
        secondary = Color(0xFF006686),
        onSecondary = Color.White,
        background = Color(0xFFFAF9FF),
        onBackground = OceanicInk,
        surface = Color(0xFFFAF9FF),
        onSurface = OceanicInk,
        surfaceVariant = Color(0xFFE9EDFF),
        onSurfaceVariant = Color(0xFF414752),
        outline = Color(0xFF727783),
        outlineVariant = SubtleBorder,
        error = Color(0xFFBA1A1A),
        onError = Color.White,
    )
