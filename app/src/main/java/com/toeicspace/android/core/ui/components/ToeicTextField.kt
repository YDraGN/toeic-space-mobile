package com.toeicspace.android.core.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import androidx.compose.ui.unit.sp
import com.toeicspace.android.R
import com.toeicspace.android.core.ui.theme.AppTextStyles
import com.toeicspace.android.core.ui.theme.OceanicInk
import com.toeicspace.android.core.ui.theme.PrimaryBorder
import com.toeicspace.android.core.ui.theme.Spacing
import com.toeicspace.android.core.ui.theme.SubtleBorder
import com.toeicspace.android.core.ui.theme.ToeicSpaceShapes
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme

/** Hint under a field: italic and a cooler blue, so it never reads as the field's own placeholder. */
private val HintColor = Color(0xFF4F6E96)

/**
 * Outlined text field with the app styling. [errorText] replaces [supportingText] when set.
 * With [isPassword] the value is masked and a show/hide toggle is added. [isRequired] adds a red
 * asterisk to the label; forms that use it should say what the asterisk means.
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
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
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

    Column(modifier = modifier) {
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
                isPasswordVisible = isPasswordVisible,
                onTogglePasswordVisibility = { isPasswordVisible = !isPasswordVisible },
                keyboardType = keyboardType,
                imeAction = imeAction,
                keyboardActions = keyboardActions,
            )
        }
        if (helperText != null) {
            // Starts at the field's edge, like Google and Material forms, not indented to the text.
            Text(
                text = helperText,
                style = HelperTextStyle,
                fontStyle = if (errorText != null) FontStyle.Normal else FontStyle.Italic,
                color = if (errorText != null) errorColor else HintColor,
                modifier = Modifier.padding(start = Spacing.xs, top = Spacing.xs, end = Spacing.xs),
            )
        }
    }
}

/** 12sp, as in Material's supporting text: a step below the 13sp floating label. */
private val HelperTextStyle = AppTextStyles.Caption.copy(fontSize = 12.sp, lineHeight = 16.sp)

@Composable
private fun ToeicOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: AnnotatedString,
    enabled: Boolean,
    isError: Boolean,
    isPassword: Boolean,
    isPasswordVisible: Boolean,
    onTogglePasswordVisibility: () -> Unit,
    keyboardType: KeyboardType,
    imeAction: ImeAction,
    keyboardActions: KeyboardActions,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        textStyle = MaterialTheme.typography.bodyLarge,
        label = { Text(text = label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        isError = isError,
        singleLine = true,
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
                focusedBorderColor = PrimaryBorder,
                unfocusedBorderColor = SubtleBorder,
                focusedLabelColor = PrimaryBorder,
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
