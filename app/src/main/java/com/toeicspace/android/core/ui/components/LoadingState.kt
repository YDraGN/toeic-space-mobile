package com.toeicspace.android.core.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import com.toeicspace.android.R
import com.toeicspace.android.core.ui.TestTags
import com.toeicspace.android.core.ui.theme.Dimens
import com.toeicspace.android.core.ui.theme.Spacing
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme

/** First load of a screen or section: the waiting Oysteic, a short message and a spinner. */
@Composable
fun LoadingState(
    modifier: Modifier = Modifier,
    message: String = stringResource(id = R.string.state_loading_title),
    fullScreen: Boolean = true,
) {
    MascotMessage(
        mascotResId = R.drawable.oy2_waiting,
        title = message,
        body = null,
        fullScreen = fullScreen,
        modifier =
            modifier
                .testTag(TestTags.STATE_LOADING)
                .semantics { liveRegion = LiveRegionMode.Polite },
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(if (fullScreen) Dimens.progressMd else Dimens.progressSm),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = if (fullScreen) Spacing.xs else Dimens.strokeThin,
        )
    }
}

@StatePreviews
@Composable
private fun LoadingStatePreview() {
    ToeicSpaceTheme { LoadingState() }
}

@InlineStatePreviews
@Composable
private fun LoadingStateInlinePreview() {
    ToeicSpaceTheme { LoadingState(fullScreen = false) }
}
