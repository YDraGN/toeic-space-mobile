package com.toeicspace.android.feature.course.domain

import com.toeicspace.android.core.common.AppResult

/** Public course catalog: anyone can browse it, signed in or not. */
interface CourseRepository {
    suspend fun getCourses(
        filter: CourseFilter,
        page: Int,
        pageSize: Int = DEFAULT_PAGE_SIZE,
    ): AppResult<CoursePage>

    /** A course that is not published (or no longer exists) fails with a 404 server error. */
    suspend fun getCourse(id: String): AppResult<CourseDetail>

    companion object {
        const val DEFAULT_PAGE_SIZE = 20
    }
}
