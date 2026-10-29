package com.toeicspace.android.core.ui.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.toeicspace.android.R
import com.toeicspace.android.core.ui.theme.NavDropletBorder
import com.toeicspace.android.core.ui.theme.NavDropletGradientEnd
import com.toeicspace.android.core.ui.theme.NavDropletGradientStart
import com.toeicspace.android.core.ui.theme.OceanicInk
import com.toeicspace.android.core.ui.theme.PrimaryGradientCenter
import com.toeicspace.android.core.ui.theme.Spacing
import com.toeicspace.android.core.ui.theme.ToeicSpaceShapes
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme

enum class BottomNavTab(
    @get:StringRes val labelRes: Int,
    @get:DrawableRes val iconRes: Int,
) {
    HOME(R.string.common_label_tab_home, R.drawable.ic_home),
    EXPLORE(R.string.common_label_tab_explore, R.drawable.ic_explore),
    PRACTICE(R.string.common_label_tab_practice, R.drawable.ic_practice),
    VOCABULARY(R.string.common_label_tab_vocabulary, R.drawable.ic_vocabulary),
    ACCOUNT(R.string.common_label_tab_account, R.drawable.ic_account),
}

@Composable
fun BottomNavBar(
    selectedTab: BottomNavTab,
    onTabSelected: (BottomNavTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.smd, vertical = Spacing.sm)
                .height(64.dp),
        shape = ToeicSpaceShapes.large,
        color = Color.White.copy(alpha = 0.95f),
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.8f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.smd),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BottomNavTab.entries.forEach { tab ->
                TabItem(
                    tab = tab,
                    isSelected = tab == selectedTab,
                    onClick = { onTabSelected(tab) },
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
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) PrimaryGradientCenter else OceanicInk.copy(alpha = 0.5f),
        animationSpec = tween(durationMillis = 200),
        label = "TabContentColor",
    )

    Column(
        modifier =
            modifier.selectable(
                selected = isSelected,
                onClick = onClick,
                role = Role.Tab,
                interactionSource = null,
                indication = null,
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
                                                NavDropletGradientStart,
                                                NavDropletGradientEnd,
                                            ),
                                    ),
                                shape = ToeicSpaceShapes.small,
                            ).border(
                                width = 1.dp,
                                color = NavDropletBorder,
                                shape = ToeicSpaceShapes.small,
                            ),
                )
            }

            // The label below already describes the tab, so the icon is decorative.
            Icon(
                painter = painterResource(id = tab.iconRes),
                contentDescription = null,
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
            text = stringResource(id = tab.labelRes),
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
private fun BottomNavBarPreview() {
    var selectedTab by remember { mutableStateOf(BottomNavTab.HOME) }
    ToeicSpaceTheme {
        BottomNavBar(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
            modifier = Modifier.padding(Spacing.md),
        )
    }
}
