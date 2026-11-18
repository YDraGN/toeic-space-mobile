package com.toeicspace.android.feature.enrollment.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/** Content service enrollment endpoints. They need a signed-in user, so none is public. */
interface EnrollmentApiService {
    /** Free published courses only; enrolling twice returns the existing enrollment. */
    @POST("content/api/v1/enrollments/{courseId}")
    suspend fun enroll(
        @Path("courseId") courseId: String,
    ): EnrollmentDto

    /** 200 with the enrollment, or 204 with no body when the user is not enrolled. */
    @GET("content/api/v1/enrollments/{courseId}")
    suspend fun getEnrollment(
        @Path("courseId") courseId: String,
    ): Response<EnrollmentDto>

    @GET("content/api/v1/enrollments/me")
    suspend fun getMyCourses(
        @Query("search") search: String?,
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int,
    ): MyCoursePageDto
}
