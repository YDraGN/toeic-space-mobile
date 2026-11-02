package com.toeicspace.android.feature.home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.toeicspace.android.R
import com.toeicspace.android.core.ui.components.MascotView
import com.toeicspace.android.core.ui.components.PrimaryButton
import com.toeicspace.android.core.ui.components.SecondaryButton
import com.toeicspace.android.core.ui.theme.Spacing
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme

@Composable
internal fun HomeRoute(
    onNavigateToSignIn: () -> Unit,
    onSessionEnded: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState) {
        if (uiState == HomeUiState.SessionEnded) onSessionEnded()
    }

    HomeScreen(
        uiState = uiState,
        onSignInClick = onNavigateToSignIn,
        onSignOutClick = viewModel::onSignOutClick,
        modifier = modifier,
    )
}

// Placeholder until the Home shell is implemented; it only shows who is using the app.
@Composable
internal fun HomeScreen(
    uiState: HomeUiState,
    onSignInClick: () -> Unit,
    onSignOutClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val buttonModifier =
        Modifier
            .widthIn(max = 360.dp)
            .fillMaxWidth()

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .systemBarsPadding()
                .padding(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        MascotView(mascotResId = R.drawable.oy2_maintenance, size = 140.dp)
        Text(
            text =
                when (uiState) {
                    is HomeUiState.SignedIn ->
                        stringResource(
                            id = R.string.home_greeting_user,
                            uiState.fullName,
                        )
                    HomeUiState.Guest -> stringResource(id = R.string.home_greeting_guest)
                    else -> stringResource(id = R.string.home_title_placeholder)
                },
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(id = R.string.common_msg_coming_soon),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        when (uiState) {
            is HomeUiState.SignedIn ->
                SecondaryButton(
                    text = stringResource(id = R.string.home_action_sign_out),
                    onClick = onSignOutClick,
                    modifier = buttonModifier,
                )
            HomeUiState.Guest ->
                PrimaryButton(
                    text = stringResource(id = R.string.home_action_sign_in),
                    onClick = onSignInClick,
                    modifier = buttonModifier,
                )
            else -> Unit
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenSignedInPreview() {
    ToeicSpaceTheme {
        HomeScreen(
            uiState = HomeUiState.SignedIn("Oysteic"),
            onSignInClick = {},
            onSignOutClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenGuestPreview() {
    ToeicSpaceTheme {
        HomeScreen(uiState = HomeUiState.Guest, onSignInClick = {}, onSignOutClick = {})
    }
}
