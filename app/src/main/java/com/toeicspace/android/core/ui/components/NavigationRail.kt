package com.toeicspace.android.core.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.toeicspace.android.R
import com.toeicspace.android.core.ui.theme.BarSurface
import com.toeicspace.android.core.ui.theme.CardBorder
import com.toeicspace.android.core.ui.theme.InkSoft
import com.toeicspace.android.core.ui.theme.NavDropletGradientEnd
import com.toeicspace.android.core.ui.theme.NavDropletGradientStart
import com.toeicspace.android.core.ui.theme.PillShape
import com.toeicspace.android.core.ui.theme.PrimaryGradientStart
import com.toeicspace.android.core.ui.theme.Spacing
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme
import com.toeicspace.android.core.ui.theme.softShadow

private val CollapsedWidth = 88.dp
private val ExpandedWidth = 248.dp
private val ExpandedItemHeight = 48.dp
private val ItemIconSize = 22.dp
private val OpenRailLabel = R.string.common_cd_open_navigation
private val CloseRailLabel = R.string.common_cd_close_navigation

/** Profile sits at the foot of the rail, so the rail lists the other tabs. */
private val RailTabs = BottomNavTab.entries - BottomNavTab.PROFILE

/** The pearl-to-sky pill behind the selected item, shared by both rail states. */
internal val SelectedItemBrush =
    Brush.horizontalGradient(
        listOf(NavDropletGradientStart, NavDropletGradientEnd),
    )

/**
 * Side navigation for windows at least 600dp wide. Collapsed it shows each tab as icon over label;
 * expanded (from [NavigationMenuButton] in the top bar) it shows icon beside label. [account] is drawn at the foot of the rail and
 * takes the place of the Profile tab, so it should open Profile when tapped. A rail floating over
 * the content should pass an opaque [containerColor], or the rail beneath shows through.
 */
@Composable
fun ToeicNavigationRail(
    selectedTab: BottomNavTab,
    onTabSelected: (BottomNavTab) -> Unit,
    expanded: Boolean,
    modifier: Modifier = Modifier,
    containerColor: Color = BarSurface,
    account: @Composable (expanded: Boolean) -> Unit = {},
) {
    val width by animateDpAsState(
        targetValue = if (expanded) ExpandedWidth else CollapsedWidth,
        animationSpec = tween(durationMillis = 300),
        label = "RailWidth",
    )

    Surface(
        modifier =
            modifier
                .fillMaxHeight()
                .padding(horizontal = Spacing.sm, vertical = Spacing.smd)
                .softShadow(BarShape)
                .width(width),
        shape = BarShape,
        color = containerColor,
        border = BorderStroke(1.dp, CardBorder),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.smd),
            horizontalAlignment = if (expanded) Alignment.Start else Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(Spacing.sm))
            Column(
                verticalArrangement =
                    Arrangement.spacedBy(
                        if (expanded) Spacing.xs else Spacing.lg,
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                RailTabs.forEach { tab ->
                    if (expanded) {
                        ExpandedRailItem(
                            tab = tab,
                            isSelected = tab == selectedTab,
                            onClick = { onTabSelected(tab) },
                        )
                    } else {
                        TabItem(
                            tab = tab,
                            isSelected = tab == selectedTab,
                            onClick = { onTabSelected(tab) },
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            account(expanded)
        }
    }
}

@Composable
private fun ExpandedRailItem(
    tab: BottomNavTab,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentColor = if (isSelected) PrimaryGradientStart else InkSoft
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(ExpandedItemHeight)
                .then(
                    if (isSelected) Modifier.background(SelectedItemBrush, PillShape) else Modifier,
                ).selectable(selected = isSelected, onClick = onClick, role = Role.Tab)
                .padding(horizontal = Spacing.md),
        horizontalArrangement = Arrangement.spacedBy(Spacing.smd),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(id = tab.iconRes),
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(ItemIconSize),
        )
        Text(
            text = stringResource(id = tab.labelRes),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview(heightDp = 640)
@Composable
private fun ToeicNavigationRailCollapsedPreview() {
    ToeicSpaceTheme {
        ToeicNavigationRail(
            selectedTab = BottomNavTab.HOME,
            onTabSelected = {},
            expanded = false,
        )
    }
}

@Preview(heightDp = 640)
@Composable
private fun ToeicNavigationRailExpandedPreview() {
    ToeicSpaceTheme {
        Box {
            ToeicNavigationRail(
                selectedTab = BottomNavTab.EXPLORE,
                onTabSelected = {},
                expanded = true,
            )
        }
    }
}

/** Rail column center (8dp margin + half of 88dp) minus top bar padding (16dp) and half the button (24dp). */
private val MenuButtonStartInset = 12.dp

/**
 * The ☰ button at the start of the top bar on wide windows. A plain icon, not a bubble like the
 * page actions, nudged so it sits right above the rail icons.
 */
@Composable
fun NavigationMenuButton(
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(onClick = onClick, modifier = modifier.padding(start = MenuButtonStartInset)) {
        Icon(
            painter = painterResource(id = R.drawable.ic_menu),
            contentDescription =
                stringResource(
                    id = if (expanded) CloseRailLabel else OpenRailLabel,
                ),
            tint = MaterialTheme.colorScheme.onSurface,
        )
    }
}
