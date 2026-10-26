package com.toeicspace.android.feature.onboarding.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.toeicspace.android.R
import com.toeicspace.android.core.ui.components.AppTopBar
import com.toeicspace.android.core.ui.components.BrandLogo
import com.toeicspace.android.core.ui.components.MascotView
import com.toeicspace.android.core.ui.components.PillButton
import com.toeicspace.android.core.ui.theme.OceanicInk
import com.toeicspace.android.core.ui.theme.PrimaryBorder
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme
import com.toeicspace.android.core.util.parseHtmlToAnnotatedString
import com.toeicspace.android.feature.onboarding.presentation.components.OnboardingAuthOptCard
import com.toeicspace.android.feature.onboarding.presentation.components.OnboardingCard
import com.toeicspace.android.feature.onboarding.presentation.model.OnboardingSlide
import com.toeicspace.android.feature.onboarding.presentation.model.OnboardingTag
import kotlinx.coroutines.launch

@Composable
fun rememberOnboardingSlides(): List<OnboardingSlide> =
    listOf(
        OnboardingSlide(
            mascotResId = R.drawable.oy2_hello,
            greeting = stringResource(id = R.string.onboarding_greeting_1),
            title = stringResource(id = R.string.onboarding_title_1),
            description =
                stringResource(id = R.string.onboarding_desc_1)
                    .parseHtmlToAnnotatedString(PrimaryBorder),
            tags =
                listOf(
                    OnboardingTag("🔥", stringResource(id = R.string.onboarding_tag_1_1)),
                    OnboardingTag("📊", stringResource(id = R.string.onboarding_tag_1_2)),
                ),
        ),
        OnboardingSlide(
            mascotResId = R.drawable.oy2_search,
            greeting = stringResource(id = R.string.onboarding_greeting_2),
            title = stringResource(id = R.string.onboarding_title_2),
            description =
                stringResource(id = R.string.onboarding_desc_2)
                    .parseHtmlToAnnotatedString(PrimaryBorder),
            tags =
                listOf(
                    OnboardingTag("🎯", stringResource(id = R.string.onboarding_tag_2_1)),
                    OnboardingTag("💰", stringResource(id = R.string.onboarding_tag_2_2)),
                    OnboardingTag("⭐", stringResource(id = R.string.onboarding_tag_2_3)),
                ),
        ),
        OnboardingSlide(
            mascotResId = R.drawable.oy2_listening,
            greeting = stringResource(id = R.string.onboarding_greeting_3),
            title = stringResource(id = R.string.onboarding_title_3),
            description =
                stringResource(id = R.string.onboarding_desc_3)
                    .parseHtmlToAnnotatedString(PrimaryBorder),
            tags =
                listOf(
                    OnboardingTag("🎧", stringResource(id = R.string.onboarding_tag_3_1)),
                    OnboardingTag("📖", stringResource(id = R.string.onboarding_tag_3_2)),
                    OnboardingTag("⏱️", stringResource(id = R.string.onboarding_tag_3_3)),
                ),
        ),
    )

@Composable
fun OnboardingRoute(
    onNavigate: (OnboardingNavTarget) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    LaunchedEffect(viewModel) {
        viewModel.navEvents.collect { target ->
            onNavigate(target)
        }
    }

    val slides = rememberOnboardingSlides()
    OnboardingScreen(
        slides = slides,
        onSignUpClick = viewModel::onRegisterSelected,
        onLogInClick = viewModel::onLoginSelected,
        onGuestClick = viewModel::onGuestSelected,
        modifier = modifier,
    )
}

@Composable
fun OnboardingScreen(
    slides: List<OnboardingSlide>,
    onSignUpClick: () -> Unit,
    onLogInClick: () -> Unit,
    onGuestClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val totalPages = slides.size + 1
    val pagerState = rememberPagerState(pageCount = { totalPages })
    val coroutineScope = rememberCoroutineScope()

    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.img_splash_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .systemBarsPadding(),
        ) {
            AppTopBar(
                leadingContent = { BrandLogo() },
                trailingContent = {
                    if (pagerState.currentPage < totalPages - 1) {
                        PillButton(
                            text = stringResource(id = R.string.btn_skip),
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(totalPages - 1)
                                }
                            },
                            trailingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_chevron_right),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = OceanicInk,
                                )
                            },
                        )
                    }
                },
            )

            HorizontalPager(
                state = pagerState,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f),
            ) { page ->
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(modifier = Modifier.weight(0.25f))
                    val mascotRes =
                        if (page < slides.size) {
                            slides[page].mascotResId
                        } else {
                            R.drawable.oy2_cheer
                        }
                    val greetingText =
                        if (page < slides.size) {
                            slides[page].greeting
                        } else {
                            stringResource(id = R.string.txt_greeting)
                        }
                    MascotView(
                        mascotResId = mascotRes,
                        size = 175.dp,
                        backgroundBrush =
                            Brush.radialGradient(
                                colors =
                                    listOf(
                                        Color.White.copy(alpha = 0.7f),
                                        Color.White.copy(alpha = 0.2f),
                                        Color.Transparent,
                                    ),
                            ),
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = greetingText,
                        style =
                            MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = OceanicInk,
                            ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier,
                    )
                    Spacer(modifier = Modifier.weight(0.45f))
                    if (page < slides.size) {
                        OnboardingCard(
                            slide = slides[page],
                            pageCount = totalPages,
                            currentPage = page,
                            onNextClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(page + 1)
                                }
                            },
                        )
                    } else {
                        OnboardingAuthOptCard(
                            onSignUpClick = onSignUpClick,
                            onLogInClick = onLogInClick,
                            onGuestClick = onGuestClick,
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Preview(name = "Onboarding Screen Preview", showBackground = true)
@Composable
private fun OnboardingScreenPreview() {
    ToeicSpaceTheme {
        OnboardingScreen(
            slides = emptyList(),
            onSignUpClick = {},
            onLogInClick = {},
            onGuestClick = {},
        )
    }
}
