package com.toeicspace.android.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun ToeicSpaceTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ToeicSpaceLightColorScheme,
        typography = ToeicSpaceTypography,
        shapes = ToeicSpaceShapes,
        content = content,
    )
}
