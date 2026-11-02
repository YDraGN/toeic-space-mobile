package com.toeicspace.android.feature.auth.presentation.signin

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.toeicspace.android.R
import com.toeicspace.android.core.ui.components.PrimaryButton
import com.toeicspace.android.core.ui.components.ToeicTextField
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme
import com.toeicspace.android.feature.auth.presentation.common.AuthMessageBanner
import com.toeicspace.android.feature.auth.presentation.common.AuthPromptLink
import com.toeicspace.android.feature.auth.presentation.common.AuthScaffold
import com.toeicspace.android.feature.auth.presentation.common.AuthTextButton
import com.toeicspace.android.feature.auth.presentation.common.GuestEntryLink
import com.toeicspace.android.feature.auth.presentation.common.messageRes

@Composable
internal fun SignInRoute(
    initialEmail: String,
    notice: SignInNotice?,
    onSignedIn: () -> Unit,
    onNavigateToVerifyEmail: (email: String, challengeId: String) -> Unit,
    onNavigateToSignUp: () -> Unit,
    onNavigateToForgotPassword: (email: String) -> Unit,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null,
    viewModel: SignInViewModel =
        hiltViewModel<SignInViewModel, SignInViewModel.Factory>(
            creationCallback = { factory -> factory.create(initialEmail, notice) },
        ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.nextStep) {
        when (val step = uiState.nextStep) {
            SignInNextStep.Home -> onSignedIn()
            is SignInNextStep.VerifyEmail -> onNavigateToVerifyEmail(step.email, step.challengeId)
            null -> return@LaunchedEffect
        }
        viewModel.onNavigationHandled()
    }

    SignInScreen(
        uiState = uiState,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onSubmit = viewModel::onSubmit,
        onForgotPasswordClick = { onNavigateToForgotPassword(uiState.email) },
        onSignUpClick = onNavigateToSignUp,
        onContinueAsGuestClick = viewModel::onContinueAsGuestClick,
        onBackClick = onBackClick,
        modifier = modifier,
    )
}

@Composable
internal fun SignInScreen(
    uiState: SignInUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onSignUpClick: () -> Unit,
    onContinueAsGuestClick: () -> Unit,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null,
) {
    val focusManager = LocalFocusManager.current
    val enabled = !uiState.isSubmitting

    AuthScaffold(
        title = stringResource(id = R.string.auth_title_sign_in),
        subtitle = stringResource(id = R.string.auth_subtitle_sign_in),
        mascotRes = R.drawable.oy2_hello,
        onBackClick = onBackClick,
        modifier = modifier,
        footer = {
            AuthPromptLink(
                prompt = stringResource(id = R.string.auth_prompt_no_account),
                action = stringResource(id = R.string.auth_action_go_to_sign_up),
                onClick = onSignUpClick,
                enabled = enabled,
            )
            GuestEntryLink(onClick = onContinueAsGuestClick, enabled = enabled)
        },
    ) {
        uiState.noticeMessage?.let {
            AuthMessageBanner(
                text = stringResource(id = it),
                isError = false,
            )
        }
        uiState.errorMessage?.let {
            AuthMessageBanner(
                text = stringResource(id = it),
                isError = true,
            )
        }

        ToeicTextField(
            value = uiState.email,
            onValueChange = onEmailChange,
            label = stringResource(id = R.string.auth_label_email),
            enabled = enabled,
            errorText = uiState.emailError?.let { stringResource(id = it.messageRes()) },
            keyboardType = KeyboardType.Email,
        )
        ToeicTextField(
            value = uiState.password,
            onValueChange = onPasswordChange,
            label = stringResource(id = R.string.auth_label_password),
            enabled = enabled,
            errorText = uiState.passwordError?.let { stringResource(id = it.messageRes()) },
            isPassword = true,
            imeAction = ImeAction.Done,
            keyboardActions =
                KeyboardActions(onDone = {
                    focusManager.clearFocus()
                    onSubmit()
                }),
        )
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            AuthTextButton(
                text = stringResource(id = R.string.auth_action_forgot_password),
                onClick = onForgotPasswordClick,
                enabled = enabled,
            )
        }
        PrimaryButton(
            text = stringResource(id = R.string.auth_action_sign_in),
            onClick = {
                focusManager.clearFocus()
                onSubmit()
            },
            modifier = Modifier.fillMaxWidth(),
            isLoading = uiState.isSubmitting,
            loadingText = stringResource(id = R.string.auth_action_signing_in),
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_log_in),
                    contentDescription = null,
                )
            },
        )
    }
}

@Preview(showBackground = true)
@PreviewScreenSizes
@Composable
private fun SignInScreenPreview() {
    ToeicSpaceTheme {
        SignInScreen(
            uiState =
                SignInUiState(
                    email = "oysteic@toeicspace.vn",
                    noticeMessage = R.string.auth_notice_email_verified,
                ),
            onEmailChange = {},
            onPasswordChange = {},
            onSubmit = {},
            onForgotPasswordClick = {},
            onSignUpClick = {},
            onContinueAsGuestClick = {},
        )
    }
}
