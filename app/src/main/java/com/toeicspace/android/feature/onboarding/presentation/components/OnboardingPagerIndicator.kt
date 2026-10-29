package com.toeicspace.android.feature.onboarding.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.toeicspace.android.core.ui.theme.PillShape
import com.toeicspace.android.core.ui.theme.PrimaryBorder
import com.toeicspace.android.core.ui.theme.Spacing
import com.toeicspace.android.core.ui.theme.SubtleBorder
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme

@Composable
internal fun OnboardingPagerIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { index ->
            val isSelected = index == currentPage
            val width by animateDpAsState(
                targetValue = if (isSelected) 24.dp else 8.dp,
                animationSpec = tween(durationMillis = 300),
                label = "IndicatorWidth",
            )
            val color by animateColorAsState(
                targetValue = if (isSelected) PrimaryBorder else SubtleBorder,
                animationSpec = tween(durationMillis = 300),
                label = "IndicatorColor",
            )
            Box(
                modifier =
                    Modifier
                        .height(8.dp)
                        .width(width)
                        .clip(PillShape)
                        .background(color),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingPagerIndicatorPreview() {
    ToeicSpaceTheme {
        OnboardingPagerIndicator(
            pageCount = 4,
            currentPage = 1,
            modifier = Modifier.padding(Spacing.sm),
        )
    }
}
