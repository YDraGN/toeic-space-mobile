package com.toeicspace.android.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp

/** Material 3 window width classes, the one place the app decides between phone and tablet layouts. */
enum class WindowWidth {
    /** Under 600dp: phones in portrait. Bottom bar, one column. */
    COMPACT,

    /** 600 to 840dp: small tablets, foldables, some phones in landscape. Rail, expanded as an overlay. */
    MEDIUM,

    /** 840dp and wider: tablets in landscape. Rail that can stay expanded beside the content. */
    EXPANDED,
    ;

    val isAtLeastMedium: Boolean get() = this != COMPACT
}

@Composable
fun currentWindowWidth(): WindowWidth {
    val width =
        with(LocalDensity.current) {
            LocalWindowInfo.current.containerSize.width
                .toDp()
        }
    return when {
        width >= 840.dp -> WindowWidth.EXPANDED
        width >= 600.dp -> WindowWidth.MEDIUM
        else -> WindowWidth.COMPACT
    }
}
