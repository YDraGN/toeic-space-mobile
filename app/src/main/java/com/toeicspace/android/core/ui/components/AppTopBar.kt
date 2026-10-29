package com.toeicspace.android.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.toeicspace.android.R
import com.toeicspace.android.core.ui.theme.BrandGradientBrush
import com.toeicspace.android.core.ui.theme.OceanicInk
import com.toeicspace.android.core.ui.theme.Spacing
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme

@Composable
fun AppTopBar(
    leadingContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    trailingContent: @Composable () -> Unit = {},
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(64.dp)
                .padding(horizontal = Spacing.gutter, vertical = Spacing.smd),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leadingContent()
        trailingContent()
    }
}

@Preview(name = "TopBar - Onboarding", showBackground = true, backgroundColor = 0xFFFAF9FF)
@Composable
private fun AppTopBarOnboardingPreview() {
    ToeicSpaceTheme {
        AppTopBar(
            leadingContent = { BrandLogo() },
            trailingContent = {
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
            },
        )
    }
}

@Preview(name = "TopBar - Home Guest", showBackground = true, backgroundColor = 0xFFFAF9FF)
@Composable
private fun AppTopBarHomeGuestPreview() {
    ToeicSpaceTheme {
        AppTopBar(
            leadingContent = { BrandLogo() },
            trailingContent = {
                PillButton(
                    text = stringResource(id = R.string.auth_title_sign_in),
                    onClick = {},
                    backgroundBrush = BrandGradientBrush,
                    contentColor = Color.White,
                )
            },
        )
    }
}

@Preview(name = "TopBar - Home Signed In", showBackground = true, backgroundColor = 0xFFFAF9FF)
@Composable
private fun AppTopBarHomeSignedInPreview() {
    ToeicSpaceTheme {
        AppTopBar(
            leadingContent = { BrandLogo() },
            trailingContent = {
                ToeicIconButton(
                    onClick = {},
                    badgeCount = 3,
                    contentDescription = stringResource(id = R.string.common_cd_notifications),
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_bell),
                        contentDescription = null,
                    )
                }
            },
        )
    }
}
