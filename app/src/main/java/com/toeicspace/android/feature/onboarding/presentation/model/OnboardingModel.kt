package com.toeicspace.android.feature.onboarding.presentation.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.toeicspace.android.R

data class OnboardingTag(
    val icon: String,
    @get:StringRes val labelRes: Int,
)

/** One intro slide. [descriptionRes] may contain simple HTML such as `<b>`. */
data class OnboardingSlide(
    @get:DrawableRes val mascotRes: Int,
    @get:StringRes val greetingRes: Int,
    @get:StringRes val titleRes: Int,
    @get:StringRes val descriptionRes: Int,
    val tags: List<OnboardingTag>,
)

internal val OnboardingSlides: List<OnboardingSlide> =
    listOf(
        OnboardingSlide(
            mascotRes = R.drawable.oy2_hello,
            greetingRes = R.string.onboarding_greeting_1,
            titleRes = R.string.onboarding_title_1,
            descriptionRes = R.string.onboarding_desc_1,
            tags =
                listOf(
                    OnboardingTag("🔥", R.string.onboarding_tag_1_1),
                    OnboardingTag("📊", R.string.onboarding_tag_1_2),
                ),
        ),
        OnboardingSlide(
            mascotRes = R.drawable.oy2_search,
            greetingRes = R.string.onboarding_greeting_2,
            titleRes = R.string.onboarding_title_2,
            descriptionRes = R.string.onboarding_desc_2,
            tags =
                listOf(
                    OnboardingTag("🎯", R.string.onboarding_tag_2_1),
                    OnboardingTag("💰", R.string.onboarding_tag_2_2),
                    OnboardingTag("⭐", R.string.onboarding_tag_2_3),
                ),
        ),
        OnboardingSlide(
            mascotRes = R.drawable.oy2_listening,
            greetingRes = R.string.onboarding_greeting_3,
            titleRes = R.string.onboarding_title_3,
            descriptionRes = R.string.onboarding_desc_3,
            tags =
                listOf(
                    OnboardingTag("🎧", R.string.onboarding_tag_3_1),
                    OnboardingTag("📖", R.string.onboarding_tag_3_2),
                    OnboardingTag("⏱️", R.string.onboarding_tag_3_3),
                ),
        ),
    )
