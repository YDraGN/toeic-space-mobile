package com.toeicspace.android.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.toeicspace.android.R
import com.toeicspace.android.core.ui.theme.BarSurface
import com.toeicspace.android.core.ui.theme.BrandGradientBrush
import com.toeicspace.android.core.ui.theme.CardBorder
import com.toeicspace.android.core.ui.theme.InkSoft
import com.toeicspace.android.core.ui.theme.OceanicInk
import com.toeicspace.android.core.ui.theme.PrimaryGradientStart
import com.toeicspace.android.core.ui.theme.ShellGradientBrush
import com.toeicspace.android.core.ui.theme.Spacing
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme
import com.toeicspace.android.core.ui.theme.softShadow

private val CollapsedWidth = 96.dp
private val ExpandedWidth = 264.dp

/** Collapsed items: a roomy rounded square, bigger than the phone's bottom bar droplet. */
private val IndicatorWidth = 64.dp
private val IndicatorHeight = 44.dp
private val IndicatorShape = RoundedCornerShape(16.dp)
private val ItemIconSize = 24.dp

private val ExpandedItemHeight = 52.dp
private val ExpandedItemShape = RoundedCornerShape(16.dp)
private val OpenRailLabel = R.string.common_cd_open_navigation
private val CloseRailLabel = R.string.common_cd_close_navigation

/** Profile sits at the foot of the rail, so the rail lists the other tabs. */
private val RailTabs = BottomNavTab.entries - BottomNavTab.PROFILE

/** Fill of the selected navigation item: the app's primary gradient, as on primary buttons. */
internal val SelectedItemBrush = BrandGradientBrush

/**
 * The selected look shared by every navigation surface (bottom bar, rail, its account row): the
 * primary gradient, with white content on it.
 */
internal fun Modifier.selectedDroplet(
    isSelected: Boolean,
    shape: Shape,
): Modifier =
    if (isSelected) {
        background(SelectedItemBrush, shape)
    } else {
        this
    }

/** Label of a selected item where it sits off the gradient (under the icon). */
internal val SelectedNavContent = PrimaryGradientStart

/**
 * Side navigation for windows at least 600dp wide.
 *
 * Collapsed, it has no panel of its own: big icon items sit straight on the page background, the
 * selected one on the same gradient as the phone's bottom bar. Expanded (from [NavigationMenuButton]), it becomes a
 * full-height side panel docked to the left edge, with full-width rows. [account] is drawn at the
 * foot and takes the place of the Profile tab, so it should open Profile when tapped. A rail
 * floating over the content should pass an opaque [containerColor].
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
    val panelColor by animateColorAsState(
        targetValue = if (expanded) containerColor else Color.Transparent,
        animationSpec = tween(durationMillis = 300),
        label = "RailPanel",
    )

    Column(
        modifier =
            modifier
                .fillMaxHeight()
                .width(width)
                .then(if (expanded) Modifier.softShadow(RectangleShape) else Modifier)
                .background(panelColor)
                .then(
                    if (expanded) {
                        // A hairline on the content side; the other edges are the screen's.
                        Modifier.drawBehind {
                            drawLine(
                                color = CardBorder,
                                start = Offset(size.width, 0f),
                                end = Offset(size.width, size.height),
                                strokeWidth = 1.dp.toPx(),
                            )
                        }
                    } else {
                        Modifier
                    },
                ).navigationBarsPadding()
                .padding(
                    // Collapsed labels get almost the whole width, so "Trang chủ" in bold still fits.
                    horizontal = if (expanded) Spacing.smd else Spacing.xs,
                    vertical = Spacing.md,
                ),
        horizontalAlignment = if (expanded) Alignment.Start else Alignment.CenterHorizontally,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(if (expanded) Spacing.xs else Spacing.md),
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
                    CollapsedRailItem(
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

@Composable
private fun CollapsedRailItem(
    tab: BottomNavTab,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier.selectable(
                selected = isSelected,
                onClick = onClick,
                role = Role.Tab,
                // The square default ripple fights the rounded indicator; the fill is feedback enough.
                interactionSource = null,
                indication = null,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        Box(
            modifier =
                Modifier
                    .size(width = IndicatorWidth, height = IndicatorHeight)
                    .selectedDroplet(isSelected, IndicatorShape),
            contentAlignment = Alignment.Center,
        ) {
            // The label below already names the tab, so the icon is decorative.
            Icon(
                painter = painterResource(id = tab.iconRes),
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else OceanicInk,
                modifier = Modifier.size(ItemIconSize),
            )
        }
        Text(
            text = stringResource(id = tab.labelRes),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) SelectedNavContent else OceanicInk,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ExpandedRailItem(
    tab: BottomNavTab,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else InkSoft
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(ExpandedItemHeight)
                .selectedDroplet(
                    isSelected,
                    ExpandedItemShape,
                ).selectable(
                    selected = isSelected,
                    onClick = onClick,
                    role = Role.Tab,
                    // The square default ripple fights the rounded indicator; the fill is feedback enough.
                    interactionSource = null,
                    indication = null,
                ).padding(horizontal = Spacing.md),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
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
        Box(modifier = Modifier.background(ShellGradientBrush)) {
            ToeicNavigationRail(
                selectedTab = BottomNavTab.HOME,
                onTabSelected = {},
                expanded = false,
            )
        }
    }
}

@Preview(heightDp = 640, widthDp = 360)
@Composable
private fun ToeicNavigationRailExpandedPreview() {
    ToeicSpaceTheme {
        Box(modifier = Modifier.background(ShellGradientBrush)) {
            ToeicNavigationRail(
                selectedTab = BottomNavTab.EXPLORE,
                onTabSelected = {},
                expanded = true,
                containerColor = MaterialTheme.colorScheme.surface,
            )
        }
    }
}

/** Collapsed rail center (half of 96dp) minus top bar padding (16dp) and half the button (24dp). */
private val MenuButtonStartInset = 8.dp

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
