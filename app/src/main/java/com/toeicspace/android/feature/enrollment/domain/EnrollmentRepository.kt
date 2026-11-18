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

    companion object {
        const val DEFAULT_PAGE_SIZE = 20

        /** The backend allows at most this many items per page. */
        const val MAX_PAGE_SIZE = 100

        const val ERROR_COURSE_FULL = "COURSE_FULL"
        const val ERROR_FREE_ENROLLMENT_UNAVAILABLE = "FREE_ENROLLMENT_UNAVAILABLE"
    }
}
