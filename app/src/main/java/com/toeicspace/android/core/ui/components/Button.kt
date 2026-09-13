package com.toeicspace.android.core.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.toeicspace.android.R
import com.toeicspace.android.core.ui.theme.CoralBadge
import com.toeicspace.android.core.ui.theme.OceanicInk
import com.toeicspace.android.core.ui.theme.PillShape
import com.toeicspace.android.core.ui.theme.PrimaryGradientCenter
import com.toeicspace.android.core.ui.theme.PrimaryGradientEnd
import com.toeicspace.android.core.ui.theme.PrimaryGradientStart
import com.toeicspace.android.core.ui.theme.SecondaryButtonBackground
import com.toeicspace.android.core.ui.theme.SecondaryButtonBorder
import com.toeicspace.android.core.ui.theme.SecondaryButtonContent
import com.toeicspace.android.core.ui.theme.Spacing
import com.toeicspace.android.core.ui.theme.ToeicSpaceShapes
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme

private val DefaultPrimaryBrush =
    Brush.horizontalGradient(
        colors = listOf(PrimaryGradientStart, PrimaryGradientCenter, PrimaryGradientEnd),
    )

private val DisabledPrimaryBrush =
    Brush.horizontalGradient(
        colors =
            listOf(
                PrimaryGradientStart.copy(alpha = 0.5f),
                PrimaryGradientCenter.copy(alpha = 0.5f),
                PrimaryGradientEnd.copy(alpha = 0.5f),
            ),
    )

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = ToeicSpaceShapes.small,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    loadingText: String? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    val isInteractive = enabled && !isLoading
    val currentBrush =
        if (isInteractive) {
            DefaultPrimaryBrush
        } else {
            DisabledPrimaryBrush
        }

    Button(
        onClick = onClick,
        modifier =
            modifier
                .background(currentBrush, shape)
                .heightIn(min = Spacing.xxl),
        enabled = isInteractive,
        shape = shape,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                disabledContentColor = Color.Transparent,
            ),
        contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.sm),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(Spacing.space18),
                strokeWidth = Spacing.space2,
                color = Color.White,
            )
            Spacer(modifier = Modifier.width(Spacing.sm))
        } else {
            leadingIcon?.let {
                it()
                Spacer(modifier = Modifier.width(Spacing.sm))
            }
        }

        Text(
            text = if (isLoading && loadingText != null) loadingText else text,
            style = MaterialTheme.typography.labelLarge,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        if (!isLoading) {
            trailingIcon?.let {
                Spacer(modifier = Modifier.width(Spacing.sm))
                it()
            }
        }
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = ToeicSpaceShapes.small,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    val isInteractive = enabled && !isLoading
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = Spacing.xxl),
        enabled = isInteractive,
        shape = shape,
        border = BorderStroke(1.dp, SecondaryButtonBorder),
        colors =
            ButtonDefaults.outlinedButtonColors(
                containerColor = SecondaryButtonBackground,
                contentColor = SecondaryButtonContent,
            ),
        contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.space12),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = SecondaryButtonContent,
            )
            Spacer(modifier = Modifier.width(Spacing.sm))
        } else {
            leadingIcon?.let {
                it()
                Spacer(modifier = Modifier.width(Spacing.sm))
            }
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = SecondaryButtonContent,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (!isLoading) {
            trailingIcon?.let {
                Spacer(modifier = Modifier.width(Spacing.sm))
                it()
            }
        }
    }
}

@Composable
fun IconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    hasBadge: Boolean = false,
    badgeCount: Int? = null,
    enabled: Boolean = true,
    isSelected: Boolean = false,
    contentDescription: String? = null,
    icon: @Composable () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow,
            ),
        label = "IconButtonScale",
    )

    val containerColor =
        when {
            !enabled -> Color.White.copy(alpha = 0.5f)
            isSelected -> Color(0xFFE9EDFF)
            else -> Color.White
        }

    val contentColor =
        when {
            !enabled -> OceanicInk.copy(alpha = 0.38f)
            isSelected -> Color(0xFF1467C2)
            else -> OceanicInk
        }

    Box(
        modifier = modifier.size(Spacing.xxl),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .size(Spacing.space40)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    },
        ) {
            Surface(
                onClick = onClick,
                modifier = Modifier.size(Spacing.space40),
                shape = CircleShape,
                color = containerColor,
                border =
                    BorderStroke(
                        width = 1.dp,
                        color =
                            if (isSelected) {
                                Color(0xFF1467C2).copy(alpha = 0.3f)
                            } else {
                                Color(0xFFE9EDFF)
                            },
                    ),
                shadowElevation = if (enabled) 2.dp else 0.dp,
                interactionSource = interactionSource,
                enabled = enabled,
            ) {
                CompositionLocalProvider(LocalContentColor provides contentColor) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(Spacing.space40),
                    ) {
                        Box(
                            modifier = Modifier.size(20.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            icon()
                        }
                    }
                }
            }

            if (badgeCount != null && badgeCount > 0) {
                val displayCount = if (badgeCount > 99) "99+" else badgeCount.toString()
                Box(
                    modifier =
                        Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 4.dp, y = (-2).dp)
                            .defaultMinSize(minWidth = 16.dp, minHeight = 16.dp)
                            .background(CoralBadge, CircleShape)
                            .border(1.5.dp, Color.White, CircleShape)
                            .padding(horizontal = 4.dp, vertical = 1.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = displayCount,
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        lineHeight = 10.sp,
                    )
                }
            } else if (hasBadge) {
                Box(
                    modifier =
                        Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 4.dp, y = (-2).dp)
                            .size(14.dp)
                            .background(CoralBadge, CircleShape)
                            .border(1.5.dp, Color.White, CircleShape),
                )
            }
        }
    }
}

// --- COMPOSE PREVIEW ---
@Preview(name = "Primary - Full Width", showBackground = true)
@Composable
private fun PrimaryButtonFullWidthPreview() {
    ToeicSpaceTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            PrimaryButton(
                text = "LÀM BÀI KIỂM TRA ĐẦU VÀO NGAY",
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_move_right),
                        contentDescription = null,
                    )
                },
            )
        }
    }
}

@Preview(name = "Primary - Loading State", showBackground = true)
@Composable
private fun PrimaryButtonLoadingPreview() {
    ToeicSpaceTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            PrimaryButton(
                text = "TIẾP THEO",
                onClick = {},
                isLoading = true,
                modifier = Modifier.fillMaxWidth(),
                loadingText = "Đang xử lý...",
            )
        }
    }
}

@Preview(name = "Secondary - Login Action", showBackground = true)
@Composable
private fun SecondaryButtonPreview() {
    ToeicSpaceTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            SecondaryButton(
                text = "Đã có tài khoản? Đăng nhập",
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_log_in),
                        contentDescription = null,
                    )
                },
            )
        }
    }
}

@Preview(name = "Secondary - Pill Quick Action", showBackground = true)
@Composable
private fun SecondaryButtonPillPreview() {
    ToeicSpaceTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            SecondaryButton(
                text = "Chuỗi Streak",
                shape = PillShape,
                onClick = {},
                leadingIcon = {
                    Text(text = "🔥")
                },
            )
        }
    }
}

@Preview(name = "Icon Buttons Set", showBackground = true, backgroundColor = 0xFFD8E2FF)
@Composable
private fun ToeicIconButtonsPreview() {
    ToeicSpaceTheme {
        Row(
            horizontalArrangement =
                androidx.compose.foundation.layout.Arrangement
                    .spacedBy(8.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            // 1. Chuông có chấm hồng san hô
            IconButton(
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_bell),
                        contentDescription = null,
                    )
                },
                onClick = {},
                hasBadge = true,
                contentDescription = "Notification Dot",
            )

            // 2. Chuông có số lượng thông báo (ví dụ 5)
            IconButton(
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_bell),
                        contentDescription = null,
                    )
                },
                onClick = {},
                badgeCount = 5,
                contentDescription = "Notification Count",
            )

            // 3. Kính lúp tìm kiếm
            IconButton(
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_search),
                        contentDescription = null,
                    )
                },
                onClick = {},
                contentDescription = "Search",
            )

            // 4. Mũi tên quay lại
            IconButton(
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_chevron_right),
                        contentDescription = null,
                    )
                },
                onClick = {},
                contentDescription = "Back",
            )

            // 5. Làm mới
            IconButton(
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_refresh),
                        contentDescription = null,
                    )
                },
                onClick = {},
                contentDescription = "Refresh",
            )
        }
    }
}
