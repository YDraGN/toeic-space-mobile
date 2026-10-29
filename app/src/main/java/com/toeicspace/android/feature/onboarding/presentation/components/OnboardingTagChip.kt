package com.toeicspace.android.feature.onboarding.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.toeicspace.android.R
import com.toeicspace.android.core.ui.theme.OceanicInk
import com.toeicspace.android.core.ui.theme.PillShape
import com.toeicspace.android.core.ui.theme.SecondaryButtonBackground
import com.toeicspace.android.core.ui.theme.Spacing
import com.toeicspace.android.core.ui.theme.SubtleBorder
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme
import com.toeicspace.android.feature.onboarding.presentation.model.OnboardingTag

@Composable
internal fun OnboardingTagChip(
    tag: OnboardingTag,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .clip(PillShape)
                .background(SecondaryButtonBackground)
                .border(1.dp, SubtleBorder, PillShape)
                .padding(horizontal = 6.dp, vertical = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = tag.icon,
            fontSize = 13.sp,
        )
        Text(
            text = stringResource(id = tag.labelRes),
            style =
                MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = OceanicInk,
                ),
            textAlign = TextAlign.Center,
            maxLines = 2,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingTagChipPreview() {
    ToeicSpaceTheme {
        OnboardingTagChip(
            tag = OnboardingTag("🔥", R.string.onboarding_tag_1_1),
            modifier = Modifier.padding(Spacing.sm),
        )
    }
}
