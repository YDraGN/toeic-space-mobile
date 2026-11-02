package com.toeicspace.android.feature.auth.presentation.verifyemail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
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

@Composable
internal fun VerifyEmailRoute(
    email: String,
    challengeId: String,
    onBackClick: () -> Unit,
    onVerified: (email: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VerifyEmailViewModel =
        hiltViewModel<VerifyEmailViewModel, VerifyEmailViewModel.Factory>(
            creationCallback = { factory -> factory.create(email, challengeId) },
        ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isVerified) {
        if (!uiState.isVerified) return@LaunchedEffect
        onVerified(uiState.email)
        viewModel.onNavigationHandled()
    }

    VerifyEmailScreen(
        uiState = uiState,
        onOtpChange = viewModel::onOtpChange,
        onSubmit = viewModel::onSubmit,
        onResendClick = viewModel::onResendClick,
        onBackClick = onBackClick,
        modifier = modifier,
    )
}

@Composable
internal fun VerifyEmailScreen(
    uiState: VerifyEmailUiState,
    onOtpChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onResendClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AuthScaffold(
        title = stringResource(id = R.string.auth_title_verify_email),
        subtitle = stringResource(id = R.string.auth_body_verify_email, uiState.email),
        mascotRes = R.drawable.oy2_waiting,
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

        OtpField(
            otp = uiState.otp,
            onOtpChange = onOtpChange,
            onDone = onSubmit,
            enabled = !uiState.isSubmitting,
            errorText = uiState.otpError?.let { stringResource(id = it.messageRes()) },
        )
        PrimaryButton(
            text = stringResource(id = R.string.auth_action_verify),
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth(),
            isLoading = uiState.isSubmitting,
        )
        ResendOtpButton(
            secondsLeft = uiState.resendInSeconds,
            enabled = uiState.canResend,
            onClick = onResendClick,
        )
    }
}

@Composable
internal fun OtpField(
    otp: String,
    onOtpChange: (String) -> Unit,
    onDone: () -> Unit,
    enabled: Boolean,
    errorText: String?,
    modifier: Modifier = Modifier,
) {
    ToeicTextField(
        value = otp,
        onValueChange = onOtpChange,
        label = stringResource(id = R.string.auth_label_otp),
        modifier = modifier,
        enabled = enabled,
        errorText = errorText,
        keyboardType = KeyboardType.NumberPassword,
        imeAction = ImeAction.Done,
        keyboardActions = KeyboardActions(onDone = { onDone() }),
    )
}

@Composable
internal fun ResendOtpButton(
    secondsLeft: Int,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        AuthTextButton(
            text =
                if (secondsLeft > 0) {
                    pluralStringResource(
                        id = R.plurals.auth_action_resend_otp_in,
                        count = secondsLeft,
                        secondsLeft,
                    )
                } else {
                    stringResource(id = R.string.auth_action_resend_otp)
                },
            onClick = onClick,
            enabled = enabled,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun VerifyEmailScreenPreview() {
    ToeicSpaceTheme {
        VerifyEmailScreen(
            uiState =
                VerifyEmailUiState(
                    email = "oysteic@toeicspace.vn",
                    challengeId = "",
                    otp = "1234",
                    resendInSeconds = 42,
                ),
            onOtpChange = {},
            onSubmit = {},
            onResendClick = {},
            onBackClick = {},
        )
    }
}
