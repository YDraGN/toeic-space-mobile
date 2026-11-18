package com.toeicspace.android.feature.classes.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/** Classroom service, student side (role User): the classes the caller is enrolled in. */
interface ClassroomApiService {
    @GET("classroom/api/v1/classes/me/enrolled")
    suspend fun getMyClasses(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int,
    ): StudentClassPageDto

    @GET("classroom/api/v1/classes/me/enrolled/{id}")
    suspend fun getMyClass(
        @Path("id") id: String,
    ): StudentClassDetailDto
}
