package com.toeicspace.android.feature.onboarding.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.toeicspace.android.R
import com.toeicspace.android.core.ui.theme.BrandGradientBrush
import com.toeicspace.android.core.ui.theme.PillShape
import com.toeicspace.android.core.ui.theme.Spacing
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme

@Composable
internal fun OnboardingActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .height(46.dp)
                .clip(PillShape)
                .background(BrandGradientBrush)
                .clickable(role = Role.Button, onClick = onClick)
                .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style =
                MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 0.5.sp,
                ),
        )
        Icon(
            painter = painterResource(id = R.drawable.ic_move_right),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Preview
@Composable
private fun OnboardingActionButtonPreview() {
    ToeicSpaceTheme {
        OnboardingActionButton(
            text = stringResource(id = R.string.common_action_next),
            onClick = {},
        )
    }
}
