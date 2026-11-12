package com.toeicspace.android.feature.auth.presentation.changepassword

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.toeicspace.android.R
import com.toeicspace.android.core.ui.components.DetailScaffold
import com.toeicspace.android.core.ui.components.PrimaryButton
import com.toeicspace.android.core.ui.components.ToeicCard
import com.toeicspace.android.core.ui.components.ToeicTextField
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme
import com.toeicspace.android.feature.auth.presentation.common.AuthMessageBanner
import com.toeicspace.android.feature.auth.presentation.common.AuthTextButton
import com.toeicspace.android.feature.auth.presentation.common.messageRes
import com.toeicspace.android.feature.auth.presentation.verifyemail.OtpField
import com.toeicspace.android.feature.auth.presentation.verifyemail.ResendOtpButton

@Composable
internal fun ChangePasswordRoute(
    onBackClick: () -> Unit,
    onPasswordChanged: (email: String) -> Unit,
    onSessionEnded: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChangePasswordViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isCompleted, uiState.isSessionEnded) {
        when {
            uiState.isCompleted -> onPasswordChanged(uiState.email)
            uiState.isSessionEnded -> onSessionEnded()
            else -> return@LaunchedEffect
        }
        viewModel.onNavigationHandled()
    }

    ChangePasswordScreen(
        uiState = uiState,
        onCurrentPasswordChange = viewModel::onCurrentPasswordChange,
        onNewPasswordChange = viewModel::onNewPasswordChange,
        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
        onOtpChange = viewModel::onOtpChange,
        onSubmit = viewModel::onSubmit,
        onResendClick = viewModel::onResendClick,
        onEditPasswordsClick = viewModel::onEditPasswordsClick,
        onBackClick = onBackClick,
        modifier = modifier,
    )
}

/** Opened from the Profile tab, so it uses the in-app page frame rather than the auth one. */
@Composable
internal fun ChangePasswordScreen(
    uiState: ChangePasswordUiState,
    onCurrentPasswordChange: (String) -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onOtpChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onResendClick: () -> Unit,
    onEditPasswordsClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val enabled = !uiState.isSubmitting
    val submit = {
        focusManager.clearFocus()
        onSubmit()
    }
    val isOtpStep = uiState.step == ChangePasswordStep.OTP
    BackHandler(enabled = isOtpStep, onBack = onEditPasswordsClick)

    DetailScaffold(
        title = stringResource(id = R.string.profile_action_change_password),
        onBackClick = if (isOtpStep) onEditPasswordsClick else onBackClick,
        modifier = modifier,
    ) {
        ToeicCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text =
                    if (isOtpStep) {
                        stringResource(id = R.string.auth_body_forgot_otp, uiState.email)
                    } else {
                        stringResource(id = R.string.auth_body_change_password)
                    },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            uiState.noticeMessage?.let {
                AuthMessageBanner(text = stringResource(id = it), isError = false)
            }
            uiState.errorMessage?.let {
                AuthMessageBanner(text = stringResource(id = it), isError = true)
            }

            if (isOtpStep) {
                OtpField(
                    otp = uiState.otp,
                    onOtpChange = onOtpChange,
                    onDone = submit,
                    enabled = enabled,
                    errorText = uiState.otpError?.let { stringResource(id = it.messageRes()) },
                )
                PrimaryButton(
                    text = stringResource(id = R.string.auth_action_save_password),
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
                        text = stringResource(id = R.string.auth_action_edit_passwords),
                        onClick = onEditPasswordsClick,
                        enabled = enabled,
                    )
                }
            } else {
                ToeicTextField(
                    value = uiState.currentPassword,
                    onValueChange = onCurrentPasswordChange,
                    label = stringResource(id = R.string.auth_label_current_password),
                    enabled = enabled,
                    errorText =
                        uiState.currentPasswordError?.let { stringResource(id = it.messageRes()) },
                    isPassword = true,
                )
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
                        uiState.confirmPasswordError?.let { stringResource(id = it.messageRes()) },
                    isPassword = true,
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
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ChangePasswordScreenPasswordsPreview() {
    ToeicSpaceTheme {
        ChangePasswordScreen(
            uiState = ChangePasswordUiState(email = "minhanh@example.com"),
            onCurrentPasswordChange = {},
            onNewPasswordChange = {},
            onConfirmPasswordChange = {},
            onOtpChange = {},
            onSubmit = {},
            onResendClick = {},
            onEditPasswordsClick = {},
            onBackClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ChangePasswordScreenOtpPreview() {
    ToeicSpaceTheme {
        ChangePasswordScreen(
            uiState =
                ChangePasswordUiState(
                    step = ChangePasswordStep.OTP,
                    email = "minhanh@example.com",
                    resendInSeconds = 42,
                ),
            onCurrentPasswordChange = {},
            onNewPasswordChange = {},
            onConfirmPasswordChange = {},
            onOtpChange = {},
            onSubmit = {},
            onResendClick = {},
            onEditPasswordsClick = {},
            onBackClick = {},
        )
    }
}
