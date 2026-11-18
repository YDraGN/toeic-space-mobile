package com.toeicspace.android.feature.course.presentation.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.toeicspace.android.R
import com.toeicspace.android.core.ui.TestTags
import com.toeicspace.android.core.ui.components.MascotView
import com.toeicspace.android.core.ui.components.PrimaryButton
import com.toeicspace.android.core.ui.components.SecondaryButton
import com.toeicspace.android.core.ui.components.ToeicCard
import com.toeicspace.android.core.ui.theme.AppTextStyles
import com.toeicspace.android.core.ui.theme.HintText
import com.toeicspace.android.core.ui.theme.InkSoft
import com.toeicspace.android.core.ui.theme.PillShape
import com.toeicspace.android.core.ui.theme.PriceGreen
import com.toeicspace.android.core.ui.theme.Spacing
import com.toeicspace.android.core.ui.theme.SubtleBorder
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme
import com.toeicspace.android.feature.course.domain.CourseDetail
import com.toeicspace.android.feature.course.domain.CourseLesson
import com.toeicspace.android.feature.course.domain.CourseModule
import com.toeicspace.android.feature.course.presentation.components.CourseCover
import com.toeicspace.android.feature.course.presentation.components.CourseLevelBadge
import com.toeicspace.android.feature.course.presentation.components.ExamTypeBadge
import com.toeicspace.android.feature.course.presentation.components.PreviewCourses
import com.toeicspace.android.feature.course.presentation.components.amountText

private val DialogMaxWidth = 440.dp
private val BillCoverSize = 64.dp
private val SoldOutMascotSize = 112.dp

/**
 * The summary a learner checks before enrolling, laid out like a receipt: the course, what it
 * includes and what it costs. The dialog stays open, with its button loading, until the server
 * answers; the view model then closes it or switches it to [SoldOutDialog].
 */
@Composable
internal fun EnrollConfirmDialog(
    course: CourseDetail,
    isEnrolling: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties =
            DialogProperties(
                dismissOnBackPress = !isEnrolling,
                dismissOnClickOutside = !isEnrolling,
                usePlatformDefaultWidth = false,
            ),
    ) {
        DialogCard(modifier = Modifier.testTag(TestTags.COURSE_ENROLL_CONFIRM)) {
            Text(
                text = stringResource(R.string.course_dialog_enroll_title),
                style = AppTextStyles.SectionTitle,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                text = stringResource(R.string.course_dialog_enroll_body),
                style = AppTextStyles.Caption,
                color = InkSoft,
            )
            BillCourseHeader(course = course)
            HorizontalDivider(color = SubtleBorder)
            BillRows(course = course)
            HorizontalDivider(color = SubtleBorder)
            BillTotal(course = course)
            Text(
                text = stringResource(R.string.course_dialog_enroll_note),
                style = AppTextStyles.HelperText,
                color = HintText,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.smd)) {
                SecondaryButton(
                    text = stringResource(R.string.course_action_later),
                    onClick = onDismiss,
                    enabled = !isEnrolling,
                    shape = PillShape,
                    modifier = Modifier.weight(1f),
                )
                PrimaryButton(
                    text = stringResource(R.string.course_action_confirm_enroll),
                    onClick = onConfirm,
                    isLoading = isEnrolling,
                    loadingText = stringResource(R.string.course_label_enrolling),
                    shape = PillShape,
                    modifier = Modifier.weight(1f).testTag(TestTags.COURSE_ENROLL_CONFIRM_BUTTON),
                )
            }
        }
    }
}

/** Shown when a sold-out course's button is tapped, or when the last seat went while enrolling. */
@Composable
internal fun SoldOutDialog(
    course: CourseDetail,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        DialogCard(modifier = Modifier.testTag(TestTags.COURSE_SOLD_OUT_DIALOG)) {
            MascotView(
                mascotResId = R.drawable.oy2_tired,
                size = SoldOutMascotSize,
                isFloating = false,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            Text(
                text = stringResource(R.string.course_dialog_sold_out_title),
                style = AppTextStyles.SectionTitle,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().semantics { heading() },
            )
            val capacity = course.summary.capacity
            Text(
                text =
                    if (capacity != null) {
                        stringResource(
                            R.string.course_dialog_sold_out_body,
                            course.summary.title,
                            capacity,
                        )
                    } else {
                        stringResource(
                            R.string.course_dialog_sold_out_body_no_capacity,
                            course.summary.title,
                        )
                    },
                style = AppTextStyles.Caption,
                color = InkSoft,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            PrimaryButton(
                text = stringResource(R.string.course_action_got_it),
                onClick = onDismiss,
                shape = PillShape,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** The card both dialogs sit on: the app's card, capped in width and scrollable on short screens. */
@Composable
private fun DialogCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    ToeicCard(
        modifier =
            modifier
                .widthIn(max = DialogMaxWidth)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
        itemSpacing = Spacing.md,
        contentPadding = PaddingValues(Spacing.lg),
        content = content,
    )
}

@Composable
private fun BillCourseHeader(course: CourseDetail) {
    val summary = course.summary
    Row(
        horizontalArrangement = Arrangement.spacedBy(Spacing.smd),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CourseCover(
            imageUrl = summary.thumbnailUrl,
            courseTitle = summary.title,
            seed = summary.id,
            modifier = Modifier.size(BillCoverSize),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                summary.examType?.let { ExamTypeBadge(examType = it) }
                summary.level?.let { CourseLevelBadge(level = it) }
            }
            Text(
                text = summary.title,
                style = AppTextStyles.ListItemTitle,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun BillRows(course: CourseDetail) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        BillRow(
            label = stringResource(R.string.course_bill_content),
            value =
                stringResource(
                    R.string.course_include_lessons,
                    course.lessonCount,
                    course.modules.size,
                ),
        )
        if (course.totalDurationSeconds > 0) {
            BillRow(
                label = stringResource(R.string.course_bill_duration),
                value = totalDurationText(course.totalDurationSeconds),
            )
        }
        val capacity = course.summary.capacity
        val remaining = course.summary.remainingSeats
        if (capacity != null && remaining != null) {
            BillRow(
                label = stringResource(R.string.course_bill_seats),
                value = stringResource(R.string.course_bill_seats_value, remaining, capacity),
            )
        }
        val price = course.summary.price
        val promotion = price.promotion
        if (promotion != null && promotion.originalPrice > price.payable) {
            BillRow(
                label = stringResource(R.string.course_bill_original_price),
                value = amountText(promotion.originalPrice, price.currency),
                strikeThrough = true,
            )
        }
    }
}

@Composable
private fun BillRow(
    label: String,
    value: String,
    strikeThrough: Boolean = false,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = label,
            style = AppTextStyles.Caption,
            color = HintText,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = AppTextStyles.Caption,
            color = InkSoft,
            textAlign = TextAlign.End,
            textDecoration = if (strikeThrough) TextDecoration.LineThrough else null,
        )
    }
}

/** The total, in the same large green as the price on the page. */
@Composable
private fun BillTotal(course: CourseDetail) {
    val price = course.summary.price
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(R.string.course_bill_total),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Text(
            text =
                if (price.isFree) {
                    stringResource(R.string.course_price_free)
                } else {
                    amountText(price.payable, price.currency)
                },
            style = AppTextStyles.PriceLarge,
            color = PriceGreen,
            maxLines = 1,
        )
    }
}

private fun previewDetail(remainingSeats: Int? = 12): CourseDetail {
    val summary = PreviewCourses.first()
    return CourseDetail(
        summary =
            summary.copy(
                price = summary.price.copy(amount = 0, promotion = null),
                capacity = if (remainingSeats == null) null else 40,
                remainingSeats = remainingSeats,
            ),
        description = null,
        skills = emptyList(),
        objectives = emptyList(),
        prerequisites = emptyList(),
        modules =
            listOf(
                CourseModule(
                    id = "m1",
                    title = "Part 3",
                    description = null,
                    lessons =
                        listOf(
                            CourseLesson("l1", "Hội thoại công sở", null, 1_200, true),
                            CourseLesson("l2", "Bẫy paraphrase", null, 1_500, false),
                        ),
                ),
            ),
    )
}

@Preview(widthDp = 390, heightDp = 760)
@Composable
private fun EnrollConfirmDialogPreview() {
    ToeicSpaceTheme {
        EnrollConfirmDialog(previewDetail(), isEnrolling = false, onConfirm = {}, onDismiss = {})
    }
}

@Preview(widthDp = 390, heightDp = 760)
@Composable
private fun EnrollConfirmDialogEnrollingPreview() {
    ToeicSpaceTheme {
        EnrollConfirmDialog(previewDetail(), isEnrolling = true, onConfirm = {}, onDismiss = {})
    }
}

@Preview(widthDp = 390, heightDp = 640)
@Composable
private fun SoldOutDialogPreview() {
    ToeicSpaceTheme { SoldOutDialog(previewDetail(remainingSeats = 0), onDismiss = {}) }
}
