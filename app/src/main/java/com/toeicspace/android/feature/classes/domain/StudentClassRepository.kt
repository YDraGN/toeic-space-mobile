package com.toeicspace.android.feature.classes.domain

import com.toeicspace.android.core.common.AppResult

/**
 * The classes of the signed-in student. What was loaded last is kept in memory for the same
 * account, so a screen can show it at once and keep showing it when the device goes offline.
 */
interface StudentClassRepository {
    /** Every class the student is enrolled in, most recently changed first. */
    suspend fun getMyClasses(): AppResult<List<StudentClass>>

    /** A class the student is not (or no longer) enrolled in fails with a 404 server error. */
    suspend fun getMyClass(id: String): AppResult<StudentClass>

    /** The last list loaded for the current account, or null when there is none. */
    suspend fun cachedClasses(): List<StudentClass>?

    /** The class as last loaded for the current account (from its detail or from the list). */
    suspend fun cachedClass(id: String): StudentClass?
}
