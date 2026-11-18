package com.toeicspace.android.feature.checkout.presentation.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.toeicspace.android.R
import com.toeicspace.android.core.ui.components.ToeicIconButton
import com.toeicspace.android.core.ui.theme.AppTextStyles
import com.toeicspace.android.core.ui.theme.HintText
import com.toeicspace.android.core.ui.theme.QuietFill
import com.toeicspace.android.core.ui.theme.SeaDeep
import com.toeicspace.android.core.ui.theme.SkyTone
import com.toeicspace.android.core.ui.theme.Spacing
import com.toeicspace.android.core.ui.theme.TagTone
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme

/*
 * Small building blocks of the checkout screen. They are internal for now; [InfoNote] and
 * [StatusPill] are candidates for core/ui/components once another feature needs them.
 */

private val SmallIconSize = 18.dp

/** A label over a read-only value, with an optional copy button. */
@Composable
internal fun CopyableField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueStyle: TextStyle = AppTextStyles.ListItemTitle,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    copyDescription: String? = null,
    onCopy: (() -> Unit)? = null,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(QuietFill, MaterialTheme.shapes.extraSmall)
                .padding(
                    start = Spacing.smd,
                    end = Spacing.xs,
                    top = Spacing.sm,
                    bottom = Spacing.sm,
                ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Spacing.space2),
        ) {
            Text(text = label, style = AppTextStyles.HelperText, color = HintText)
            Text(text = value, style = valueStyle, color = valueColor)
        }
        if (onCopy != null) {
            ToeicIconButton(onClick = onCopy, contentDescription = copyDescription) {
                Icon(
                    painter = painterResource(R.drawable.ic_copy),
                    contentDescription = null,
                    tint = SeaDeep,
                    modifier = Modifier.size(SmallIconSize),
                )
            }
        }
    }
}

/** An explanatory line on a quiet fill: never yellow, so it does not read as a warning. */
@Composable
internal fun InfoNote(
    text: String,
    modifier: Modifier = Modifier,
    @DrawableRes iconRes: Int = R.drawable.ic_info,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(QuietFill, MaterialTheme.shapes.extraSmall)
                .padding(Spacing.smd),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = SeaDeep,
            modifier = Modifier.size(SmallIconSize),
        )
        Text(
            text = text,
            style = AppTextStyles.Caption,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** A tinted pill with an icon, e.g. the countdown. */
@Composable
internal fun StatusPill(
    text: String,
    @DrawableRes iconRes: Int,
    modifier: Modifier = Modifier,
    tone: TagTone = SkyTone,
) {
    Row(
        modifier =
            modifier
                .background(tone.container, MaterialTheme.shapes.small)
                .padding(horizontal = Spacing.smd, vertical = Spacing.xs + Spacing.space2),
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs + Spacing.space2),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = tone.content,
            modifier = Modifier.size(SmallIconSize),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = tone.content,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** One line of the order summary: a label on the left, an amount on the right. */
@Composable
internal fun SummaryRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = AppTextStyles.ReadingBody,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(text = value, style = AppTextStyles.ListItemTitle, color = valueColor, maxLines = 1)
    }
}

@Preview(showBackground = true)
@Composable
private fun CheckoutComponentsPreview() {
    ToeicSpaceTheme {
        Box(modifier = Modifier.padding(Spacing.md)) {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                StatusPill(text = "Giữ chỗ còn 14:32", iconRes = R.drawable.ic_clock)
                CopyableField(label = "Số tài khoản", value = "0123456789", onCopy = {})
                SummaryRow(label = "Giá gốc", value = "699.000đ")
                InfoNote(text = "Giữ nguyên số tiền và nội dung chuyển khoản.")
            }
        }
    }
}
