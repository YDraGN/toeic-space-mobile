package com.toeicspace.android.feature.classes.domain

import java.time.LocalDate

/** Lifecycle of a class as Classroom reports it. Deleted classes never reach a student. */
enum class StudentClassStatus {
    DRAFT,
    ACTIVE,
    COMPLETED,
    ARCHIVED,
}

/**
 * A class the signed-in student is currently enrolled in (`ClassDto` from Classroom). The API sends
 * neither the course a class teaches nor a schedule yet, so neither is modelled here.
 */
data class StudentClass(
    val id: String,
    val code: String,
    val name: String,
    val description: String?,
    /** Null for a status this app version does not know; the screens then show no tag. */
    val status: StudentClassStatus?,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    /** Null when no teacher is assigned or the teacher's name has not synced yet. */
    val teacherName: String?,
    /** A teacher is assigned, even if [teacherName] is still unknown. */
    val hasTeacher: Boolean,
) {
    /** Completed and archived classes can only be looked at. */
    val isReadOnly: Boolean
        get() = status == StudentClassStatus.COMPLETED || status == StudentClassStatus.ARCHIVED
}
