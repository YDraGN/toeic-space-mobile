package com.toeicspace.android.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.toeicspace.android.R
import com.toeicspace.android.core.ui.theme.BrandGradientBrush
import com.toeicspace.android.core.ui.theme.DisabledBrandGradientBrush
import com.toeicspace.android.core.ui.theme.Spacing
import com.toeicspace.android.core.ui.theme.ToeicSpaceShapes
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = ToeicSpaceShapes.small,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    loadingText: String? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    val isInteractive = enabled && !isLoading
    val brush = if (isInteractive) BrandGradientBrush else DisabledBrandGradientBrush

    Button(
        onClick = onClick,
        modifier =
            modifier
                .background(brush, shape)
                .heightIn(min = Spacing.xxl),
        enabled = isInteractive,
        shape = shape,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
            ),
        contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.sm),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(Spacing.space18),
                strokeWidth = Spacing.space2,
                color = Color.White,
            )
            Spacer(modifier = Modifier.width(Spacing.sm))
        } else if (leadingIcon != null) {
            leadingIcon()
            Spacer(modifier = Modifier.width(Spacing.sm))
        }

        Text(
            text = if (isLoading && loadingText != null) loadingText else text,
            style = MaterialTheme.typography.labelLarge,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        if (!isLoading && trailingIcon != null) {
            Spacer(modifier = Modifier.width(Spacing.sm))
            trailingIcon()
        }
    }
}

@Preview(name = "Primary - Full Width", showBackground = true)
@Composable
private fun PrimaryButtonFullWidthPreview() {
    ToeicSpaceTheme {
        Box(modifier = Modifier.padding(Spacing.md)) {
            PrimaryButton(
                text = "LÀM BÀI KIỂM TRA ĐẦU VÀO NGAY",
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_move_right),
                        contentDescription = null,
                    )
                },
            )
        }
    }
}

@Preview(name = "Primary - Loading", showBackground = true)
@Composable
private fun PrimaryButtonLoadingPreview() {
    ToeicSpaceTheme {
        Box(modifier = Modifier.padding(Spacing.md)) {
            PrimaryButton(
                text = "ĐĂNG NHẬP",
                onClick = {},
                isLoading = true,
                loadingText = "Đang đăng nhập...",
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
