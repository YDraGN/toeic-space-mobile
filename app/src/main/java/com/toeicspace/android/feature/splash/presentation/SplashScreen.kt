package com.toeicspace.android.feature.splash.presentation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.toeicspace.android.R
import com.toeicspace.android.core.ui.components.MascotView
import com.toeicspace.android.core.ui.components.PrimaryButton
import com.toeicspace.android.core.ui.theme.CoralBadge
import com.toeicspace.android.core.ui.theme.DancingScript
import com.toeicspace.android.core.ui.theme.OceanicInk
import com.toeicspace.android.core.ui.theme.PrimaryGradientCenter
import com.toeicspace.android.core.ui.theme.PrimaryGradientStart
import com.toeicspace.android.core.ui.theme.SeaTurquoise
import com.toeicspace.android.core.ui.theme.Spacing
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme

private val MascotGlowBrush =
    Brush.radialGradient(
        colors =
            listOf(
                CoralBadge.copy(alpha = 0.45f),
                SeaTurquoise.copy(alpha = 0.65f),
                Color.Transparent,
            ),
    )

private val ProgressBarBrush =
    Brush.horizontalGradient(colors = listOf(PrimaryGradientCenter, PrimaryGradientStart))

@Composable
internal fun SplashRoute(
    onNavigateToOnboarding: () -> Unit,
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SplashViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState) {
        val readyState = uiState as? SplashUiState.Ready ?: return@LaunchedEffect
        when (readyState.startDestination) {
            StartDestination.ONBOARDING -> onNavigateToOnboarding()
            StartDestination.HOME -> onNavigateToHome()
        }
    }

    SplashScreen(
        uiState = uiState,
        onRetryClick = viewModel::onRetryClick,
        modifier = modifier,
    )
}

@Composable
internal fun SplashScreen(
    uiState: SplashUiState,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isNetworkError = uiState is SplashUiState.NetworkError

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
                    .systemBarsPadding()
                    .padding(horizontal = Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.weight(1f))
            BrandHeader()
            Spacer(modifier = Modifier.height(Spacing.space22))
            MascotView(
                mascotResId = if (isNetworkError) R.drawable.oy2_offline else R.drawable.oy2_base,
                size = 180.dp,
                backgroundBrush = MascotGlowBrush,
            )
            Spacer(modifier = Modifier.height(20.dp))
            if (isNetworkError) {
                NetworkErrorCard(onRetryClick = onRetryClick)
            } else {
                PearlLoadingIndicator()
            }
            Spacer(modifier = Modifier.weight(1.35f))
        }
    }
}

@Composable
private fun BrandHeader(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_brand_header),
            contentDescription = stringResource(id = R.string.common_cd_logo),
            modifier =
                Modifier
                    .fillMaxWidth(0.9f)
                    .widthIn(min = 200.dp, max = 300.dp),
            contentScale = ContentScale.Fit,
        )
        Text(
            text = stringResource(id = R.string.splash_subtitle_tagline),
            fontFamily = DancingScript,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Medium,
            fontSize = 22.sp,
            color = OceanicInk,
        )
    }
}

@Composable
private fun PearlLoadingIndicator(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "PearlWave")
    val pearlScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "PearlPulse",
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_glowing_pearl),
            contentDescription = null,
            modifier =
                Modifier
                    .size(Spacing.xxl)
                    .graphicsLayer {
                        scaleX = pearlScale
                        scaleY = pearlScale
                    },
        )
        Spacer(modifier = Modifier.height(10.dp))
        SmoothLinearProgressBar()
        Spacer(modifier = Modifier.height(Spacing.smd))
        Text(
            text = stringResource(id = R.string.splash_msg_loading),
            style = MaterialTheme.typography.bodySmall,
            color = OceanicInk,
        )
    }
}

@Composable
private fun SmoothLinearProgressBar(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "SmoothLoading")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Restart,
            ),
        label = "ProgressOffset",
    )

    BoxWithConstraints(
        modifier =
            modifier
                .fillMaxWidth(0.55f)
                .widthIn(min = 140.dp, max = 220.dp)
                .height(5.dp)
                .clip(CircleShape)
                .background(PrimaryGradientStart.copy(alpha = 0.18f)),
    ) {
        val trackWidth = maxWidth
        val barWidth = trackWidth * 0.45f
        Box(
            modifier =
                Modifier
                    .width(barWidth)
                    .fillMaxHeight()
                    .graphicsLayer {
                        // Slide from fully hidden on the left to fully hidden on the right.
                        val travel = (trackWidth + barWidth).toPx()
                        translationX = -barWidth.toPx() + travel * progress
                    }.clip(CircleShape)
                    .background(ProgressBarBrush),
        )
    }
}

@Composable
private fun NetworkErrorCard(
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(Spacing.lg)
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Column(
            modifier = Modifier.padding(Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            Text(
                text = stringResource(id = R.string.splash_title_network_error),
                style = MaterialTheme.typography.headlineSmall,
                color = OceanicInk,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(id = R.string.splash_body_network_error),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            PrimaryButton(
                text = stringResource(id = R.string.common_action_retry),
                onClick = onRetryClick,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = Spacing.smd),
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_refresh),
                        contentDescription = null,
                        tint = Color.White,
                    )
                },
            )
        }
    }
}

@Preview(name = "Splash - Loading", showBackground = true)
@PreviewScreenSizes
@Composable
private fun SplashScreenLoadingPreview() {
    ToeicSpaceTheme {
        SplashScreen(uiState = SplashUiState.Loading, onRetryClick = {})
    }
}

@Preview(name = "Splash - Network error", showBackground = true)
@Composable
private fun SplashScreenNetworkErrorPreview() {
    ToeicSpaceTheme {
        SplashScreen(uiState = SplashUiState.NetworkError, onRetryClick = {})
    }
}
