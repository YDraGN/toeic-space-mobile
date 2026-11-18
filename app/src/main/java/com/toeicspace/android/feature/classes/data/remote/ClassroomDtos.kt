package com.toeicspace.android.feature.classes.data.remote

import com.toeicspace.android.feature.classes.domain.StudentClass
import com.toeicspace.android.feature.classes.domain.StudentClassStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.format.DateTimeParseException

/*
 * Classroom sends enums as their names ("Active") and dates as ISO `DateOnly` ("2026-09-01").
 * Audit fields (createdBy, updatedAt, ...) are not needed by the student screens and are ignored.
 */

/** `PagedResult<ClassDto>`. */
@Serializable
data class StudentClassPageDto(
    val items: List<StudentClassDto> = emptyList(),
    val page: Int,
    val pageSize: Int,
    val totalCount: Int,
)

/** `ClassDto`. */
@Serializable
data class StudentClassDto(
    val id: String,
    val code: String,
    val name: String,
    val description: String? = null,
    val status: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val teacherId: String? = null,
    val teacherName: String? = null,
)

/** `ClassDetailDto`. Students always get an empty instructor history, so it is not read. */
@Serializable
data class StudentClassDetailDto(
    @SerialName("class") val classInfo: StudentClassDto,
)

private fun String?.toStatus(): StudentClassStatus? =
    StudentClassStatus.entries.firstOrNull { it.name.equals(this, ignoreCase = true) }

private fun String?.toLocalDateOrNull(): LocalDate? =
    this?.let {
        try {
            // A date-time ("2026-09-01T00:00:00") still yields its date.
            LocalDate.parse(it.substringBefore('T'))
        } catch (_: DateTimeParseException) {
            null
        }
    }

private fun String?.blankToNull(): String? = this?.trim()?.takeIf(String::isNotEmpty)

internal fun StudentClassDto.toDomain() =
    StudentClass(
        id = id,
        code = code,
        name = name,
        description = description.blankToNull(),
        status = status.toStatus(),
        startDate = startDate.toLocalDateOrNull(),
        endDate = endDate.toLocalDateOrNull(),
        teacherName = teacherName.blankToNull(),
        hasTeacher = teacherId != null || !teacherName.isNullOrBlank(),
    )
