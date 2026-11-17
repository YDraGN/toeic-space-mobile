package com.toeicspace.android.feature.course.presentation.detail

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.toeicspace.android.R
import java.util.Locale

private const val SECONDS_PER_MINUTE = 60
private const val SECONDS_PER_HOUR = 3600

/** "12:30" for a lesson of 12 min 30 s, "1:05:09" once it passes an hour. */
internal fun formatLessonDuration(seconds: Int): String {
    val safe = seconds.coerceAtLeast(0)
    val hours = safe / SECONDS_PER_HOUR
    val minutes = safe % SECONDS_PER_HOUR / SECONDS_PER_MINUTE
    val rest = safe % SECONDS_PER_MINUTE
    return if (hours > 0) {
        String.format(Locale.ROOT, "%d:%02d:%02d", hours, minutes, rest)
    } else {
        String.format(Locale.ROOT, "%d:%02d", minutes, rest)
    }
}

/** Whole hours and minutes of a total, rounding the seconds to the nearest minute. */
internal fun splitHoursMinutes(seconds: Int): Pair<Int, Int> {
    val totalMinutes = (seconds.coerceAtLeast(0) + SECONDS_PER_MINUTE / 2) / SECONDS_PER_MINUTE
    return totalMinutes / SECONDS_PER_MINUTE to totalMinutes % SECONDS_PER_MINUTE
}

/** "8 giờ 30 phút", or "8g 30p" when [short], for a chapter or a whole course. */
@Composable
internal fun totalDurationText(
    seconds: Int,
    short: Boolean = false,
): String {
    val (hours, minutes) = splitHoursMinutes(seconds)
    return when {
        hours > 0 && minutes > 0 -> {
            val res =
                if (short) {
                    R.string.course_duration_short_hours_minutes
                } else {
                    R.string.course_duration_hours_minutes
                }
            stringResource(res, hours, minutes)
        }
        hours > 0 -> {
            val res =
                if (short) R.string.course_duration_short_hours else R.string.course_duration_hours
            stringResource(res, hours)
        }
        else -> {
            val res =
                if (short) {
                    R.string.course_duration_short_minutes
                } else {
                    R.string.course_duration_minutes
                }
            stringResource(res, minutes)
        }
    }
}
