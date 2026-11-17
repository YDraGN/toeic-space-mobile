package com.toeicspace.android.feature.course.domain

import java.time.Instant

/** The TOEIC exam a course prepares for. */
enum class CourseExamType {
    LISTENING_READING,
    SPEAKING_WRITING,
    FOUR_SKILLS,
}

enum class CourseLevel {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED,
}

/** A time-limited discount; [sellingPrice] is what the learner pays while it lasts. */
data class CoursePromotion(
    val name: String,
    val originalPrice: Long,
    val sellingPrice: Long,
    val expiresAt: Instant?,
)

/** Amounts are whole units of [currency] (VND has no minor unit). */
data class CoursePrice(
    val amount: Long,
    val currency: String,
    val promotion: CoursePromotion? = null,
) {
    /** What the learner pays now: the promotion price when there is one. */
    val payable: Long get() = promotion?.sellingPrice ?: amount

    val isFree: Boolean get() = payable == 0L
}

/** A published course as shown in the catalog. */
data class CourseSummary(
    val id: String,
    val title: String,
    val shortDescription: String?,
    val thumbnailUrl: String?,
    /** Null when the backend sends a value this app version does not know. */
    val examType: CourseExamType?,
    val level: CourseLevel?,
    val targetScore: Int?,
    val price: CoursePrice,
    /** Null means no seat limit; [remainingSeats] is then null too. */
    val capacity: Int?,
    val remainingSeats: Int?,
)

data class CourseLesson(
    val id: String,
    val title: String,
    val description: String?,
    val durationSeconds: Int?,
    /** Anyone may open this lesson before enrolling. */
    val isFreePreview: Boolean,
)

data class CourseModule(
    val id: String,
    val title: String,
    val description: String?,
    /** Published lessons only, in teaching order. */
    val lessons: List<CourseLesson>,
)

data class CourseDetail(
    val summary: CourseSummary,
    val description: String?,
    val skills: List<String>,
    val objectives: List<String>,
    val prerequisites: List<String>,
    /** In teaching order. */
    val modules: List<CourseModule>,
) {
    val lessonCount: Int get() = modules.sumOf { it.lessons.size }

    /** Sum of the lessons that have a duration; lessons without one are left out. */
    val totalDurationSeconds: Int get() =
        modules
            .flatMap { it.lessons }
            .sumOf { it.durationSeconds ?: 0 }
}

/** Catalog filters; an empty [search] or a null field means "any". */
data class CourseFilter(
    val search: String = "",
    val examType: CourseExamType? = null,
    val level: CourseLevel? = null,
    /** Courses aimed at this score or higher. */
    val targetScore: Int? = null,
)

/** One page of the catalog; [page] starts at 1. */
data class CoursePage(
    val items: List<CourseSummary>,
    val page: Int,
    val pageSize: Int,
    val totalCount: Int,
) {
    val hasNextPage: Boolean get() = page.toLong() * pageSize < totalCount
}
