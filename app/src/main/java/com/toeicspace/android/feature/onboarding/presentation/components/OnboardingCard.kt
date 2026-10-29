package com.toeicspace.android.feature.onboarding.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.toeicspace.android.R
import com.toeicspace.android.core.ui.theme.OceanicInk
import com.toeicspace.android.core.ui.theme.PrimaryBorder
import com.toeicspace.android.core.ui.theme.Spacing
import com.toeicspace.android.core.ui.theme.ToeicSpaceShapes
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme
import com.toeicspace.android.core.util.htmlToAnnotatedString
import com.toeicspace.android.feature.onboarding.presentation.model.OnboardingSlide
import com.toeicspace.android.feature.onboarding.presentation.model.OnboardingSlides

/** Intro slide card: title, description, tags, page indicator and next button. */
@Composable
internal fun OnboardingCard(
    slide: OnboardingSlide,
    pageCount: Int,
    currentPage: Int,
    isLastSlide: Boolean,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val descriptionHtml = stringResource(id = slide.descriptionRes)
    val description =
        remember(descriptionHtml) { descriptionHtml.htmlToAnnotatedString(PrimaryBorder) }
    val actionText =
        if (isLastSlide) {
            stringResource(id = R.string.onboarding_action_get_started)
        } else {
            stringResource(id = R.string.common_action_next)
        }

    Surface(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
        shape = ToeicSpaceShapes.large,
        color = Color.White,
        shadowElevation = 8.dp,
    ) {
        Column(
            modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 30.dp, bottom = 24.dp),
        ) {
            Text(
                text = stringResource(id = slide.titleRes),
                style =
                    MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = OceanicInk,
                        lineHeight = 28.sp,
                    ),
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = description,
                style =
                    MaterialTheme.typography.bodyMedium.copy(
                        color = OceanicInk.copy(alpha = 0.75f),
                        lineHeight = 22.sp,
                    ),
            )
            Spacer(modifier = Modifier.height(Spacing.md))

            if (slide.tags.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    slide.tags.forEach { tag ->
                        OnboardingTagChip(tag = tag, modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OnboardingPagerIndicator(pageCount = pageCount, currentPage = currentPage)
                OnboardingActionButton(text = actionText, onClick = onNextClick)
            }
        }
    }
}

@Preview(name = "Slide 1 (2 tags)", showBackground = true, backgroundColor = 0xFFDCEBFF)
@Composable
private fun OnboardingCardFirstSlidePreview() {
    ToeicSpaceTheme {
        OnboardingCard(
            slide = OnboardingSlides.first(),
            pageCount = OnboardingSlides.size + 1,
            currentPage = 0,
            isLastSlide = false,
            onNextClick = {},
            modifier = Modifier.padding(Spacing.md),
        )
    }
}

@Preview(name = "Last slide (3 tags)", showBackground = true, backgroundColor = 0xFFDCEBFF)
@Composable
private fun OnboardingCardLastSlidePreview() {
    ToeicSpaceTheme {
        OnboardingCard(
            slide = OnboardingSlides.last(),
            pageCount = OnboardingSlides.size + 1,
            currentPage = OnboardingSlides.lastIndex,
            isLastSlide = true,
            onNextClick = {},
            modifier = Modifier.padding(Spacing.md),
        )
    }
}
