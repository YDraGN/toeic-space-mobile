package com.toeicspace.android.feature.course.data.remote

import com.toeicspace.android.core.network.PublicEndpoint
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Content service public course endpoints. They need no token, so they are [PublicEndpoint]:
 * a guest can browse, and an expired session does not trigger a refresh just to read the catalog.
 * Null query values are left out of the URL.
 */
interface CourseApiService {
    @PublicEndpoint
    @GET("content/api/v1/public/courses")
    suspend fun getCourses(
        @Query("search") search: String?,
        @Query("examType") examType: String?,
        @Query("level") level: String?,
        @Query("targetScore") targetScore: Int?,
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int,
    ): CoursePageDto

    @PublicEndpoint
    @GET("content/api/v1/public/courses/{id}")
    suspend fun getCourse(
        @Path("id") id: String,
    ): CourseDetailDto
}
