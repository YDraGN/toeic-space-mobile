package com.toeicspace.android.core.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.toeicspace.android.R
import com.toeicspace.android.core.ui.theme.AppTextStyles
import com.toeicspace.android.core.ui.theme.BlushTint
import com.toeicspace.android.core.ui.theme.CardBorder
import com.toeicspace.android.core.ui.theme.PastelSky
import com.toeicspace.android.core.ui.theme.Pearl
import com.toeicspace.android.core.ui.theme.Spacing
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme

private val RowMinHeight = 56.dp
private val IconBubbleSize = 36.dp
private val IconBubbleShape = RoundedCornerShape(12.dp)
private val RowIconSize = 20.dp

/** A titled group of [ToeicListRow]s inside one card, as in the settings and profile screens. */
@Composable
fun ToeicListSection(
    modifier: Modifier = Modifier,
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        if (title != null) {
            Text(
                text = title.uppercase(),
                style = AppTextStyles.KickerLabel,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = Spacing.xs),
            )
        }
        ToeicCard(
            modifier = Modifier.fillMaxWidth(),
            // Rows bring their own padding and the dividers sit flush between them, so the card adds
            // no gap of its own: every row is then the same distance from its neighbours and the edge.
            contentPadding = PaddingValues(vertical = Spacing.xs),
            itemSpacing = 0.dp,
            content = content,
        )
    }
}

/** Hairline between two rows of a [ToeicListSection]. */
@Composable
fun ToeicListDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(modifier = modifier.padding(horizontal = Spacing.md), color = CardBorder)
}

/**
 * One row: an icon in a tinted bubble, a label with an optional small [overline] above it (a field
 * name over its value) and [supportingText] below, then a value, a custom [trailing] slot or a
 * chevron when the row opens something.
 */
@Composable
fun ToeicListRow(
    @DrawableRes iconRes: Int,
    label: String,
    modifier: Modifier = Modifier,
    iconBackground: Color = PastelSky,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    overline: String? = null,
    supportingText: String? = null,
    value: String? = null,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                .heightIn(min = RowMinHeight)
                .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(Spacing.smd),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(IconBubbleSize).background(iconBackground, IconBubbleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(RowIconSize),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            if (overline != null) {
                Text(
                    text = overline,
                    style = AppTextStyles.Caption,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(text = label, style = MaterialTheme.typography.titleSmall, color = contentColor)
            if (supportingText != null) {
                Text(
                    text = supportingText,
                    style = AppTextStyles.Caption,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (value != null) {
            Text(
                text = value,
                style = AppTextStyles.Caption,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        when {
            trailing != null -> trailing()
            onClick != null ->
                Icon(
                    painter = painterResource(id = R.drawable.ic_chevron_right),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(RowIconSize),
                )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFE7FAFF)
@Composable
private fun ToeicListSectionPreview() {
    ToeicSpaceTheme {
        ToeicListSection(title = "Tài khoản", modifier = Modifier.padding(Spacing.md)) {
            ToeicListRow(
                iconRes = R.drawable.ic_account,
                label = "Thông tin tài khoản",
                value = "Minh Anh",
                onClick = {},
            )
            ToeicListDivider()
            ToeicListRow(
                iconRes = R.drawable.ic_key,
                label = "Đổi mật khẩu",
                iconBackground = Pearl,
                onClick = {},
            )
            ToeicListDivider()
            ToeicListRow(
                iconRes = R.drawable.ic_trash,
                label = "Xóa tài khoản",
                iconBackground = BlushTint,
                iconTint = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.error,
                onClick = {},
            )
        }
    }
}
