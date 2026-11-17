package com.toeicspace.android.core.ui.components

import androidx.compose.ui.tooling.preview.Preview

/**
 * Phone and tablet previews for the shared states. Light only: `ToeicSpaceTheme` has no dark
 * color scheme yet; add a `uiMode = UI_MODE_NIGHT_YES` pair here once it does.
 * The background is `BrandBackground`, the colour behind the main shell.
 */
@Preview(
    name = "Phone",
    group = "States",
    widthDp = 360,
    heightDp = 640,
    showBackground = true,
    backgroundColor = 0xFFE7FAFF,
)
@Preview(
    name = "Tablet",
    group = "States",
    widthDp = 840,
    heightDp = 600,
    showBackground = true,
    backgroundColor = 0xFFE7FAFF,
)
internal annotation class StatePreviews

/** Inline variant: a list section on a phone, with room above and below. */
@Preview(
    name = "Inline - Phone",
    group = "States",
    widthDp = 360,
    showBackground = true,
    backgroundColor = 0xFFE7FAFF,
)
@Preview(
    name = "Inline - Tablet",
    group = "States",
    widthDp = 840,
    showBackground = true,
    backgroundColor = 0xFFE7FAFF,
)
internal annotation class InlineStatePreviews
