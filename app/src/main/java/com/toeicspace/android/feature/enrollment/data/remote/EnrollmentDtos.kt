package com.toeicspace.android.feature.enrollment.data.remote

import com.toeicspace.android.feature.enrollment.domain.Enrollment
import com.toeicspace.android.feature.enrollment.domain.MyCourse
import com.toeicspace.android.feature.enrollment.domain.MyCoursePage
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeParseException

/** Content's `CourseEnrollment` entity, returned as is. */
@Serializable
data class EnrollmentDto(
    val id: String? = null,
    val courseId: String,
    val status: String? = null,
    val enrolledAt: String? = null,
)

/** `MyCourseDto`. [thumbnailUrl] is signed for five minutes. */
@Serializable
data class MyCourseDto(
    val courseId: String,
    val title: String,
    val slug: String? = null,
    val shortDescription: String? = null,
    val enrollmentStatus: String? = null,
    val enrolledAt: String? = null,
    val thumbnailUrl: String? = null,
)

/** `PagedResult<MyCourseDto>`. */
@Serializable
data class MyCoursePageDto(
    val items: List<MyCourseDto> = emptyList(),
    val page: Int,
    val pageSize: Int,
    val totalCount: Int,
)

/**
 * EF reads `DateTime` columns back without a kind, so the same instant can come as
 * "2026-11-10T08:00:00Z" or "2026-11-10T08:00:00". Both are UTC.
 */
internal fun String?.toUtcInstantOrNull(): Instant? {
    if (this.isNullOrBlank()) return null
    return try {
        Instant.parse(this)
    } catch (_: DateTimeParseException) {
        try {
            LocalDateTime.parse(this).toInstant(ZoneOffset.UTC)
        } catch (_: DateTimeParseException) {
            null
        }
    }
}

private fun String?.blankToNull(): String? = this?.takeIf(String::isNotBlank)

internal fun EnrollmentDto.toDomain() =
    Enrollment(courseId = courseId, enrolledAt = enrolledAt.toUtcInstantOrNull())

internal fun MyCourseDto.toDomain() =
    MyCourse(
        courseId = courseId,
        title = title,
        shortDescription = shortDescription.blankToNull(),
        thumbnailUrl = thumbnailUrl.blankToNull(),
        enrolledAt = enrolledAt.toUtcInstantOrNull(),
    )

internal fun MyCoursePageDto.toDomain() =
    MyCoursePage(
        items = items.map { it.toDomain() },
        page = page,
        pageSize = pageSize,
        totalCount = totalCount,
    )
