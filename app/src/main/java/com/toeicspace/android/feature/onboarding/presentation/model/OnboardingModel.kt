package com.toeicspace.android.feature.onboarding.presentation.model

import androidx.annotation.DrawableRes
import androidx.compose.ui.text.AnnotatedString

data class OnboardingTag(
    val icon: String,
    val label: String,
)

data class OnboardingSlide(
    @get:DrawableRes val mascotResId: Int,
    val greeting: String,
    val title: String,
    val description: AnnotatedString,
    val tags: List<OnboardingTag>,
)
