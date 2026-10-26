package com.toeicspace.android.feature.onboarding.presentation.components

import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.toeicspace.android.R
import com.toeicspace.android.core.ui.components.BrandGradientBrush
import com.toeicspace.android.core.ui.components.PrimaryButton
import com.toeicspace.android.core.ui.components.SecondaryButton
import com.toeicspace.android.core.ui.theme.OceanicInk
import com.toeicspace.android.core.ui.theme.PillShape
import com.toeicspace.android.core.ui.theme.PrimaryBorder
import com.toeicspace.android.core.ui.theme.SeaTurquoise
import com.toeicspace.android.core.ui.theme.SecondaryButtonBackground
import com.toeicspace.android.core.ui.theme.SubtleBorder
import com.toeicspace.android.core.ui.theme.ToeicSpaceShapes
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme
import com.toeicspace.android.feature.onboarding.presentation.model.OnboardingSlide
import com.toeicspace.android.feature.onboarding.presentation.model.OnboardingTag

@Composable
fun OnboardingTagChip(
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
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = tag.icon,
            fontSize = 13.sp,
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = tag.label,
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

@Composable
fun OnboardingPagerIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        for (i in 0 until pageCount) {
            val isSelected = i == currentPage
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

@Composable
fun OnboardingActionButton(
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
                .clickable(onClick = onClick)
                .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
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

@Composable
fun OnboardingActionButton(
    @StringRes textRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OnboardingActionButton(
        text = stringResource(id = textRes),
        onClick = onClick,
        modifier = modifier,
    )
}

@Composable
fun OnboardingCard(
    slide: OnboardingSlide,
    pageCount: Int,
    currentPage: Int,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isLastPage = currentPage == pageCount - 1
    val actionTextRes = if (isLastPage) R.string.btn_start else R.string.btn_continue

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Surface(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
            shape = ToeicSpaceShapes.large,
            color = Color.White,
            shadowElevation = 8.dp,
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 24.dp),
            ) {
                Spacer(modifier = Modifier.height(6.dp))

                // TITLE
                Text(
                    text = slide.title,
                    style =
                        MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = OceanicInk,
                            lineHeight = 28.sp,
                        ),
                )

                Spacer(modifier = Modifier.height(10.dp))

                // DESCRIPTION
                Text(
                    text = slide.description,
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            color = OceanicInk.copy(alpha = 0.75f),
                            lineHeight = 22.sp,
                        ),
                )

                Spacer(modifier = Modifier.height(16.dp))

                // TAGS
                if (slide.tags.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        slide.tags.forEach { tag ->
                            OnboardingTagChip(
                                tag = tag,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // INDICATOR & BUTTON
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OnboardingPagerIndicator(
                        pageCount = pageCount,
                        currentPage = currentPage,
                    )
                    OnboardingActionButton(
                        textRes = actionTextRes,
                        onClick = onNextClick,
                    )
                }
            }
        }
    }
}

@Composable
fun OnboardingAuthOptCard(
    onSignUpClick: () -> Unit,
    onLogInClick: () -> Unit,
    onGuestClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Surface(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
            shape = ToeicSpaceShapes.large,
            color = Color.White,
            shadowElevation = 8.dp,
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(id = R.string.onboarding_auth_prompt),
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = SeaTurquoise,
                            lineHeight = 22.sp,
                        ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(20.dp))
                PrimaryButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(id = R.string.btn_register_new),
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_sign_up),
                            contentDescription = null,
                        )
                    },
                    onClick = onSignUpClick,
                )

                Spacer(modifier = Modifier.height(10.dp))
                SecondaryButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(id = R.string.btn_login_existing),
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_log_in),
                            contentDescription = null,
                        )
                    },
                    onClick = onLogInClick,
                )

                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier =
                        Modifier
                            .clip(PillShape)
                            .clickable(onClick = onGuestClick)
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = stringResource(id = R.string.btn_guest_explore),
                        style =
                            MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium,
                                color = OceanicInk.copy(alpha = 0.75f),
                            ),
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.ic_chevron_right),
                        contentDescription = null,
                        tint = OceanicInk.copy(alpha = 0.75f),
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

@Preview(
    name = "Onboarding Card - Slide 1 (2 tags)",
    showBackground = true,
    backgroundColor = 0xFFDCEBFF,
)
@Composable
private fun OnboardingCardSlide1Preview() {
    val slide1 =
        OnboardingSlide(
            mascotResId = R.drawable.oy2_hello,
            greeting = "Chào bạn, mình là Oysteic!",
            title = "Hành trình học của riêng bạn",
            description =
                buildAnnotatedString {
                    append("Không gian cá nhân hoá thông minh: theo dõi ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = PrimaryBorder)) {
                        append("chuỗi streak ngọc trai")
                    }
                    append(
                        ", tiếp tục đúng bài học dở dang và nhận gợi ý nhiệm vụ vừa vặn mỗi ngày.",
                    )
                },
            tags =
                listOf(
                    OnboardingTag("🔥", "Chuỗi Streak"),
                    OnboardingTag("📊", "Tiến trình học"),
                ),
        )
    ToeicSpaceTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            OnboardingCard(
                slide = slide1,
                pageCount = 3,
                currentPage = 0,
                onNextClick = {},
            )
        }
    }
}

@Preview(
    name = "Onboarding Card - Slide 2 (3 tags)",
    showBackground = true,
    backgroundColor = 0xFFDCEBFF,
)
@Composable
private fun OnboardingCardSlide2Preview() {
    val slide2 =
        OnboardingSlide(
            mascotResId = R.drawable.oy2_listening,
            greeting = "Luyện tập mọi lúc, mọi nơi!",
            title = "Luyện đúng phần, đúng lúc",
            description =
                buildAnnotatedString {
                    append("Tách biệt linh hoạt: Luyện sâu ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = PrimaryBorder)) {
                        append("Nghe (Part 1-4)")
                    }
                    append(", nhịp nhàng ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = PrimaryBorder)) {
                        append("Đọc (Part 5-7)")
                    }
                    append(" hoặc bấm giờ thi thử Full Test nghiêm ngặt.")
                },
            tags =
                listOf(
                    OnboardingTag("🎧", "Listening"),
                    OnboardingTag("📖", "Reading"),
                    OnboardingTag("⏱️", "Full Test"),
                ),
        )
    ToeicSpaceTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            OnboardingCard(
                slide = slide2,
                pageCount = 3,
                currentPage = 1,
                onNextClick = {},
            )
        }
    }
}

@Preview(name = "StartMethodCard", showBackground = true, backgroundColor = 0xFFDCEBFF)
@Composable
private fun StartMethodCardPreview() {
    ToeicSpaceTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            OnboardingAuthOptCard(
                onSignUpClick = {},
                onLogInClick = {},
                onGuestClick = {},
            )
        }
    }
}
