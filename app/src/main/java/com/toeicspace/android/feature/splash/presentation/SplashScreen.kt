package com.toeicspace.android.feature.splash.presentation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
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
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme

/**
 * Route component kết nối ViewModel & Navigation (dùng trong NavHost).
 */
@Composable
fun SplashRoute(
    onNavigate: (SplashNavTarget) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SplashViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState) {
        if (uiState is SplashUiState.Success) {
            onNavigate((uiState as SplashUiState.Success).target)
        }
    }

    SplashScreen(
        uiState = uiState,
        onRetry = viewModel::retryConnection,
        modifier = modifier,
    )
}

/**
 * Screen component hiển thị giao diện thuần (Stateless).
 */
@Composable
fun SplashScreen(
    uiState: SplashUiState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SplashBackground(modifier = modifier.fillMaxSize()) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .systemBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.weight(1f))

            BrandHeader()

            Spacer(modifier = Modifier.height(22.dp))

            val mascotRes =
                if (uiState is SplashUiState.NetworkError) {
                    R.drawable.oy2_offline
                } else {
                    R.drawable.oy2_base
                }

            MascotView(
                mascotResId = mascotRes,
                size = 180.dp,
                backgroundBrush =
                    Brush.radialGradient(
                        colors =
                            listOf(
                                CoralBadge.copy(alpha = 0.45f),
                                SeaTurquoise.copy(alpha = 0.65f),
                                Color.Transparent,
                            ),
                    ),
            )

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                when (uiState) {
                    is SplashUiState.Loading,
                    is SplashUiState.Success,
                    -> {
                        PearlLoadingProgressBar()
                    }

                    is SplashUiState.NetworkError -> {
                        NetworkErrorCard(onRetry = onRetry)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1.35f))
        }
    }
}

@Composable
private fun SplashBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier) {
        Image(
            painter = painterResource(id = R.drawable.img_splash_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        content()
    }
}

/**
 * Header thương hiệu
 */
@Composable
private fun BrandHeader(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_brand_header),
            contentDescription = "ToeicSpace Logo",
            modifier =
                Modifier
                    .fillMaxWidth(0.9f)
                    .widthIn(min = 200.dp, max = 300.dp),
            contentScale = ContentScale.Fit,
        )

        Text(
            text = "Mở sổ tay — Luyện từng lớp xà cừ",
            fontFamily = DancingScript,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Medium,
            fontSize = 22.sp,
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
        val startX = -barWidth
        val currentX = startX + (trackWidth - startX) * progress

        Box(
            modifier =
                Modifier
                    .width(barWidth)
                    .fillMaxHeight()
                    .offset(x = currentX)
                    .clip(CircleShape)
                    .background(
                        brush =
                            Brush.horizontalGradient(
                                colors =
                                    listOf(
                                        PrimaryGradientCenter,
                                        PrimaryGradientStart,
                                    ),
                            ),
                    ),
        )
    }
}

@Composable
private fun PearlLoadingProgressBar(modifier: Modifier = Modifier) {
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
        Box(
            modifier =
                Modifier
                    .size(48.dp)
                    .graphicsLayer {
                        scaleX = pearlScale
                        scaleY = pearlScale
                    },
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_glowing_pearl),
                contentDescription = null,
                modifier = Modifier.size(48.dp),
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        SmoothLinearProgressBar()

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Đang chuẩn bị không gian học cho bạn...",
            style = MaterialTheme.typography.bodySmall,
            color = OceanicInk,
        )
    }
}

/**
 * Card lỗi mạng (dự phòng).
 */
@Composable
private fun NetworkErrorCard(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Sóng biển đang chập chờn",
                style = MaterialTheme.typography.headlineSmall,
                color = OceanicInk,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text =
                    "Oysteic tạm thời chưa bắt được tín hiệu mạng. " +
                        "Bạn kiểm tra lại Wi-Fi hoặc 4G rồi thử lại nhé!",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(20.dp))

            PrimaryButton(
                text = "Thử kết nối lại",
                onClick = onRetry,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = Color.White,
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// ==========================================
// PREVIEWS
// ==========================================

@Preview(name = "Splash - Loading State", showBackground = true)
@Composable
private fun SplashScreenLoadingPreview() {
    ToeicSpaceTheme {
        SplashScreen(
            uiState = SplashUiState.Loading,
            onRetry = {},
        )
    }
}

@Preview(name = "Splash - Network Error State", showBackground = true)
@Composable
private fun SplashScreenNetworkErrorPreview() {
    ToeicSpaceTheme {
        SplashScreen(
            uiState = SplashUiState.NetworkError(),
            onRetry = {},
        )
    }
}
