package com.toeicspace.android.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.toeicspace.android.core.ui.theme.PillShape
import com.toeicspace.android.core.ui.theme.SecondaryButtonBackground
import com.toeicspace.android.core.ui.theme.SecondaryButtonBorder
import com.toeicspace.android.core.ui.theme.SecondaryButtonContent
import com.toeicspace.android.core.ui.theme.Spacing
import com.toeicspace.android.core.ui.theme.ToeicSpaceShapes
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = ToeicSpaceShapes.small,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = Spacing.xxl),
        enabled = enabled && !isLoading,
        shape = shape,
        border = BorderStroke(1.dp, SecondaryButtonBorder),
        colors =
            ButtonDefaults.outlinedButtonColors(
                containerColor = SecondaryButtonBackground,
                contentColor = SecondaryButtonContent,
            ),
        contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.space12),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(Spacing.space18),
                strokeWidth = Spacing.space2,
                color = SecondaryButtonContent,
            )
            Spacer(modifier = Modifier.width(Spacing.sm))
        } else if (leadingIcon != null) {
            leadingIcon()
            Spacer(modifier = Modifier.width(Spacing.sm))
        }

        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = SecondaryButtonContent,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        if (!isLoading && trailingIcon != null) {
            Spacer(modifier = Modifier.width(Spacing.sm))
            trailingIcon()
        }
    }
}

@Preview(name = "Secondary - Pill Quick Action", showBackground = true)
@Composable
private fun SecondaryButtonPillPreview() {
    ToeicSpaceTheme {
        Box(modifier = Modifier.padding(Spacing.md)) {
            SecondaryButton(
                text = "Chuỗi Streak",
                shape = PillShape,
                onClick = {},
                leadingIcon = { Text(text = "🔥") },
            )
        }
    }
}
