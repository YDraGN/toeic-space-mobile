package com.toeicspace.android.feature.profile.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

/** Identity profile endpoints for the signed-in user. Paths include the gateway prefix. */
interface ProfileApiService {
    @GET("identity/api/v1/users/me")
    suspend fun getMyProfile(): UserProfileDto

    @PUT("identity/api/v1/users/me")
    suspend fun updateMyProfile(
        @Body body: UpdateProfileRequestDto,
    ): UserProfileDto
}
