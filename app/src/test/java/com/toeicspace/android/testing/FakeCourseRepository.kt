package com.toeicspace.android.testing

import com.toeicspace.android.core.common.AppError
import com.toeicspace.android.core.common.AppResult
import com.toeicspace.android.feature.course.domain.CourseDetail
import com.toeicspace.android.feature.course.domain.CourseFilter
import com.toeicspace.android.feature.course.domain.CoursePage
import com.toeicspace.android.feature.course.domain.CoursePrice
import com.toeicspace.android.feature.course.domain.CourseRepository
import com.toeicspace.android.feature.course.domain.CourseSummary

fun testCourse(id: String) =
    CourseSummary(
        id = id,
        title = "Course $id",
        shortDescription = null,
        thumbnailUrl = null,
        examType = null,
        level = null,
        targetScore = null,
        price = CoursePrice(amount = 0, currency = "VND"),
        capacity = null,
        remainingSeats = null,
    )

/**
 * Serves [courses] in pages of [pageSize], whatever the filter. Set [failure] to fail every call,
 * or [failOnPage] to fail only one page.
 */
class FakeCourseRepository(
    var courses: List<CourseSummary> = (1..5).map { testCourse("$it") },
    private val pageSize: Int = 2,
) : CourseRepository {
    val requests = mutableListOf<Pair<CourseFilter, Int>>()
    var failure: AppError? = null
    var failOnPage: Int? = null
    var detail: AppResult<CourseDetail> =
        AppResult.Failure(
            AppError.Server(code = null, status = 404),
        )

    override suspend fun getCourses(
        filter: CourseFilter,
        page: Int,
        pageSize: Int,
    ): AppResult<CoursePage> {
        requests += filter to page
        failure?.let { return AppResult.Failure(it) }
        if (page == failOnPage) return AppResult.Failure(AppError.Network)
        val items = courses.drop((page - 1) * this.pageSize).take(this.pageSize)
        return AppResult.Success(CoursePage(items, page, this.pageSize, courses.size))
    }

    override suspend fun getCourse(id: String): AppResult<CourseDetail> = detail
}
