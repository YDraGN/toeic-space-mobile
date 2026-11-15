package com.toeicspace.android.feature.course.data.remote

import com.toeicspace.android.feature.course.domain.CourseDetail
import com.toeicspace.android.feature.course.domain.CourseExamType
import com.toeicspace.android.feature.course.domain.CourseLesson
import com.toeicspace.android.feature.course.domain.CourseLevel
import com.toeicspace.android.feature.course.domain.CourseModule
import com.toeicspace.android.feature.course.domain.CoursePage
import com.toeicspace.android.feature.course.domain.CoursePrice
import com.toeicspace.android.feature.course.domain.CoursePromotion
import com.toeicspace.android.feature.course.domain.CourseSummary
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.format.DateTimeParseException
import kotlin.math.roundToLong

/*
 * Content sends enums as their names ("ListeningReading", "Beginner"). Prices are C# decimals,
 * read as Double so "499000" and "499000.0" both parse.
 */

@Serializable
data class CoursePageDto(
    val items: List<CourseSummaryDto> = emptyList(),
    val page: Int,
    val pageSize: Int,
    val totalCount: Int,
)

/** `PublicCourseSummaryDto`. */
@Serializable
data class CourseSummaryDto(
    val id: String,
    val title: String,
    val shortDescription: String? = null,
    val thumbnailUrl: String? = null,
    val examType: String? = null,
    val level: String? = null,
    val targetScore: Int? = null,
    val price: Double = 0.0,
    val currency: String = DEFAULT_CURRENCY,
    val capacity: Int? = null,
    val remainingSeats: Int? = null,
    val promotion: PromotionPriceDto? = null,
)

/** `PromotionPrice`. */
@Serializable
data class PromotionPriceDto(
    val name: String,
    val originalPrice: Double,
    val sellingPrice: Double,
    val expiresAt: String? = null,
)

/** `PublicCourseDetailDto`. */
@Serializable
data class CourseDetailDto(
    val id: String,
    val title: String,
    val shortDescription: String? = null,
    val description: String? = null,
    val thumbnailUrl: String? = null,
    val examType: String? = null,
    val level: String? = null,
    val targetScore: Int? = null,
    val skills: List<String> = emptyList(),
    val objectives: List<String> = emptyList(),
    val prerequisites: List<String> = emptyList(),
    val modules: List<CourseModuleDto> = emptyList(),
    val price: Double = 0.0,
    val currency: String = DEFAULT_CURRENCY,
    val capacity: Int? = null,
    val remainingSeats: Int? = null,
    val promotion: PromotionPriceDto? = null,
)

/** `PublicModuleDto`. */
@Serializable
data class CourseModuleDto(
    val id: String,
    val title: String,
    val description: String? = null,
    val sortOrder: Int = 0,
    val lessons: List<CourseLessonDto> = emptyList(),
)

/** `PublicLessonSummaryDto`. */
@Serializable
data class CourseLessonDto(
    val id: String,
    val title: String,
    val description: String? = null,
    val durationSeconds: Int? = null,
    val sortOrder: Int = 0,
    val isFreePreview: Boolean = false,
)

private const val DEFAULT_CURRENCY = "VND"

/** The backend name of each exam type, used both to read responses and to filter. */
internal val CourseExamType.apiName: String
    get() =
        when (this) {
            CourseExamType.LISTENING_READING -> "ListeningReading"
            CourseExamType.SPEAKING_WRITING -> "SpeakingWriting"
            CourseExamType.FOUR_SKILLS -> "FourSkills"
        }

internal val CourseLevel.apiName: String
    get() =
        when (this) {
            CourseLevel.BEGINNER -> "Beginner"
            CourseLevel.INTERMEDIATE -> "Intermediate"
            CourseLevel.ADVANCED -> "Advanced"
        }

private fun String?.toExamType(): CourseExamType? =
    CourseExamType.entries.firstOrNull {
        it.apiName.equals(this, ignoreCase = true)
    }

private fun String?.toLevel(): CourseLevel? =
    CourseLevel.entries.firstOrNull {
        it.apiName.equals(this, ignoreCase = true)
    }

private fun String?.blankToNull(): String? = this?.takeIf(String::isNotBlank)

private fun String?.toInstantOrNull(): Instant? =
    this?.let {
        try {
            Instant.parse(it)
        } catch (_: DateTimeParseException) {
            null
        }
    }

private fun PromotionPriceDto.toDomain() =
    CoursePromotion(
        name = name,
        originalPrice = originalPrice.roundToLong(),
        sellingPrice = sellingPrice.roundToLong(),
        expiresAt = expiresAt.toInstantOrNull(),
    )

private fun price(
    amount: Double,
    currency: String,
    promotion: PromotionPriceDto?,
) = CoursePrice(
    amount = amount.roundToLong(),
    currency = currency,
    promotion = promotion?.toDomain(),
)

internal fun CourseSummaryDto.toDomain() =
    CourseSummary(
        id = id,
        title = title,
        shortDescription = shortDescription.blankToNull(),
        thumbnailUrl = thumbnailUrl.blankToNull(),
        examType = examType.toExamType(),
        level = level.toLevel(),
        targetScore = targetScore,
        price = price(price, currency, promotion),
        capacity = capacity,
        remainingSeats = remainingSeats,
    )

internal fun CoursePageDto.toDomain() =
    CoursePage(
        items = items.map { it.toDomain() },
        page = page,
        pageSize = pageSize,
        totalCount = totalCount,
    )

internal fun CourseDetailDto.toDomain() =
    CourseDetail(
        summary =
            CourseSummaryDto(
                id = id,
                title = title,
                shortDescription = shortDescription,
                thumbnailUrl = thumbnailUrl,
                examType = examType,
                level = level,
                targetScore = targetScore,
                price = price,
                currency = currency,
                capacity = capacity,
                remainingSeats = remainingSeats,
                promotion = promotion,
            ).toDomain(),
        description = description.blankToNull(),
        skills = skills.filter(String::isNotBlank),
        objectives = objectives.filter(String::isNotBlank),
        prerequisites = prerequisites.filter(String::isNotBlank),
        modules =
            modules.sortedBy { it.sortOrder }.map { module ->
                CourseModule(
                    id = module.id,
                    title = module.title,
                    description = module.description.blankToNull(),
                    lessons =
                        module.lessons.sortedBy { it.sortOrder }.map { lesson ->
                            CourseLesson(
                                id = lesson.id,
                                title = lesson.title,
                                description = lesson.description.blankToNull(),
                                durationSeconds = lesson.durationSeconds,
                                isFreePreview = lesson.isFreePreview,
                            )
                        },
                )
            },
    )
