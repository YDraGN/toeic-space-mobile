package com.toeicspace.android.feature.enrollment.presentation

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.toeicspace.android.R
import com.toeicspace.android.core.ui.components.AppTopBar
import com.toeicspace.android.core.ui.components.MascotMessage
import com.toeicspace.android.core.ui.components.SecondaryButton
import com.toeicspace.android.core.ui.components.ToeicIconButton
import com.toeicspace.android.core.ui.theme.Spacing
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme

/*
 * Small pieces shared by the learning pages (my courses, lesson). They follow the course detail
 * page and stay internal until the shared PBL-105 states replace the loading and retry ones.
 */

/** Top bar of a page above the shell: back, then the page title on one line. */
@Composable
internal fun LearningTopBar(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppTopBar(
        modifier = modifier,
        leadingContent = {
            ToeicIconButton(
                onClick = onBackClick,
                contentDescription = stringResource(id = R.string.common_action_back),
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_chevron_left),
                    contentDescription = null,
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier =
                    Modifier
                        .weight(1f)
                        .padding(start = Spacing.smd)
                        .semantics { heading() },
            )
        },
    )
}

@Composable
internal fun LearningLoading(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

/** Something failed to load: what happened, and "Thử lại". */
@Composable
internal fun LearningRetry(
    title: String,
    body: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes mascotResId: Int = R.drawable.oy2_error,
) {
    MascotMessage(mascotResId = mascotResId, title = title, body = body, modifier = modifier) {
        SecondaryButton(
            text = stringResource(id = R.string.state_action_retry),
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFE7FAFF, widthDp = 390, heightDp = 600)
@Composable
private fun LearningRetryPreview() {
    ToeicSpaceTheme {
        LearningTopBar(title = "Khoá học của tôi", onBackClick = {})
        LearningRetry(
            title = "Chưa tải được khoá học",
            body = "Không có kết nối mạng. Bạn kiểm tra lại nhé.",
            onRetry = {},
        )
    }
}
