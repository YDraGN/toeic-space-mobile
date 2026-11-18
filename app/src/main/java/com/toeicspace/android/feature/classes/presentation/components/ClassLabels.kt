package com.toeicspace.android.feature.classes.presentation.components

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.toeicspace.android.R
import com.toeicspace.android.core.common.AppError
import com.toeicspace.android.core.ui.theme.LilacTone
import com.toeicspace.android.core.ui.theme.MintTone
import com.toeicspace.android.core.ui.theme.PeachTone
import com.toeicspace.android.core.ui.theme.SkyTone
import com.toeicspace.android.core.ui.theme.TagTone
import com.toeicspace.android.feature.classes.domain.StudentClass
import com.toeicspace.android.feature.classes.domain.StudentClassStatus
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private const val HTTP_FORBIDDEN = 403
private const val HTTP_NOT_FOUND = 404

/** Day/month/year, as the web app shows class dates (vi-VN). */
private val ClassDateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

@get:StringRes
internal val StudentClassStatus.labelRes: Int
    get() =
        when (this) {
            StudentClassStatus.DRAFT -> R.string.my_classes_status_draft
            StudentClassStatus.ACTIVE -> R.string.my_classes_status_active
            StudentClassStatus.COMPLETED -> R.string.my_classes_status_completed
            StudentClassStatus.ARCHIVED -> R.string.my_classes_status_archived
        }

/** Each status has its own tone: mint while running, peach before, sky when done, lilac archived. */
internal val StudentClassStatus.tone: TagTone
    get() =
        when (this) {
            StudentClassStatus.DRAFT -> PeachTone
            StudentClassStatus.ACTIVE -> MintTone
            StudentClassStatus.COMPLETED -> SkyTone
            StudentClassStatus.ARCHIVED -> LilacTone
        }

@Composable
internal fun LocalDate?.classDateText(): String =
    this?.format(ClassDateFormat) ?: stringResource(R.string.my_classes_value_date_unset)

@Composable
internal fun StudentClass.teacherText(): String =
    teacherName
        ?: stringResource(
            if (hasTeacher) {
                R.string.my_classes_value_teacher_unknown
            } else {
                R.string.my_classes_value_teacher_unassigned
            },
        )

/** "07/09/2026 – 20/12/2026"; a date not set yet reads "Chưa thiết lập". */
@Composable
internal fun StudentClass.dateRangeText(): String =
    stringResource(
        R.string.my_classes_value_date_range,
        startDate.classDateText(),
        endDate.classDateText(),
    )

/** Classroom answers 404 for a class the student is not (or no longer) enrolled in. */
internal val AppError.isNotFound: Boolean
    get() = (this as? AppError.Server)?.status == HTTP_NOT_FOUND

/** Wording follows the web "Lớp học của tôi" page. Backend titles and details are never shown. */
@StringRes
internal fun AppError.classMessageRes(): Int =
    when (this) {
        AppError.Network -> R.string.common_error_network
        AppError.ServerUnreachable -> R.string.common_error_server_unreachable
        AppError.Unauthorized -> R.string.my_classes_error_session
        is AppError.Server ->
            when (status) {
                HTTP_FORBIDDEN -> R.string.my_classes_error_forbidden
                HTTP_NOT_FOUND -> R.string.my_classes_body_not_found
                else -> R.string.my_classes_error_load
            }
        is AppError.Validation, AppError.Unknown -> R.string.my_classes_error_load
    }

/** The shared offline banner reads "offline" or "server unreachable"; any other failure is the server. */
internal val AppError.staleBannerError: AppError
    get() = if (this == AppError.Network) this else AppError.ServerUnreachable
