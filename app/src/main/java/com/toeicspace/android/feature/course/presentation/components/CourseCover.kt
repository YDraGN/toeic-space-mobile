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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.toeicspace.android.R
import com.toeicspace.android.core.ui.theme.CourseCoverBrushes
import com.toeicspace.android.core.ui.theme.Spacing
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme

/** The fallback cover of a course: always the same one for the same [seed] (the course id). */
internal fun courseCoverBrush(seed: String): Brush =
    CourseCoverBrushes[Math.floorMod(seed.hashCode(), CourseCoverBrushes.size)]

/**
 * Rounded course cover, square unless [aspectRatio] says otherwise (width / height). A gradient with a pearl sits underneath, so a course without a
 * thumbnail (or one that fails to load) still has a cover, and nothing jumps while the image loads.
 * The gradient is chosen from [seed], so neighbouring courses get different colors. [onImageError]
 * lets a screen refetch an expired signed URL.
 */
@Composable
fun CourseCover(
    imageUrl: String?,
    courseTitle: String,
    seed: String,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.small,
    onImageError: (() -> Unit)? = null,
    aspectRatio: Float = 1f,
) {
    Box(
        modifier =
            modifier
                .aspectRatio(aspectRatio)
                .clip(shape)
                .background(courseCoverBrush(seed)),
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
                onError = onImageError?.let { callback -> { callback() } },
            )
        }
    }
}

private const val PEARL_FRACTION = 0.42f

/** Seeds "0" to "5" land on the six cover gradients in order. */
@Preview(showBackground = true, widthDp = 420)
@Composable
private fun CourseCoverPalettePreview() {
    ToeicSpaceTheme {
        Row(
            modifier = Modifier.padding(Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            CourseCoverBrushes.indices.forEach { index ->
                CourseCover(
                    imageUrl = null,
                    courseTitle = "TOEIC 450+",
                    seed = "$index",
                    modifier = Modifier.size(56.dp),
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 320)
@Composable
private fun CourseCoverLargePreview() {
    ToeicSpaceTheme {
        CourseCover(
            imageUrl = null,
            courseTitle = "TOEIC 450+",
            seed = "3",
            modifier = Modifier.fillMaxWidth().padding(Spacing.md),
            shape = MaterialTheme.shapes.medium,
        )
    }
}
