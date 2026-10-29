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
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.toeicspace.android.R
import com.toeicspace.android.core.ui.components.AppTopBar
import com.toeicspace.android.core.ui.components.BrandLogo
import com.toeicspace.android.core.ui.components.MascotView
import com.toeicspace.android.core.ui.components.PillButton
import com.toeicspace.android.core.ui.theme.OceanicInk
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme
import com.toeicspace.android.feature.onboarding.presentation.components.OnboardingAuthOptionsCard
import com.toeicspace.android.feature.onboarding.presentation.components.OnboardingCard
import com.toeicspace.android.feature.onboarding.presentation.model.OnboardingSlide
import com.toeicspace.android.feature.onboarding.presentation.model.OnboardingSlides
import kotlinx.coroutines.launch

private val MascotGlowBrush =
    Brush.radialGradient(
        colors =
            listOf(
                Color.White.copy(alpha = 0.7f),
                Color.White.copy(alpha = 0.2f),
                Color.Transparent,
            ),
    )

@Composable
internal fun OnboardingRoute(
    onNavigateToSignUp: () -> Unit,
    onNavigateToSignIn: () -> Unit,
    onContinueAsGuest: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Navigate only after the ViewModel has persisted the onboarding flag.
    LaunchedEffect(uiState.nextStep) {
        when (uiState.nextStep) {
            OnboardingNextStep.SIGN_UP -> onNavigateToSignUp()
            OnboardingNextStep.SIGN_IN -> onNavigateToSignIn()
            OnboardingNextStep.GUEST -> onContinueAsGuest()
            null -> return@LaunchedEffect
        }
        viewModel.onNavigationHandled()
    }

    OnboardingScreen(
        slides = OnboardingSlides,
        isCompleting = uiState.isCompleting,
        onSignUpClick = viewModel::onSignUpClick,
        onSignInClick = viewModel::onSignInClick,
        onContinueAsGuestClick = viewModel::onContinueAsGuestClick,
        modifier = modifier,
    )
}

@Composable
internal fun OnboardingScreen(
    slides: List<OnboardingSlide>,
    isCompleting: Boolean,
    onSignUpClick: () -> Unit,
    onSignInClick: () -> Unit,
    onContinueAsGuestClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Intro slides followed by one auth options page.
    val pageCount = slides.size + 1
    val authPage = pageCount - 1
    val pagerState = rememberPagerState(pageCount = { pageCount })
    val coroutineScope = rememberCoroutineScope()

    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.img_splash_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        Column(modifier = Modifier.systemBarsPadding()) {
            AppTopBar(
                leadingContent = { BrandLogo() },
                trailingContent = {
                    if (pagerState.currentPage < authPage) {
                        PillButton(
                            text = stringResource(id = R.string.common_action_skip),
                            onClick = {
                                coroutineScope.launch { pagerState.animateScrollToPage(authPage) }
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
                // A null slide means the final auth options page.
                val slide = slides.getOrNull(page)
                val greetingRes = slide?.greetingRes ?: R.string.onboarding_title_ready
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(modifier = Modifier.weight(0.25f))
                    MascotView(
                        mascotResId = slide?.mascotRes ?: R.drawable.oy2_cheer,
                        size = 175.dp,
                        backgroundBrush = MascotGlowBrush,
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = stringResource(id = greetingRes),
                        style =
                            MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = OceanicInk,
                            ),
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.weight(0.45f))
                    if (slide != null) {
                        OnboardingCard(
                            slide = slide,
                            pageCount = pageCount,
                            currentPage = page,
                            isLastSlide = page == slides.lastIndex,
                            onNextClick = {
                                coroutineScope.launch { pagerState.animateScrollToPage(page + 1) }
                            },
                        )
                    } else {
                        OnboardingAuthOptionsCard(
                            onSignUpClick = onSignUpClick,
                            onSignInClick = onSignInClick,
                            onContinueAsGuestClick = onContinueAsGuestClick,
                            enabled = !isCompleting,
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@PreviewScreenSizes
@PreviewFontScale
@Composable
private fun OnboardingScreenPreview() {
    ToeicSpaceTheme {
        OnboardingScreen(
            slides = OnboardingSlides,
            isCompleting = false,
            onSignUpClick = {},
            onSignInClick = {},
            onContinueAsGuestClick = {},
        )
    }
}
