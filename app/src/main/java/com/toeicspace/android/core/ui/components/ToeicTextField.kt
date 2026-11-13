package com.toeicspace.android.core.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import com.toeicspace.android.R
import com.toeicspace.android.core.ui.theme.AppTextStyles
import com.toeicspace.android.core.ui.theme.HintText
import com.toeicspace.android.core.ui.theme.OceanicInk
import com.toeicspace.android.core.ui.theme.PrimaryBorder
import com.toeicspace.android.core.ui.theme.Spacing
import com.toeicspace.android.core.ui.theme.SubtleBorder
import com.toeicspace.android.core.ui.theme.ToeicSpaceShapes
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme
import com.toeicspace.android.core.ui.theme.ValidBorder

private const val MULTI_LINE_MIN_LINES = 3

/**
 * Outlined text field with the app styling. [errorText] replaces [supportingText] when set.
 * With [isPassword] the value is masked and a show/hide toggle is added. [singleLine] false gives a
 * box of a few lines for longer text such as a short bio. [isRequired] adds a red asterisk to the
 * label; forms that use it should say what the asterisk means. [isValid] turns the border green once
 * the value passed its check; an error always wins over it. [testTag] goes on the input itself,
 * so UI tests (Maestro reads it as a resource id) tap and type into the field, not its helper text.
 * While focused, the whole field including the helper text is kept above the keyboard: by default
 * Compose only scrolls the input itself into view, which leaves hints such as the password rules
 * hidden behind it.
 */
@Composable
fun ToeicTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isRequired: Boolean = false,
    errorText: String? = null,
    supportingText: String? = null,
    isPassword: Boolean = false,
    singleLine: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    testTag: String? = null,
    isValid: Boolean = false,
) {
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
    val helperText = errorText ?: supportingText
    val typography = MaterialTheme.typography
    val errorColor = MaterialTheme.colorScheme.error
    val fieldLabel =
        remember(label, isRequired, errorColor) {
            buildAnnotatedString {
                append(label)
                if (isRequired) withStyle(SpanStyle(color = errorColor)) { append(" *") }
            }
        }

    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    var isFocused by remember { mutableStateOf(false) }
    // Changes on every frame of the keyboard animation; the last run, once it settles, wins.
    val imeBottom = WindowInsets.ime.getBottom(LocalDensity.current)
    LaunchedEffect(isFocused, imeBottom, helperText) {
        if (isFocused) bringIntoViewRequester.bringIntoView()
    }

    Column(
        modifier =
            modifier
                .bringIntoViewRequester(bringIntoViewRequester)
                .onFocusChanged { isFocused = it.hasFocus },
    ) {
        // The field animates its label between bodyLarge and bodySmall. The app's bodyLarge (18sp)
        // makes forms bulky, so both are stepped down here for a compact field.
        MaterialTheme(
            typography =
                typography.copy(
                    bodyLarge = typography.bodyMedium,
                    bodySmall = AppTextStyles.Caption,
                ),
        ) {
            ToeicOutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = fieldLabel,
                enabled = enabled,
                isError = errorText != null,
                isPassword = isPassword,
                singleLine = singleLine,
                isPasswordVisible = isPasswordVisible,
                onTogglePasswordVisibility = { isPasswordVisible = !isPasswordVisible },
                keyboardType = keyboardType,
                imeAction = imeAction,
                keyboardActions = keyboardActions,
                testTag = testTag,
                isValid = isValid && errorText == null,
            )
        }
        if (helperText != null) {
            // Starts at the field's edge, like Google and Material forms, not indented to the text.
            Text(
                text = helperText,
                style = AppTextStyles.HelperText,
                fontStyle = if (errorText != null) FontStyle.Normal else FontStyle.Italic,
                color = if (errorText != null) errorColor else HintText,
                modifier = Modifier.padding(start = Spacing.xs, top = Spacing.xs, end = Spacing.xs),
            )
        }
    }
}

@Composable
private fun ToeicOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: AnnotatedString,
    enabled: Boolean,
    isError: Boolean,
    isPassword: Boolean,
    singleLine: Boolean,
    isPasswordVisible: Boolean,
    onTogglePasswordVisibility: () -> Unit,
    keyboardType: KeyboardType,
    imeAction: ImeAction,
    keyboardActions: KeyboardActions,
    testTag: String?,
    isValid: Boolean,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier =
            Modifier
                .fillMaxWidth()
                .then(if (testTag != null) Modifier.testTag(testTag) else Modifier),
        enabled = enabled,
        textStyle = MaterialTheme.typography.bodyLarge,
        label = { Text(text = label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        isError = isError,
        singleLine = singleLine,
        minLines = if (singleLine) 1 else MULTI_LINE_MIN_LINES,
        shape = ToeicSpaceShapes.small,
        visualTransformation =
            if (isPassword && !isPasswordVisible) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
        keyboardOptions =
            KeyboardOptions(
                keyboardType = if (isPassword) KeyboardType.Password else keyboardType,
                imeAction = imeAction,
                autoCorrectEnabled = false,
            ),
        keyboardActions = keyboardActions,
        trailingIcon =
            if (isPassword) {
                {
                    IconButton(onClick = onTogglePasswordVisibility) {
                        Icon(
                            painter =
                                painterResource(
                                    id =
                                        if (isPasswordVisible) {
                                            R.drawable.ic_eye_off
                                        } else {
                                            R.drawable.ic_eye
                                        },
                                ),
                            contentDescription =
                                stringResource(
                                    id =
                                        if (isPasswordVisible) {
                                            R.string.auth_cd_hide_password
                                        } else {
                                            R.string.auth_cd_show_password
                                        },
                                ),
                        )
                    }
                }
            } else {
                null
            },
        colors =
            OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (isValid) ValidBorder else PrimaryBorder,
                unfocusedBorderColor = if (isValid) ValidBorder else SubtleBorder,
                focusedLabelColor = if (isValid) ValidBorder else PrimaryBorder,
                unfocusedLabelColor = if (isValid) ValidBorder else Color.Unspecified,
                focusedTextColor = OceanicInk,
                unfocusedTextColor = OceanicInk,
            ),
    )
}

@Preview(showBackground = true)
@Composable
private fun ToeicTextFieldPreview() {
    ToeicSpaceTheme {
        Column(modifier = Modifier.padding(Spacing.md)) {
            ToeicTextField(value = "oysteic@toeicspace.vn", onValueChange = {}, label = "Email")
            ToeicTextField(
                value = "secret",
                onValueChange = {},
                label = "Mật khẩu",
                isPassword = true,
                errorText = "Mật khẩu cần ít nhất 8 ký tự",
            )
        }
    }
}
