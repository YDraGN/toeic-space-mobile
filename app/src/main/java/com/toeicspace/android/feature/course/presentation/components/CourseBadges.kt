package com.toeicspace.android.feature.course.presentation.components

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.toeicspace.android.R
import com.toeicspace.android.core.ui.theme.CardBorder
import com.toeicspace.android.core.ui.theme.ExamFourSkillsContainer
import com.toeicspace.android.core.ui.theme.ExamFourSkillsContent
import com.toeicspace.android.core.ui.theme.ExamListeningReadingContainer
import com.toeicspace.android.core.ui.theme.ExamListeningReadingContent
import com.toeicspace.android.core.ui.theme.ExamSpeakingWritingContainer
import com.toeicspace.android.core.ui.theme.ExamSpeakingWritingContent
import com.toeicspace.android.core.ui.theme.LevelDotOff
import com.toeicspace.android.core.ui.theme.OceanicInk
import com.toeicspace.android.core.ui.theme.PillShape
import com.toeicspace.android.core.ui.theme.PrimaryGradientStart
import com.toeicspace.android.core.ui.theme.Spacing
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme
import com.toeicspace.android.feature.course.domain.CourseExamType
import com.toeicspace.android.feature.course.domain.CourseLevel

@get:StringRes
val CourseExamType.labelRes: Int
    get() =
        when (this) {
            CourseExamType.LISTENING_READING -> R.string.course_exam_listening_reading
            CourseExamType.SPEAKING_WRITING -> R.string.course_exam_speaking_writing
            CourseExamType.FOUR_SKILLS -> R.string.course_exam_four_skills
        }

@get:StringRes
val CourseLevel.labelRes: Int
    get() =
        when (this) {
            CourseLevel.BEGINNER -> R.string.course_level_beginner
            CourseLevel.INTERMEDIATE -> R.string.course_level_intermediate
            CourseLevel.ADVANCED -> R.string.course_level_advanced
        }

private val CourseExamType.container: Color
    get() =
        when (this) {
            CourseExamType.LISTENING_READING -> ExamListeningReadingContainer
            CourseExamType.SPEAKING_WRITING -> ExamSpeakingWritingContainer
            CourseExamType.FOUR_SKILLS -> ExamFourSkillsContainer
        }

private val CourseExamType.content: Color
    get() =
        when (this) {
            CourseExamType.LISTENING_READING -> ExamListeningReadingContent
            CourseExamType.SPEAKING_WRITING -> ExamSpeakingWritingContent
            CourseExamType.FOUR_SKILLS -> ExamFourSkillsContent
        }

/** The exam a course prepares for, in that exam's own color. */
@Composable
fun ExamTypeBadge(
    examType: CourseExamType,
    modifier: Modifier = Modifier,
) {
    Text(
        text = stringResource(examType.labelRes),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = examType.content,
        maxLines = 1,
        modifier =
            modifier
                .clip(PillShape)
                .background(examType.container)
                .padding(horizontal = Spacing.sm, vertical = Spacing.space2),
    )
}

private const val LEVEL_DOTS = 3
private val LevelDotSize = 6.dp

/**
 * Level as a label with a three-dot meter (one dot for Beginner, three for Advanced). It stays
 * neutral so it never competes with the exam color next to it.
 */
@Composable
fun CourseLevelBadge(
    level: CourseLevel,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .clip(PillShape)
                .border(1.dp, CardBorder, PillShape)
                .padding(horizontal = Spacing.sm, vertical = Spacing.space2),
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.space2)) {
            repeat(LEVEL_DOTS) { index ->
                Box(
                    modifier =
                        Modifier
                            .size(LevelDotSize)
                            .clip(CircleShape)
                            .background(
                                if (index <=
                                    level.ordinal
                                ) {
                                    PrimaryGradientStart
                                } else {
                                    LevelDotOff
                                },
                            ),
                )
            }
        }
        Text(
            text = stringResource(level.labelRes),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = OceanicInk,
            maxLines = 1,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CourseBadgesPreview() {
    ToeicSpaceTheme {
        Column(
            modifier = Modifier.padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                CourseExamType.entries.forEach { ExamTypeBadge(examType = it) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                CourseLevel.entries.forEach { CourseLevelBadge(level = it) }
            }
        }
    }
}
