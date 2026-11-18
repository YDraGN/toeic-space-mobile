package com.toeicspace.android.core.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.toeicspace.android.R
import com.toeicspace.android.core.ui.TestTags
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme

/** A feature that has no backend yet: Oysteic in a hard hat and a friendly "coming soon". */
@Composable
fun ComingSoonState(
    modifier: Modifier = Modifier,
    title: String = stringResource(id = R.string.common_label_coming_soon),
    body: String = stringResource(id = R.string.common_msg_coming_soon),
    fullScreen: Boolean = true,
) {
    MascotMessage(
        mascotResId = R.drawable.oy2_maintenance,
        title = title,
        body = body,
        fullScreen = fullScreen,
        modifier = modifier.testTag(TestTags.STATE_COMING_SOON),
    )
}

@StatePreviews
@Composable
private fun ComingSoonStatePreview() {
    ToeicSpaceTheme { ComingSoonState(title = "Luyện tập") }
}

@InlineStatePreviews
@Composable
private fun ComingSoonStateInlinePreview() {
    ToeicSpaceTheme { ComingSoonState(fullScreen = false) }
}
