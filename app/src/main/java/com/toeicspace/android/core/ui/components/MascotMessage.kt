package com.toeicspace.android.core.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.toeicspace.android.R
import com.toeicspace.android.core.ui.theme.Dimens
import com.toeicspace.android.core.ui.theme.Spacing
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme

/**
 * Mascot, title, optional body and actions: the layout behind every shared state
 * (`LoadingState`, `EmptyState`, `ErrorState`, `ComingSoonState`, `SignInRequiredState`).
 *
 * [fullScreen] fills the available area, centres the message and scrolls on short screens or large
 * fonts. Inline (`fullScreen = false`) wraps its content with a smaller mascot, so it fits inside a
 * list section, a card or a `LazyColumn` item.
 */
@Composable
fun MascotMessage(
    @DrawableRes mascotResId: Int,
    title: String,
    body: String?,
    modifier: Modifier = Modifier,
    fullScreen: Boolean = true,
    actions: @Composable ColumnScope.() -> Unit = {},
) {
    if (fullScreen) {
        // Centred when it fits, scrollable when the screen is short or the font is large.
        BoxWithConstraints(modifier = modifier.fillMaxSize()) {
            MessageColumn(
                mascotResId = mascotResId,
                title = title,
                body = body,
                fullScreen = true,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .heightIn(min = maxHeight)
                        .padding(horizontal = Spacing.lg, vertical = Spacing.xl),
                actions = actions,
            )
        }
    } else {
        MessageColumn(
            mascotResId = mascotResId,
            title = title,
            body = body,
            fullScreen = false,
            // No side padding: the list section or card around it already has its gutter.
            modifier =
                modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacing.md),
            actions = actions,
        )
    }
}

@Composable
private fun MessageColumn(
    @DrawableRes mascotResId: Int,
    title: String,
    body: String?,
    fullScreen: Boolean,
    modifier: Modifier,
    actions: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement =
            Arrangement.spacedBy(
                if (fullScreen) Spacing.md else Spacing.sm,
                Alignment.CenterVertically,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        MascotView(
            mascotResId = mascotResId,
            size = if (fullScreen) Dimens.mascotLg else Dimens.mascotSm,
            isFloating = fullScreen,
        )
        Text(
            text = title,
            style =
                if (fullScreen) {
                    MaterialTheme.typography.headlineSmall
                } else {
                    MaterialTheme.typography.titleMedium
                },
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = Dimens.messageMaxWidth),
        )
        if (body != null) {
            Text(
                text = body,
                style =
                    if (fullScreen) {
                        MaterialTheme.typography.bodySmall
                    } else {
                        MaterialTheme.typography.bodyMedium
                    },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = Dimens.messageMaxWidth),
            )
        }
        Column(
            modifier =
                Modifier
                    .widthIn(max = Dimens.messageMaxWidth)
                    .fillMaxWidth()
                    .padding(top = Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(Spacing.smd),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = actions,
        )
    }
}

@StatePreviews
@Composable
private fun MascotMessagePreview() {
    ToeicSpaceTheme {
        MascotMessage(
            mascotResId = R.drawable.oy2_base,
            title = "Oysteic chào bạn",
            body = "Tiêu đề, nội dung và nút hành động dùng chung cho mọi trạng thái.",
        )
    }
}
