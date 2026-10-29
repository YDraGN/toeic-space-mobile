package com.toeicspace.android.feature.home.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.toeicspace.android.R
import com.toeicspace.android.core.ui.components.PlaceholderScreen
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme

// Placeholder until the Home feature is implemented.
@Composable
internal fun HomeScreen(modifier: Modifier = Modifier) {
    PlaceholderScreen(
        title = stringResource(id = R.string.home_title_placeholder),
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    ToeicSpaceTheme {
        HomeScreen()
    }
}
