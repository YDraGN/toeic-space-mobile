package com.toeicspace.android.core.ui.theme

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// --- Raw color tokens từ design.md ---
val md_surface = Color(0xFFFAF9FF)
val md_surfaceDim = Color(0xFFCCDAFF)
val md_surfaceBright = Color(0xFFFAF9FF)
val md_surfaceContainerLowest = Color(0xFFFFFFFF)
val md_surfaceContainerLow = Color(0xFFF1F3FF)
val md_surfaceContainer = Color(0xFFE9EDFF)
val md_surfaceContainerHigh = Color(0xFFE1E8FF)
val md_surfaceContainerHighest = Color(0xFFD8E2FF)
val md_onSurface = Color(0xFF031A3F)
val md_onSurfaceVariant = Color(0xFF414752)
val md_inverseSurface = Color(0xFF1C3055)
val md_inverseOnSurface = Color(0xFFEDF0FF)
val md_outline = Color(0xFF727783)
val md_outlineVariant = Color(0xFFC1C6D4)
val md_surfaceTint = Color(0xFF005DB5)

val md_primary = Color(0xFF004F9B)
val md_onPrimary = Color(0xFFFFFFFF)
val md_primaryContainer = Color(0xFF1467C2)
val md_onPrimaryContainer = Color(0xFFDEE8FF)
val md_inversePrimary = Color(0xFFA9C7FF)

val md_secondary = Color(0xFF006686)
val md_onSecondary = Color(0xFFFFFFFF)
val md_secondaryContainer = Color(0xFF7CD4FD)
val md_onSecondaryContainer = Color(0xFF005B78)

val md_tertiary = Color(0xFF893338)
val md_onTertiary = Color(0xFFFFFFFF)
val md_tertiaryContainer = Color(0xFFA74B4E)
val md_onTertiaryContainer = Color(0xFFFFE1E0)

val md_error = Color(0xFFBA1A1A)
val md_onError = Color(0xFFFFFFFF)
val md_errorContainer = Color(0xFFFFDAD6)
val md_onErrorContainer = Color(0xFF93000A)

val md_primaryFixed = Color(0xFFD6E3FF)
val md_primaryFixedDim = Color(0xFFA9C7FF)
val md_onPrimaryFixed = Color(0xFF001B3D)
val md_onPrimaryFixedVariant = Color(0xFF00468B)

val md_secondaryFixed = Color(0xFFBFE8FF)
val md_secondaryFixedDim = Color(0xFF79D1FA)
val md_onSecondaryFixed = Color(0xFF001F2A)
val md_onSecondaryFixedVariant = Color(0xFF004D65)

val md_tertiaryFixed = Color(0xFFFFDAD9)
val md_tertiaryFixedDim = Color(0xFFFFB3B3)
val md_onTertiaryFixed = Color(0xFF400009)
val md_onTertiaryFixedVariant = Color(0xFF7D2A30)

val md_background = Color(0xFFFAF9FF)
val md_onBackground = Color(0xFF031A3F)
val md_surfaceVariant = Color(0xFFD8E2FF)

// --- Gradient & Action Button Colors
val PrimaryGradientStart = Color(0xFF1467C2)
val PrimaryGradientCenter = Color(0xFF2087E6)
val PrimaryGradientEnd = Color(0xFFFF9092)

val SecondaryButtonBackground = Color(0xFFF1F3FF)
val SecondaryButtonContent = Color(0xFF1467C2)
val SecondaryButtonBorder = Color(0xFF1467C2).copy(alpha = 0.25f)

// --- Icon Button & Badge Tokens ---
val CoralBadge = Color(0xFFFF9092)
val IconButtonBackground = Color.White.copy(alpha = 0.85f)
val IconButtonBorder = Color.White.copy(alpha = 0.9f)

val OceanicInk = Color(0xFF0A2045)
val JournalPaper = Color(0xFFFDFCF5)
val SunlitGold = Color(0xFFE3C23B)
val SeaTurquoise = Color(0xFF22B5E1)
val AbyssalMidnight = Color(0xFF0D102C)

val ToeicSpaceLightColorScheme =
    lightColorScheme(
        primary = md_primary,
        onPrimary = md_onPrimary,
        primaryContainer = md_primaryContainer,
        onPrimaryContainer = md_onPrimaryContainer,
        inversePrimary = md_inversePrimary,
        secondary = md_secondary,
        onSecondary = md_onSecondary,
        secondaryContainer = md_secondaryContainer,
        onSecondaryContainer = md_onSecondaryContainer,
        tertiary = md_tertiary,
        onTertiary = md_onTertiary,
        tertiaryContainer = md_tertiaryContainer,
        onTertiaryContainer = md_onTertiaryContainer,
        error = md_error,
        onError = md_onError,
        errorContainer = md_errorContainer,
        onErrorContainer = md_onErrorContainer,
        background = md_background,
        onBackground = md_onBackground,
        surface = md_surface,
        onSurface = md_onSurface,
        surfaceVariant = md_surfaceVariant,
        onSurfaceVariant = md_onSurfaceVariant,
        surfaceTint = md_surfaceTint,
        inverseSurface = md_inverseSurface,
        inverseOnSurface = md_inverseOnSurface,
        outline = md_outline,
        outlineVariant = md_outlineVariant,
        surfaceBright = md_surfaceBright,
        surfaceDim = md_surfaceDim,
        surfaceContainer = md_surfaceContainer,
        surfaceContainerHigh = md_surfaceContainerHigh,
        surfaceContainerHighest = md_surfaceContainerHighest,
        surfaceContainerLow = md_surfaceContainerLow,
        surfaceContainerLowest = md_surfaceContainerLowest,
        primaryFixed = md_primaryFixed,
        primaryFixedDim = md_primaryFixedDim,
        onPrimaryFixed = md_onPrimaryFixed,
        onPrimaryFixedVariant = md_onPrimaryFixedVariant,
        secondaryFixed = md_secondaryFixed,
        secondaryFixedDim = md_secondaryFixedDim,
        onSecondaryFixed = md_onSecondaryFixed,
        onSecondaryFixedVariant = md_onSecondaryFixedVariant,
        tertiaryFixed = md_tertiaryFixed,
        tertiaryFixedDim = md_tertiaryFixedDim,
        onTertiaryFixed = md_onTertiaryFixed,
        onTertiaryFixedVariant = md_onTertiaryFixedVariant,
    )
