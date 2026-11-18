package com.toeicspace.android.feature.course.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.toeicspace.android.R
import com.toeicspace.android.core.ui.components.ToeicCard
import com.toeicspace.android.core.ui.theme.AppTextStyles
import com.toeicspace.android.core.ui.theme.BlushTone
import com.toeicspace.android.core.ui.theme.LilacTone
import com.toeicspace.android.core.ui.theme.PeachTone
import com.toeicspace.android.core.ui.theme.QuietFill
import com.toeicspace.android.core.ui.theme.Spacing
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme
import com.toeicspace.android.feature.course.domain.CourseExamType
import com.toeicspace.android.feature.course.domain.CourseLevel
import com.toeicspace.android.feature.course.domain.CoursePrice
import com.toeicspace.android.feature.course.domain.CourseSummary

private val CoverSize = 88.dp
private val MetaRowHeight = 24.dp

/**
 * A course in the catalog: square cover on the left, then exam, title, a two-line summary, level
 * and target score, and the price on a quiet strip. Fixed line counts keep the cards in a grid row
 * the same height.
 */
@Composable
fun CourseCard(
    course: CourseSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ToeicCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(Spacing.md),
        itemSpacing = Spacing.md,
        onClick = onClick,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
            CourseCover(
                imageUrl = course.thumbnailUrl,
                courseTitle = course.title,
                seed = course.id,
                modifier = Modifier.width(CoverSize),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                course.examType?.let { ExamTypeBadge(examType = it) }
                // Every line keeps its height even when shorter or missing, so cards in a grid row
                // line up instead of each ending at a different place.
                Text(
                    text = course.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    minLines = 2,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = course.shortDescription.orEmpty(),
                    style = AppTextStyles.Caption,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    minLines = 2,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                CourseMetaRow(
                    level = course.level,
                    targetScore = course.targetScore,
                    modifier = Modifier.padding(top = Spacing.xs),
                )
            }
        }
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.small)
                    .background(QuietFill)
                    .padding(horizontal = Spacing.smd, vertical = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CoursePriceText(price = course.price)
            Spacer(modifier = Modifier.weight(1f))
            SeatsLabel(remainingSeats = course.remainingSeats)
        }
    }
}

/**
 * A course as a narrow, tall tile: wide cover on top, then exam, title, a three-line summary, level
 * and target score, and the price strip. For rows of cards too narrow for [CourseCard]'s side cover,
 * such as the suggestions under a course. Fixed line counts keep tiles in a row the same height.
 */
@Composable
fun CourseTileCard(
    course: CourseSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ToeicCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(Spacing.md),
        itemSpacing = Spacing.sm,
        onClick = onClick,
    ) {
        CourseCover(
            imageUrl = course.thumbnailUrl,
            courseTitle = course.title,
            seed = course.id,
            aspectRatio = TILE_COVER_RATIO,
            modifier = Modifier.fillMaxWidth(),
        )
        course.examType?.let { ExamTypeBadge(examType = it) }
        Text(
            text = course.title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            minLines = 2,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = course.shortDescription.orEmpty(),
            style = AppTextStyles.Caption,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            minLines = TILE_SUMMARY_LINES,
            maxLines = TILE_SUMMARY_LINES,
            overflow = TextOverflow.Ellipsis,
        )
        CourseMetaRow(level = course.level, targetScore = course.targetScore)
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.small)
                    .background(QuietFill)
                    .padding(horizontal = Spacing.smd, vertical = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CoursePriceText(price = course.price)
            Spacer(modifier = Modifier.weight(1f))
            SeatsLabel(remainingSeats = course.remainingSeats)
        }
    }
}

private const val TILE_COVER_RATIO = 2.4f
private const val TILE_SUMMARY_LINES = 3

/**
 * Level meter and target score on one line of fixed height: a course missing one or both still takes
 * the same room, and a narrow card shortens the score instead of wrapping to a second line.
 */
@Composable
fun CourseMetaRow(
    level: CourseLevel?,
    targetScore: Int?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.height(MetaRowHeight),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        level?.let { CourseLevelBadge(level = it) }
        targetScore?.let {
            ToneBadge(
                text = stringResource(R.string.course_label_target, it),
                tone = PeachTone,
                iconRes = R.drawable.ic_target,
                modifier = Modifier.weight(1f, fill = false),
            )
        }
    }
}

/** Seats left for a course with a seat limit; nothing for one without. */
@Composable
fun SeatsLabel(
    remainingSeats: Int?,
    modifier: Modifier = Modifier,
) {
    if (remainingSeats == null) return
    val soldOut = remainingSeats <= 0
    ToneBadge(
        text =
            if (soldOut) {
                stringResource(R.string.course_label_sold_out)
            } else {
                stringResource(R.string.course_label_seats_left, remainingSeats)
            },
        tone = if (soldOut) BlushTone else LilacTone,
        iconRes = R.drawable.ic_users,
        modifier = modifier,
    )
}

internal val PreviewCourses =
    listOf(
        CourseSummary(
            id = "1",
            title = "TOEIC 450+ Nền tảng",
            shortDescription = "Ôn ngữ pháp gốc và từ vựng cốt lõi cho người mới bắt đầu.",
            thumbnailUrl = null,
            examType = CourseExamType.LISTENING_READING,
            level = CourseLevel.BEGINNER,
            targetScore = 450,
            price = CoursePrice(amount = 0, currency = "VND"),
            capacity = null,
            remainingSeats = null,
        ),
        CourseSummary(
            id = "2",
            title = "Bứt phá Listening Part 3–4",
            shortDescription = "Bắt bẫy hội thoại dài, 12 đề rút gọn kèm giải thích.",
            thumbnailUrl = null,
            examType = CourseExamType.LISTENING_READING,
            level = CourseLevel.INTERMEDIATE,
            targetScore = 650,
            price = CoursePrice(amount = 499_000, currency = "VND"),
            capacity = 40,
            remainingSeats = 40,
        ),
        CourseSummary(
            id = "3",
            title = "Speaking & Writing khởi động",
            shortDescription = "Làm quen 11 câu Speaking và 8 câu Writing.",
            thumbnailUrl = null,
            examType = CourseExamType.SPEAKING_WRITING,
            level = CourseLevel.BEGINNER,
            targetScore = null,
            price = CoursePrice(amount = 899_000, currency = "VND"),
            capacity = 30,
            remainingSeats = 0,
        ),
    )

@Preview(showBackground = true, backgroundColor = 0xFFE7FAFF, widthDp = 360)
@Composable
private fun CourseCardPreview() {
    ToeicSpaceTheme {
        Column(
            modifier = Modifier.padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.smd),
        ) {
            PreviewCourses.forEach { CourseCard(course = it, onClick = {}) }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFE7FAFF, widthDp = 820)
@Composable
private fun CourseTileCardPreview() {
    ToeicSpaceTheme {
        Row(
            modifier = Modifier.padding(Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.smd),
        ) {
            PreviewCourses.forEach {
                CourseTileCard(course = it, onClick = {}, modifier = Modifier.weight(1f))
            }
        }
    }
}
