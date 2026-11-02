package com.toeicspace.android.feature.auth.presentation.signup

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.toeicspace.android.R
import com.toeicspace.android.core.ui.components.PrimaryButton
import com.toeicspace.android.core.ui.components.ToeicTextField
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme
import com.toeicspace.android.feature.auth.domain.FieldError
import com.toeicspace.android.feature.auth.presentation.common.AuthMessageBanner
import com.toeicspace.android.feature.auth.presentation.common.AuthPromptLink
import com.toeicspace.android.feature.auth.presentation.common.AuthScaffold
import com.toeicspace.android.feature.auth.presentation.common.messageRes

@Composable
internal fun SignUpRoute(
    onBackClick: () -> Unit,
    onNavigateToVerifyEmail: (email: String, challengeId: String) -> Unit,
    onNavigateToSignIn: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SignUpViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.verification) {
        val verification = uiState.verification ?: return@LaunchedEffect
        onNavigateToVerifyEmail(verification.email, verification.challengeId)
        viewModel.onNavigationHandled()
    }

    SignUpScreen(
        uiState = uiState,
        onFullNameChange = viewModel::onFullNameChange,
        onEmailChange = viewModel::onEmailChange,
        onPhoneChange = viewModel::onPhoneChange,
        onPasswordChange = viewModel::onPasswordChange,
        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
        onSubmit = viewModel::onSubmit,
        onBackClick = onBackClick,
        onSignInClick = onNavigateToSignIn,
        modifier = modifier,
    )
}

@Composable
internal fun SignUpScreen(
    uiState: SignUpUiState,
    onFullNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onBackClick: () -> Unit,
    onSignInClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val enabled = !uiState.isSubmitting

    @Composable
    fun errorFor(
        localError: FieldError?,
        field: String,
    ): String? =
        when {
            localError != null -> stringResource(id = localError.messageRes())
            field in uiState.serverRejectedFields ->
                stringResource(
                    id = R.string.auth_error_field_rejected,
                )
            else -> null
        }

    AuthScaffold(
        title = stringResource(id = R.string.auth_title_sign_up),
        subtitle = stringResource(id = R.string.auth_subtitle_sign_up),
        mascotRes = R.drawable.oy2_warm,
        onBackClick = onBackClick,
        modifier = modifier,
        footer = {
            AuthPromptLink(
                prompt = stringResource(id = R.string.auth_prompt_have_account),
                action = stringResource(id = R.string.auth_action_go_to_sign_in),
                onClick = onSignInClick,
                enabled = enabled,
            )
        },
    ) {
        uiState.errorMessage?.let {
            AuthMessageBanner(
                text = stringResource(id = it),
                isError = true,
            )
        }

        ToeicTextField(
            value = uiState.fullName,
            onValueChange = onFullNameChange,
            label = stringResource(id = R.string.auth_label_full_name),
            enabled = enabled,
            errorText = errorFor(uiState.fullNameError, "fullName"),
        )
        ToeicTextField(
            value = uiState.email,
            onValueChange = onEmailChange,
            label = stringResource(id = R.string.auth_label_email),
            enabled = enabled,
            errorText = errorFor(uiState.emailError, "email"),
            keyboardType = KeyboardType.Email,
        )
        ToeicTextField(
            value = uiState.phone,
            onValueChange = onPhoneChange,
            label = stringResource(id = R.string.auth_label_phone),
            enabled = enabled,
            errorText = errorFor(uiState.phoneError, "phone"),
            supportingText = stringResource(id = R.string.auth_hint_optional),
            keyboardType = KeyboardType.Phone,
        )
        ToeicTextField(
            value = uiState.password,
            onValueChange = onPasswordChange,
            label = stringResource(id = R.string.auth_label_password),
            enabled = enabled,
            errorText = errorFor(uiState.passwordError, "password"),
            supportingText = stringResource(id = R.string.auth_hint_password_rules),
            isPassword = true,
        )
        ToeicTextField(
            value = uiState.confirmPassword,
            onValueChange = onConfirmPasswordChange,
            label = stringResource(id = R.string.auth_label_confirm_password),
            enabled = enabled,
            errorText = errorFor(uiState.confirmPasswordError, "confirmPassword"),
            isPassword = true,
            imeAction = ImeAction.Done,
            keyboardActions =
                KeyboardActions(onDone = {
                    focusManager.clearFocus()
                    onSubmit()
                }),
        )
        PrimaryButton(
            text = stringResource(id = R.string.auth_action_sign_up),
            onClick = {
                focusManager.clearFocus()
                onSubmit()
            },
            modifier = Modifier.fillMaxWidth(),
            isLoading = uiState.isSubmitting,
            loadingText = stringResource(id = R.string.auth_action_signing_up),
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_sign_up),
                    contentDescription = null,
                )
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SignUpScreenPreview() {
    ToeicSpaceTheme {
        SignUpScreen(
            uiState =
                SignUpUiState(
                    fullName = "Oysteic",
                    email = "oysteic@",
                    emailError = FieldError.InvalidEmail,
                ),
            onFullNameChange = {},
            onEmailChange = {},
            onPhoneChange = {},
            onPasswordChange = {},
            onConfirmPasswordChange = {},
            onSubmit = {},
            onBackClick = {},
            onSignInClick = {},
        )
    }
}
