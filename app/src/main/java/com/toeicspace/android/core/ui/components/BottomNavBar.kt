package com.toeicspace.android.core.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.toeicspace.android.R
import com.toeicspace.android.core.ui.theme.OceanicInk
import com.toeicspace.android.core.ui.theme.PrimaryGradientCenter
import com.toeicspace.android.core.ui.theme.Spacing
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme

enum class BottomNavTab(
    val route: String,
    val label: String,
    @get:DrawableRes val iconRes: Int,
) {
    HOME("home", "Trang chủ", R.drawable.ic_home),
    EXPLORE("explore", "Khám phá", R.drawable.ic_explore),
    PRACTICE("practice", "Luyện tập", R.drawable.ic_practice),
    VOCABULARY("vocabulary", "Từ vựng", R.drawable.ic_vocabulary),
    ACCOUNT("account", "Tài khoản", R.drawable.ic_account),
}

@Composable
fun BottomNavBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.smd, vertical = Spacing.sm)
                .height(64.dp),
        shape = RoundedCornerShape(32.dp),
        color = Color.White.copy(alpha = 0.95f),
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.8f)),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = Spacing.smd),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BottomNavTab.entries.forEach { tab ->
                TabItem(
                    tab = tab,
                    isSelected = currentRoute == tab.route,
                    onClick = { onNavigate(tab.route) },
                )
            }
        }
    }
}

@Composable
private fun TabItem(
    tab: BottomNavTab,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val iconScale by animateFloatAsState(
        targetValue = if (isSelected) 1.12f else 1f,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow,
            ),
        label = "TabIconScale",
    )
    val dropletScale by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.5f,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium,
            ),
        label = "DropletScale",
    )
    val activeColor = PrimaryGradientCenter
    val inactiveColor = OceanicInk.copy(alpha = 0.5f)
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) activeColor else inactiveColor,
        animationSpec = tween(durationMillis = 200),
        label = "TabContentColor",
    )

    Column(
        modifier =
            modifier
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick,
                ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier.size(width = 52.dp, height = 30.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (isSelected) {
                Box(
                    modifier =
                        Modifier
                            .size(width = 48.dp, height = 32.dp)
                            .graphicsLayer {
                                scaleX = dropletScale
                                scaleY = dropletScale
                            }.background(
                                brush =
                                    Brush.verticalGradient(
                                        colors =
                                            listOf(
                                                Color(0xFFEBF3FF),
                                                Color(0xFFDCEBFF),
                                            ),
                                    ),
                                shape = RoundedCornerShape(16.dp),
                            ).border(
                                width = 1.dp,
                                color = Color(0xFFC7DEFF),
                                shape = RoundedCornerShape(16.dp),
                            ),
                )
            }

            Icon(
                painter = painterResource(id = tab.iconRes),
                contentDescription = tab.label,
                tint = contentColor,
                modifier =
                    Modifier
                        .size(20.dp)
                        .graphicsLayer {
                            scaleX = iconScale
                            scaleY = iconScale
                        },
            )
        }
        Text(
            text = tab.label,
            color = contentColor,
            fontSize = 9.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview(name = "Bottom Nav Bar", showBackground = true, backgroundColor = 0xFFFAF9FF)
@Composable
private fun ToeicBottomNavBarPreview() {
    var selectedRoute by remember { mutableStateOf(BottomNavTab.HOME.route) }
    ToeicSpaceTheme {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            BottomNavBar(
                currentRoute = selectedRoute,
                onNavigate = { newRoute ->
                    selectedRoute = newRoute
                },
            )
        }
    }
}
