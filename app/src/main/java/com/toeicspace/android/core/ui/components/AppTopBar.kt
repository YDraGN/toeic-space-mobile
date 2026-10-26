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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.toeicspace.android.R
import com.toeicspace.android.core.ui.theme.OceanicInk
import com.toeicspace.android.core.ui.theme.Spacing
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme

@Composable
fun AppTopBar(
    modifier: Modifier = Modifier,
    leadingContent: @Composable () -> Unit,
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
                    text = "Bỏ qua",
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
                    text = "Đăng nhập",
                    onClick = {},
                    backgroundBrush = BrandGradientBrush,
                    contentColor = Color.White,
                )
            },
        )
    }
}

@Preview(name = "TopBar - Home Logged In", showBackground = true, backgroundColor = 0xFFFAF9FF)
@Composable
private fun AppTopBarHomeLoggedInPreview() {
    ToeicSpaceTheme {
        AppTopBar(
            leadingContent = {
                BrandLogo()
            },
            trailingContent = {
                IconButton(
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_bell),
                            contentDescription = "Thông báo",
                        )
                    },
                    onClick = {},
                    hasBadge = true,
                    badgeCount = 3,
                )
            },
        )
    }
}
