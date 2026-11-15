package com.toeicspace.android.feature.course.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.toeicspace.android.R
import com.toeicspace.android.core.ui.theme.HeroGradientBrush
import com.toeicspace.android.core.ui.theme.Spacing
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme

/**
 * Square, rounded course cover. The brand gradient with a pearl sits underneath, so a course
 * without a thumbnail (or one that fails to load) still has a cover, and nothing jumps while the
 * image loads.
 */
@Composable
fun CourseCover(
    imageUrl: String?,
    courseTitle: String,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.small,
) {
    Box(
        modifier =
            modifier
                .aspectRatio(1f)
                .clip(shape)
                .background(HeroGradientBrush),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_glowing_pearl),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(PEARL_FRACTION),
        )
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = stringResource(R.string.course_cd_cover, courseTitle),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

private const val PEARL_FRACTION = 0.42f

@Preview(showBackground = true)
@Composable
private fun CourseCoverPreview() {
    ToeicSpaceTheme {
        Row(
            modifier = Modifier.padding(Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            CourseCover(
                imageUrl = null,
                courseTitle = "TOEIC 450+",
                modifier = Modifier.size(88.dp),
            )
            CourseCover(
                imageUrl = null,
                courseTitle = "TOEIC 450+",
                modifier = Modifier.width(160.dp),
                shape = MaterialTheme.shapes.medium,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 320)
@Composable
private fun CourseCoverWidePreview() {
    ToeicSpaceTheme {
        CourseCover(
            imageUrl = null,
            courseTitle = "TOEIC 450+",
            modifier = Modifier.fillMaxWidth().padding(Spacing.md),
        )
    }
}
