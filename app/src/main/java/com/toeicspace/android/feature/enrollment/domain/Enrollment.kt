package com.toeicspace.android.feature.enrollment.domain

import java.time.Instant

/** The signed-in learner owns this course and may open every lesson in it. */
data class Enrollment(
    val courseId: String,
    val enrolledAt: Instant?,
)

/** A course in "Khoá học của tôi". The backend does not track progress yet. */
data class MyCourse(
    val courseId: String,
    val title: String,
    val shortDescription: String?,
    /** Signed for five minutes: refetch the list rather than keep it. */
    val thumbnailUrl: String?,
    val enrolledAt: Instant?,
)

/** One page of the learner's courses, newest enrollment first; [page] starts at 1. */
data class MyCoursePage(
    val items: List<MyCourse>,
    val page: Int,
    val pageSize: Int,
    val totalCount: Int,
) {
    val hasNextPage: Boolean get() = page.toLong() * pageSize < totalCount
}
