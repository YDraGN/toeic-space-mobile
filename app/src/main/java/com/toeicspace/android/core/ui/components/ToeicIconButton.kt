package com.toeicspace.android.core.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.toeicspace.android.R
import com.toeicspace.android.core.ui.theme.CoralBadge
import com.toeicspace.android.core.ui.theme.IconButtonBackground
import com.toeicspace.android.core.ui.theme.IconButtonBorder
import com.toeicspace.android.core.ui.theme.IconButtonSelectedBackground
import com.toeicspace.android.core.ui.theme.IconButtonSelectedBorder
import com.toeicspace.android.core.ui.theme.IconButtonSelectedContent
import com.toeicspace.android.core.ui.theme.OceanicInk
import com.toeicspace.android.core.ui.theme.Spacing
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme

private const val MAX_BADGE_COUNT = 99

/** Circular icon button with an optional dot or count badge. */
@Composable
fun ToeicIconButton(
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
            isSelected -> IconButtonSelectedBackground
            else -> IconButtonBackground
        }
    val contentColor =
        when {
            !enabled -> OceanicInk.copy(alpha = 0.38f)
            isSelected -> IconButtonSelectedContent
            else -> OceanicInk
        }

    // Outer box keeps the 48dp touch target, the visible button is 36dp.
    Box(
        modifier = modifier.size(Spacing.xxl),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .size(Spacing.space36)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    },
        ) {
            Surface(
                onClick = onClick,
                modifier =
                    Modifier
                        .matchParentSize()
                        .semantics {
                            if (contentDescription != null) {
                                this.contentDescription = contentDescription
                            }
                        },
                enabled = enabled,
                shape = CircleShape,
                color = containerColor,
                contentColor = contentColor,
                shadowElevation = if (enabled) 1.dp else 0.dp,
                border =
                    BorderStroke(
                        width = 1.dp,
                        color = if (isSelected) IconButtonSelectedBorder else IconButtonBorder,
                    ),
                interactionSource = interactionSource,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Box(modifier = Modifier.size(20.dp), contentAlignment = Alignment.Center) {
                        CompositionLocalProvider(LocalContentColor provides contentColor) {
                            icon()
                        }
                    }
                }
            }

            IconBadge(hasBadge = hasBadge, badgeCount = badgeCount)
        }
    }
}

@Composable
private fun BoxScope.IconBadge(
    hasBadge: Boolean,
    badgeCount: Int?,
) {
    val badgeModifier =
        Modifier
            .align(Alignment.TopEnd)
            .offset(x = 4.dp, y = (-2).dp)
    if (badgeCount != null && badgeCount > 0) {
        Box(
            modifier =
                badgeModifier
                    .defaultMinSize(minWidth = 16.dp, minHeight = 16.dp)
                    .background(CoralBadge, CircleShape)
                    .border(1.5.dp, Color.White, CircleShape)
                    .padding(horizontal = 4.dp, vertical = 1.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = if (badgeCount > MAX_BADGE_COUNT) "$MAX_BADGE_COUNT+" else "$badgeCount",
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
                badgeModifier
                    .size(14.dp)
                    .background(CoralBadge, CircleShape)
                    .border(1.5.dp, Color.White, CircleShape),
        )
    }
}

@Preview(name = "Icon Buttons Set", showBackground = true, backgroundColor = 0xFFD8E2FF)
@Composable
private fun ToeicIconButtonPreview() {
    ToeicSpaceTheme {
        Row(
            modifier = Modifier.padding(Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            ToeicIconButton(
                onClick = {},
                hasBadge = true,
                contentDescription = stringResource(id = R.string.common_cd_notifications),
            ) {
                Icon(painter = painterResource(id = R.drawable.ic_bell), contentDescription = null)
            }
            ToeicIconButton(onClick = {}, badgeCount = 5, isSelected = true) {
                Icon(painter = painterResource(id = R.drawable.ic_bell), contentDescription = null)
            }
            ToeicIconButton(onClick = {}) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_search),
                    contentDescription = null,
                )
            }
        }
    }
}
