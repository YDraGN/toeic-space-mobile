package com.toeicspace.android.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.toeicspace.android.R
import com.toeicspace.android.core.ui.theme.BrandGradientBrush
import com.toeicspace.android.core.ui.theme.DisabledBrandGradientBrush
import com.toeicspace.android.core.ui.theme.OceanicInk
import com.toeicspace.android.core.ui.theme.PillButtonBackground
import com.toeicspace.android.core.ui.theme.PillButtonBorder
import com.toeicspace.android.core.ui.theme.PillShape
import com.toeicspace.android.core.ui.theme.Spacing
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme

@Composable
fun PillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundBrush: Brush? = null,
    containerColor: Color = PillButtonBackground,
    contentColor: Color = OceanicInk,
    borderColor: Color? = PillButtonBorder,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
) {
    val backgroundModifier =
        when {
            backgroundBrush == null -> Modifier.background(containerColor)
            enabled -> Modifier.background(backgroundBrush)
            else -> Modifier.background(DisabledBrandGradientBrush)
        }
    val borderModifier =
        if (backgroundBrush == null && borderColor != null) {
            Modifier.border(1.dp, borderColor, PillShape)
        } else {
            Modifier
        }

    Row(
        modifier =
            modifier
                .heightIn(min = 32.dp)
                .clip(PillShape)
                .then(backgroundModifier)
                .then(borderModifier)
                .clickable(enabled = enabled, onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leadingIcon?.invoke()
        Text(
            text = text,
            style =
                MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor,
                ),
            maxLines = 1,
        )
        trailingIcon?.invoke()
    }
}

@Preview(name = "Pill Button", showBackground = true, backgroundColor = 0xFFFAF9FF)
@Composable
private fun PillButtonPreview() {
    ToeicSpaceTheme {
        Row(
            modifier = Modifier.padding(Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.smd),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PillButton(
                text = stringResource(id = R.string.common_action_skip),
                onClick = {},
                trailingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_chevron_right),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = OceanicInk,
                    )
                },
            )
            PillButton(
                text = stringResource(id = R.string.auth_title_sign_in),
                onClick = {},
                backgroundBrush = BrandGradientBrush,
                contentColor = Color.White,
            )
        }
    }
}
