package com.toeicspace.android.feature.course.data.repository

import com.toeicspace.android.core.common.AppResult
import com.toeicspace.android.core.common.map
import com.toeicspace.android.core.network.safeApiCall
import com.toeicspace.android.feature.course.data.remote.CourseApiService
import com.toeicspace.android.feature.course.data.remote.apiName
import com.toeicspace.android.feature.course.data.remote.toDomain
import com.toeicspace.android.feature.course.domain.CourseDetail
import com.toeicspace.android.feature.course.domain.CourseFilter
import com.toeicspace.android.feature.course.domain.CoursePage
import com.toeicspace.android.feature.course.domain.CourseRepository
import kotlinx.serialization.json.Json
import javax.inject.Inject

/** Reads the catalog straight from the network; the screens hold what they have loaded. */
internal class CourseRepositoryImpl
    @Inject
    constructor(
        private val courseApiService: CourseApiService,
        private val json: Json,
    ) : CourseRepository {
        override suspend fun getCourses(
            filter: CourseFilter,
            page: Int,
            pageSize: Int,
        ): AppResult<CoursePage> =
            safeApiCall(json) {
                courseApiService.getCourses(
                    search = filter.search.trim().ifEmpty { null },
                    examType = filter.examType?.apiName,
                    level = filter.level?.apiName,
                    targetScore = filter.targetScore,
                    page = page,
                    pageSize = pageSize,
                )
            }.map { it.toDomain() }

        override suspend fun getCourse(id: String): AppResult<CourseDetail> =
            safeApiCall(json) { courseApiService.getCourse(id) }.map { it.toDomain() }
    }
