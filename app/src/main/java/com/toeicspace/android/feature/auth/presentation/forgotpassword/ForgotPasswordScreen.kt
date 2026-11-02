package com.toeicspace.android.feature.auth.presentation.forgotpassword

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
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
import com.toeicspace.android.feature.auth.presentation.common.AuthMessageBanner
import com.toeicspace.android.feature.auth.presentation.common.AuthScaffold
import com.toeicspace.android.feature.auth.presentation.common.AuthTextButton
import com.toeicspace.android.feature.auth.presentation.common.messageRes
import com.toeicspace.android.feature.auth.presentation.verifyemail.OtpField
import com.toeicspace.android.feature.auth.presentation.verifyemail.ResendOtpButton

@Composable
internal fun ForgotPasswordRoute(
    initialEmail: String,
    onBackClick: () -> Unit,
    onPasswordReset: (email: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ForgotPasswordViewModel =
        hiltViewModel<ForgotPasswordViewModel, ForgotPasswordViewModel.Factory>(
            creationCallback = { factory -> factory.create(initialEmail) },
        ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isCompleted) {
        if (!uiState.isCompleted) return@LaunchedEffect
        onPasswordReset(uiState.email.trim())
        viewModel.onNavigationHandled()
    }

    ForgotPasswordScreen(
        uiState = uiState,
        onEmailChange = viewModel::onEmailChange,
        onOtpChange = viewModel::onOtpChange,
        onNewPasswordChange = viewModel::onNewPasswordChange,
        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
        onSubmit = viewModel::onSubmit,
        onResendClick = viewModel::onResendClick,
        onChangeEmailClick = viewModel::onChangeEmailClick,
        onBackClick = onBackClick,
        modifier = modifier,
    )
}

@Composable
internal fun ForgotPasswordScreen(
    uiState: ForgotPasswordUiState,
    onEmailChange: (String) -> Unit,
    onOtpChange: (String) -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onResendClick: () -> Unit,
    onChangeEmailClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val enabled = !uiState.isSubmitting
    val submit = {
        focusManager.clearFocus()
        onSubmit()
    }

    AuthScaffold(
        title = stringResource(id = R.string.auth_title_forgot_password),
        subtitle =
            when (uiState.step) {
                ForgotPasswordStep.EMAIL -> stringResource(id = R.string.auth_body_forgot_email)
                ForgotPasswordStep.OTP ->
                    stringResource(
                        id = R.string.auth_body_forgot_otp,
                        uiState.email.trim(),
                    )
                ForgotPasswordStep.NEW_PASSWORD ->
                    stringResource(
                        id = R.string.auth_body_forgot_new_password,
                    )
            },
        mascotRes =
            if (uiState.step ==
                ForgotPasswordStep.NEW_PASSWORD
            ) {
                R.drawable.oy2_determined
            } else {
                R.drawable.oy2_search
            },
        onBackClick = onBackClick,
        modifier = modifier,
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

        when (uiState.step) {
            ForgotPasswordStep.EMAIL -> {
                ToeicTextField(
                    value = uiState.email,
                    onValueChange = onEmailChange,
                    label = stringResource(id = R.string.auth_label_email),
                    enabled = enabled,
                    errorText = uiState.emailError?.let { stringResource(id = it.messageRes()) },
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done,
                    keyboardActions = KeyboardActions(onDone = { submit() }),
                )
                PrimaryButton(
                    text = stringResource(id = R.string.auth_action_send_otp),
                    onClick = submit,
                    modifier = Modifier.fillMaxWidth(),
                    isLoading = uiState.isSubmitting,
                )
            }
            ForgotPasswordStep.OTP -> {
                OtpField(
                    otp = uiState.otp,
                    onOtpChange = onOtpChange,
                    onDone = submit,
                    enabled = enabled,
                    errorText = uiState.otpError?.let { stringResource(id = it.messageRes()) },
                )
                PrimaryButton(
                    text = stringResource(id = R.string.auth_action_continue),
                    onClick = submit,
                    modifier = Modifier.fillMaxWidth(),
                    isLoading = uiState.isSubmitting,
                )
                ResendOtpButton(
                    secondsLeft = uiState.resendInSeconds,
                    enabled = uiState.canResend,
                    onClick = onResendClick,
                )
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    AuthTextButton(
                        text = stringResource(id = R.string.auth_action_change_email),
                        onClick = onChangeEmailClick,
                        enabled = enabled,
                    )
                }
            }
            ForgotPasswordStep.NEW_PASSWORD -> {
                ToeicTextField(
                    value = uiState.newPassword,
                    onValueChange = onNewPasswordChange,
                    label = stringResource(id = R.string.auth_label_new_password),
                    enabled = enabled,
                    errorText =
                        uiState.newPasswordError?.let {
                            stringResource(
                                id = it.messageRes(),
                            )
                        },
                    supportingText =
                        stringResource(
                            id = R.string.auth_hint_reset_password_rules,
                        ),
                    isPassword = true,
                )
                ToeicTextField(
                    value = uiState.confirmPassword,
                    onValueChange = onConfirmPasswordChange,
                    label = stringResource(id = R.string.auth_label_confirm_password),
                    enabled = enabled,
                    errorText =
                        uiState.confirmPasswordError?.let {
                            stringResource(
                                id = it.messageRes(),
                            )
                        },
                    isPassword = true,
                    imeAction = ImeAction.Done,
                    keyboardActions = KeyboardActions(onDone = { submit() }),
                )
                PrimaryButton(
                    text = stringResource(id = R.string.auth_action_save_password),
                    onClick = submit,
                    modifier = Modifier.fillMaxWidth(),
                    isLoading = uiState.isSubmitting,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ForgotPasswordScreenOtpPreview() {
    ToeicSpaceTheme {
        ForgotPasswordScreen(
            uiState =
                ForgotPasswordUiState(
                    step = ForgotPasswordStep.OTP,
                    email = "oysteic@toeicspace.vn",
                    resendInSeconds = 30,
                ),
            onEmailChange = {},
            onOtpChange = {},
            onNewPasswordChange = {},
            onConfirmPasswordChange = {},
            onSubmit = {},
            onResendClick = {},
            onChangeEmailClick = {},
            onBackClick = {},
        )
    }
}
