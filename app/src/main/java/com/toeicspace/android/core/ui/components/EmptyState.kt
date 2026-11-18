package com.toeicspace.android.core.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.toeicspace.android.R
import com.toeicspace.android.core.ui.TestTags
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme

/**
 * Nothing to show yet. Pass [actionText] and [onAction] for a way out (clear filters, explore
 * courses); pass another [mascotResId] when the context calls for it, e.g. `oy2_search` for an
 * empty search result.
 */
@Composable
fun EmptyState(
    modifier: Modifier = Modifier,
    title: String = stringResource(id = R.string.state_empty_title),
    body: String? = stringResource(id = R.string.state_empty_body),
    fullScreen: Boolean = true,
    @DrawableRes mascotResId: Int = R.drawable.oy2_empty,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
) {
    MascotMessage(
        mascotResId = mascotResId,
        title = title,
        body = body,
        fullScreen = fullScreen,
        modifier = modifier.testTag(TestTags.STATE_EMPTY),
    ) {
        if (actionText != null && onAction != null) {
            SecondaryButton(
                text = actionText,
                onClick = onAction,
                modifier = Modifier.fillMaxWidth().testTag(TestTags.STATE_ACTION),
            )
        }
    }
}

@StatePreviews
@Composable
private fun EmptyStatePreview() {
    ToeicSpaceTheme { EmptyState() }
}

@InlineStatePreviews
@Composable
private fun EmptyStateInlineWithActionPreview() {
    ToeicSpaceTheme {
        EmptyState(
            title = "Không có khoá học phù hợp",
            body = "Bạn thử bỏ bớt bộ lọc để xem thêm khoá học nhé.",
            fullScreen = false,
            mascotResId = R.drawable.oy2_search,
            actionText = "Xoá bộ lọc",
            onAction = {},
        )
    }
}
