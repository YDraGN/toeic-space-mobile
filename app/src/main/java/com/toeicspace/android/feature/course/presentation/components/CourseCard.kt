package com.toeicspace.android.feature.course.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.toeicspace.android.R
import com.toeicspace.android.core.ui.components.ToeicCard
import com.toeicspace.android.core.ui.theme.AppTextStyles
import com.toeicspace.android.core.ui.theme.BrandDestructive
import com.toeicspace.android.core.ui.theme.CardBorder
import com.toeicspace.android.core.ui.theme.CoralBadge
import com.toeicspace.android.core.ui.theme.InkSoft
import com.toeicspace.android.core.ui.theme.Spacing
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme
import com.toeicspace.android.feature.course.domain.CourseExamType
import com.toeicspace.android.feature.course.domain.CourseLevel
import com.toeicspace.android.feature.course.domain.CoursePrice
import com.toeicspace.android.feature.course.domain.CourseSummary

private val CoverSize = 88.dp
private val MetaIconSize = 14.dp

/**
 * A course in the catalog: square cover on the left, then exam, title, a two-line summary, level
 * and target score, and the price row. Fixed line counts keep the cards in a grid row the same height.
 */
@Composable
fun CourseCard(
    course: CourseSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ToeicCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(Spacing.smd),
        itemSpacing = Spacing.smd,
        onClick = onClick,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.smd)) {
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
                Text(
                    text = course.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                course.shortDescription?.let {
                    Text(
                        text = it,
                        style = AppTextStyles.Caption,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                CourseMetaRow(level = course.level, targetScore = course.targetScore)
            }
        }
        HorizontalDivider(color = CardBorder)
        Row(verticalAlignment = Alignment.CenterVertically) {
            CoursePriceText(price = course.price)
            Spacer(modifier = Modifier.weight(1f))
            SeatsLabel(remainingSeats = course.remainingSeats)
        }
    }
}

/** Level meter and target score; a course without either shows nothing. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CourseMetaRow(
    level: CourseLevel?,
    targetScore: Int?,
    modifier: Modifier = Modifier,
) {
    if (level == null && targetScore == null) return
    FlowRow(
        modifier = modifier.padding(top = Spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        level?.let { CourseLevelBadge(level = it) }
        targetScore?.let {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_target),
                    contentDescription = null,
                    tint = CoralBadge,
                    modifier = Modifier.size(MetaIconSize),
                )
                Text(
                    text = stringResource(R.string.course_label_target, it),
                    style = AppTextStyles.Caption,
                    color = InkSoft,
                    maxLines = 1,
                )
            }
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
    Text(
        text =
            if (soldOut) {
                stringResource(R.string.course_label_sold_out)
            } else {
                stringResource(R.string.course_label_seats_left, remainingSeats)
            },
        style = AppTextStyles.Caption,
        color = if (soldOut) BrandDestructive else InkSoft,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
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
