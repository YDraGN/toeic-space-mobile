package com.toeicspace.android.feature.classes.presentation.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.toeicspace.android.R
import com.toeicspace.android.core.ui.components.AppTopBar
import com.toeicspace.android.core.ui.components.ToeicCard
import com.toeicspace.android.core.ui.components.ToeicIconButton
import com.toeicspace.android.core.ui.theme.AppTextStyles
import com.toeicspace.android.core.ui.theme.PillShape
import com.toeicspace.android.core.ui.theme.Spacing
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme
import com.toeicspace.android.feature.classes.domain.StudentClass
import com.toeicspace.android.feature.classes.domain.StudentClassStatus
import java.time.LocalDate

private val InfoIconSize = 16.dp

/** Back and the screen title, like the course page. */
@Composable
internal fun ClassesTopBar(
    title: String,
    onBackClick: () -> Unit,
) {
    AppTopBar(
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
                modifier = Modifier.weight(1f).padding(start = Spacing.smd),
            )
        },
    )
}

/** The status of a class as a pill in its own tone. */
@Composable
internal fun ClassStatusTag(
    status: StudentClassStatus,
    modifier: Modifier = Modifier,
) {
    val tone = status.tone
    Text(
        text = stringResource(status.labelRes),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = tone.content,
        maxLines = 1,
        modifier =
            modifier
                .clip(PillShape)
                .background(tone.container)
                .padding(horizontal = Spacing.sm, vertical = Spacing.space2),
    )
}

/** Class code, the small kicker above the class name. */
@Composable
internal fun ClassCodeText(
    code: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = code,
        style = AppTextStyles.Caption,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )
}

/** A small icon and one line of text: the teacher or the dates on a class card. */
@Composable
internal fun ClassInfoLine(
    @DrawableRes iconRes: Int,
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(InfoIconSize),
        )
        Text(
            text = text,
            style = AppTextStyles.Caption,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** One class in the list: code and status, name, teacher and dates. */
@Composable
internal fun ClassCard(
    studentClass: StudentClass,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ToeicCard(modifier = modifier.fillMaxWidth(), itemSpacing = Spacing.sm, onClick = onClick) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ClassCodeText(code = studentClass.code, modifier = Modifier.weight(1f))
            studentClass.status?.let { ClassStatusTag(status = it) }
        }
        Text(
            text = studentClass.name,
            style = AppTextStyles.CourseTitle,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        ClassInfoLine(iconRes = R.drawable.ic_account, text = studentClass.teacherText())
        ClassInfoLine(iconRes = R.drawable.ic_calendar, text = studentClass.dateRangeText())
    }
}

internal val PreviewClasses =
    listOf(
        StudentClass(
            id = "c1",
            code = "TOEIC-650-K12",
            name = "Luyện thi TOEIC 650+ buổi tối",
            description =
                "Lớp tập trung Part 3, 4 và Part 7, mỗi tuần một bài thi thử có chữa chi tiết. " +
                    "Giáo viên theo sát tiến độ từng bạn qua bài tập về nhà.",
            status = StudentClassStatus.ACTIVE,
            startDate = LocalDate.of(2026, 9, 7),
            endDate = LocalDate.of(2026, 12, 20),
            teacherName = "Trần Thu Hà",
            hasTeacher = true,
        ),
        StudentClass(
            id = "c2",
            code = "SW-K03",
            name = "TOEIC Speaking & Writing cơ bản",
            description = null,
            status = StudentClassStatus.DRAFT,
            startDate = LocalDate.of(2027, 1, 4),
            endDate = null,
            teacherName = null,
            hasTeacher = false,
        ),
        StudentClass(
            id = "c3",
            code = "LR-450-K08",
            name = "Nền tảng TOEIC 450",
            description = null,
            status = StudentClassStatus.COMPLETED,
            startDate = LocalDate.of(2026, 3, 2),
            endDate = LocalDate.of(2026, 6, 28),
            teacherName = "Lê Minh Quân",
            hasTeacher = true,
        ),
        StudentClass(
            id = "c4",
            code = "LR-300-K01",
            name = "Làm quen TOEIC",
            description = null,
            status = StudentClassStatus.ARCHIVED,
            startDate = LocalDate.of(2025, 10, 6),
            endDate = LocalDate.of(2026, 1, 18),
            teacherName = null,
            hasTeacher = true,
        ),
    )

@Preview(showBackground = true, backgroundColor = 0xFFE7FAFF, widthDp = 390)
@Composable
private fun ClassCardPreview() {
    ToeicSpaceTheme {
        Column(
            modifier = Modifier.padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.smd),
        ) {
            PreviewClasses.forEach { ClassCard(studentClass = it, onClick = {}) }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFE7FAFF, widthDp = 390)
@Composable
private fun ClassStatusTagPreview() {
    ToeicSpaceTheme {
        Row(
            modifier = Modifier.padding(Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            StudentClassStatus.entries.forEach { ClassStatusTag(status = it) }
        }
    }
}
