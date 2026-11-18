package com.toeicspace.android.testing

import com.toeicspace.android.core.common.AppError
import com.toeicspace.android.core.common.AppResult
import com.toeicspace.android.feature.enrollment.domain.Enrollment
import com.toeicspace.android.feature.enrollment.domain.EnrollmentRepository
import com.toeicspace.android.feature.enrollment.domain.MyCourse
import com.toeicspace.android.feature.enrollment.domain.MyCoursePage

fun testMyCourse(id: String) =
    MyCourse(
        courseId = id,
        title = "Course $id",
        shortDescription = null,
        thumbnailUrl = null,
        enrolledAt = null,
    )

/**
 * Enrolled course ids live in [enrolled]; [myCourses] is served in pages of [pageSize] whatever
 * the search. Set a failure field to make that call fail.
 */
class FakeEnrollmentRepository(
    var myCourses: List<MyCourse> = (1..5).map { testMyCourse("$it") },
    private val pageSize: Int = 2,
) : EnrollmentRepository {
    val enrolled = mutableSetOf<String>()
    var enrollFailure: AppError? = null
    var statusFailure: AppError? = null
    var myCoursesFailure: AppError? = null

    val enrollRequests = mutableListOf<String>()
    val statusRequests = mutableListOf<String>()
    val myCoursesRequests = mutableListOf<Pair<String, Int>>()

    override suspend fun enroll(courseId: String): AppResult<Enrollment> {
        enrollRequests += courseId
        enrollFailure?.let { return AppResult.Failure(it) }
        enrolled += courseId
        return AppResult.Success(Enrollment(courseId, enrolledAt = null))
    }

    override suspend fun getEnrollment(courseId: String): AppResult<Enrollment?> {
        statusRequests += courseId
        statusFailure?.let { return AppResult.Failure(it) }
        return AppResult.Success(
            if (courseId in enrolled) Enrollment(courseId, enrolledAt = null) else null,
        )
    }

    override suspend fun getMyCourses(
        search: String,
        page: Int,
        pageSize: Int,
    ): AppResult<MyCoursePage> {
        myCoursesRequests += search to page
        myCoursesFailure?.let { return AppResult.Failure(it) }
        val items = myCourses.drop((page - 1) * this.pageSize).take(this.pageSize)
        return AppResult.Success(MyCoursePage(items, page, this.pageSize, myCourses.size))
    }
}
