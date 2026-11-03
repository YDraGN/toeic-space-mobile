package com.toeicspace.android.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.toeicspace.android.core.ui.theme.OceanicInk
import com.toeicspace.android.core.ui.theme.Pearl
import com.toeicspace.android.core.ui.theme.PillShape
import com.toeicspace.android.core.ui.theme.SecondaryButtonBackground
import com.toeicspace.android.core.ui.theme.Spacing
import com.toeicspace.android.core.ui.theme.SubtleBorder
import com.toeicspace.android.core.ui.theme.SunlitGold
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme

enum class ToeicChipStyle(
    val container: Color,
    val border: Color,
) {
    /** Tags and benefits. */
    NEUTRAL(SecondaryButtonBackground, SubtleBorder),

    /** "Coming soon", "Sample data": something the user should not count on yet. */
    NOTICE(Pearl, SunlitGold.copy(alpha = 0.5f)),
}

/** Small non-interactive pill with an optional emoji. */
@Composable
fun ToeicChip(
    text: String,
    modifier: Modifier = Modifier,
    emoji: String? = null,
    style: ToeicChipStyle = ToeicChipStyle.NEUTRAL,
) {
    Row(
        modifier =
            modifier
                .clip(PillShape)
                .background(style.container)
                .border(1.dp, style.border, PillShape)
                .padding(horizontal = Spacing.sm, vertical = Spacing.space2),
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (emoji != null) {
            Text(text = emoji, style = MaterialTheme.typography.labelSmall)
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = OceanicInk,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFAF9FF)
@Composable
private fun ToeicChipPreview() {
    ToeicSpaceTheme {
        Row(
            modifier = Modifier.padding(Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            ToeicChip(text = "Chuỗi Streak", emoji = "🔥")
            ToeicChip(text = "Sắp ra mắt", style = ToeicChipStyle.NOTICE)
        }
    }
}
