package com.toeicspace.android.core.ui.theme

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Brand palette from the landing page (oyster-pearls-journey), converted from its OKLCH tokens.
val OceanicInk = Color(0xFF0A2045)
val InkSoft = Color(0xFF304568)
val SunlitGold = Color(0xFFE3C23B)
val CoralBadge = Color(0xFFFF9092)
val Blush = Color(0xFFFF99A3)
val SkySoft = Color(0xFF7ED6FF)
val SeaTurquoise = Color(0xFF22B5E1)
val SeaDeep = Color(0xFF0E5CAF)
val PastelSky = Color(0xFFBBE2FF)
val BrandBackground = Color(0xFFE7FAFF)
val BrandBorder = Color(0xFF8CB6DB)
val BrandDestructive = Color(0xFFD01C29)
val JournalPaper = Color(0xFFFDFCF5)
val Pearl = Color(0xFFF8EAC6)

/** Cream page behind journal-style cards: lighter than [Pearl] so ink text keeps its contrast. */
val PearlPaper = Color(0xFFFFF8E8)

/** Deep navy with a violet cast: the dark corner of hero cards, where white text sits. */
val NightSea = Color(0xFF15307A)

/** Text, lines and dots on the dark hero gradient. */
val OnHero = Color.White

/** Brighter than [SunlitGold], so the streak count and today's ring stay readable on the hero. */
val SunlitOnHero = Color(0xFFF5D85A)

/** Navy film behind the streak week: keeps its labels readable where the hero turns light. */
val HeroWeekTint = NightSea.copy(alpha = 0.45f)

/** The script tagline on the hero: the brand script is too thin to read in plain white. */
val TaglineCream = Color(0xFFFFF1C2)

/** Light coral behind icons; the pink sibling of [PastelSky] and [Pearl]. */
val BlushTint = Color(0xFFFFD3D6)

/** Hint under a form field: cooler than [InkSoft] so it never reads as the placeholder. */
val HintText = Color(0xFF4F6E96)

/** Floating bars: almost opaque, so content scrolling underneath shows only faintly. */
val BarSurface = Color.White.copy(alpha = 0.96f)

/** Rows on a [PearlPaper] card: white, softened so the paper still shows at the edges. */
val PaperRow = Color.White.copy(alpha = 0.9f)

/** Wave bands drawn over the shell gradient. */
val WaveFoam = Color.White

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

val NavDropletGradientStart = Pearl
val NavDropletGradientEnd = PastelSky
val NavDropletBorder = BrandBorder.copy(alpha = 0.35f)

/** Outline of cards and bars: the brand border, softened so it reads as a hairline. */
val CardBorder = BrandBorder.copy(alpha = 0.45f)

val ToeicSpaceLightColorScheme =
    lightColorScheme(
        primary = PrimaryGradientStart,
        onPrimary = Color(0xFFF9FCFF),
        primaryContainer = PastelSky,
        onPrimaryContainer = OceanicInk,
        secondary = SeaDeep,
        onSecondary = Color.White,
        secondaryContainer = PastelSky,
        onSecondaryContainer = OceanicInk,
        tertiary = CoralBadge,
        onTertiary = OceanicInk,
        background = BrandBackground,
        onBackground = OceanicInk,
        surface = Color.White,
        onSurface = OceanicInk,
        surfaceVariant = Color(0xFFE3F1FB),
        onSurfaceVariant = InkSoft,
        outline = BrandBorder,
        outlineVariant = Color(0xFFC6DCEE),
        error = BrandDestructive,
        onError = Color.White,
    )
