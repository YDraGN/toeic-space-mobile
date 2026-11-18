package com.toeicspace.android.feature.enrollment.domain

import com.toeicspace.android.core.common.AppResult

/** The signed-in learner's enrollments. Every call needs a session; guests must sign in first. */
interface EnrollmentRepository {
    /**
     * Enrolls in a free published course; enrolling twice returns the same enrollment. A paid course
     * fails with [ERROR_FREE_ENROLLMENT_UNAVAILABLE] and a full one with [ERROR_COURSE_FULL], both
     * 409 server errors.
     */
    suspend fun enroll(courseId: String): AppResult<Enrollment>

    /** Null when the learner is not enrolled. */
    suspend fun getEnrollment(courseId: String): AppResult<Enrollment?>

    suspend fun getMyCourses(
        search: String,
        page: Int,
        pageSize: Int = DEFAULT_PAGE_SIZE,
    ): AppResult<MyCoursePage>

    /**
     * Ids of every course the learner is enrolled in, read page by page like the web's
     * `useOwnedCourseIds`. The catalog and the related courses hide these.
     */
    suspend fun getOwnedCourseIds(): AppResult<Set<String>>

    companion object {
        const val DEFAULT_PAGE_SIZE = 20

        /** The backend allows at most this many items per page. */
        const val MAX_PAGE_SIZE = 100

        /** Stops reading owned ids after this many pages (10 000 courses), whatever the total says. */
        const val MAX_OWNED_PAGES = 100

        const val ERROR_COURSE_FULL = "COURSE_FULL"
        const val ERROR_FREE_ENROLLMENT_UNAVAILABLE = "FREE_ENROLLMENT_UNAVAILABLE"
    }
}
