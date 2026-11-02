package com.toeicspace.android.core.network

import com.toeicspace.android.core.network.model.AuthResponseDto
import com.toeicspace.android.core.network.model.RefreshTokenRequestDto
import retrofit2.http.Body
import retrofit2.http.POST

/** Session endpoints, called on a client without the auth interceptor so a refresh never recurses. */
interface SessionApiService {
    @PublicEndpoint
    @POST("identity/api/auth/refresh")
    suspend fun refresh(
        @Body body: RefreshTokenRequestDto,
    ): AuthResponseDto

    @PublicEndpoint
    @POST("identity/api/auth/logout")
    suspend fun logout(
        @Body body: RefreshTokenRequestDto,
    )
}
