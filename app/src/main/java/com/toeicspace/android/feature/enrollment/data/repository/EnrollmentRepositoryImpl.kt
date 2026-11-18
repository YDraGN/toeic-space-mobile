package com.toeicspace.android.feature.enrollment.data.repository

import com.toeicspace.android.core.common.AppResult
import com.toeicspace.android.core.common.map
import com.toeicspace.android.core.network.safeApiCall
import com.toeicspace.android.feature.enrollment.data.remote.EnrollmentApiService
import com.toeicspace.android.feature.enrollment.data.remote.toDomain
import com.toeicspace.android.feature.enrollment.domain.Enrollment
import com.toeicspace.android.feature.enrollment.domain.EnrollmentRepository
import com.toeicspace.android.feature.enrollment.domain.MyCoursePage
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import javax.inject.Inject

/** Straight from the network: an enrollment can appear on another device or after a payment. */
internal class EnrollmentRepositoryImpl
    @Inject
    constructor(
        private val enrollmentApiService: EnrollmentApiService,
        private val json: Json,
    ) : EnrollmentRepository {
        override suspend fun enroll(courseId: String): AppResult<Enrollment> =
            safeApiCall(json) { enrollmentApiService.enroll(courseId) }.map { it.toDomain() }

        override suspend fun getEnrollment(courseId: String): AppResult<Enrollment?> =
            safeApiCall(json) {
                val response = enrollmentApiService.getEnrollment(courseId)
                // Response<T> does not throw on HTTP errors, so they are rethrown for safeApiCall.
                if (!response.isSuccessful) throw HttpException(response)
                response.body()
            }.map { it?.toDomain() }

        override suspend fun getMyCourses(
            search: String,
            page: Int,
            pageSize: Int,
        ): AppResult<MyCoursePage> =
            safeApiCall(json) {
                enrollmentApiService.getMyCourses(
                    search = search.trim().ifEmpty { null },
                    page = page,
                    pageSize = pageSize.coerceIn(1, EnrollmentRepository.MAX_PAGE_SIZE),
                )
            }.map { it.toDomain() }
    }
