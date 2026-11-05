package com.toeicspace.android.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.toeicspace.android.core.ui.theme.CardBorder
import com.toeicspace.android.core.ui.theme.Spacing
import com.toeicspace.android.core.ui.theme.ToeicSpaceShapes
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme
import com.toeicspace.android.core.ui.theme.softShadow

/**
 * Content card used for sections inside tabs. Clickable when [onClick] is set. [itemSpacing] is
 * the gap between children; lists that draw their own dividers pass 0.
 *
 * The container is opaque: a translucent one lets the shadow show through as a grey film, and the
 * mockup's frosted glass needs blur, which Android only has from API 31 (design-system §6).
 */
@Composable
fun ToeicCard(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentPadding: PaddingValues = PaddingValues(Spacing.md),
    itemSpacing: Dp = Spacing.smd,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = ToeicSpaceShapes.medium
    val body: @Composable () -> Unit = {
        Column(
            modifier = Modifier.padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(itemSpacing),
            content = content,
        )
    }
    val cardModifier = modifier.softShadow(shape)
    val border = BorderStroke(1.dp, CardBorder)
    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = cardModifier,
            shape = shape,
            color = containerColor,
            border = border,
            content = body,
        )
    } else {
        Surface(
            modifier = cardModifier,
            shape = shape,
            color = containerColor,
            border = border,
            content = body,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFE7FAFF)
@Composable
private fun ToeicCardPreview() {
    ToeicSpaceTheme {
        ToeicCard(modifier = Modifier.fillMaxWidth().padding(Spacing.md)) {
            Text(text = "Lớp học chính thức")
            Text(text = "Bạn chưa tham gia lớp học nào.")
        }
    }
}
