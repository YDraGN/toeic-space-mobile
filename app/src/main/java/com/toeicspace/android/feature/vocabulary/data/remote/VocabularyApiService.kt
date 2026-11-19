package com.toeicspace.android.feature.vocabulary.data.remote

import com.toeicspace.android.core.network.PublicEndpoint
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Content service roadmap endpoints. The catalog is public ([PublicEndpoint]) so guests can study;
 * the set detail is not marked public because a token adds the learner's personal edits, and the
 * auth interceptor simply sends none for a guest. `me/...` needs a signed-in learner.
 */
interface VocabularyApiService {
    @PublicEndpoint
    @GET("content/api/v1/vocabulary/stages")
    suspend fun getStages(): List<StageDto>

    @PublicEndpoint
    @GET("content/api/v1/vocabulary/stages/{stageId}/sets")
    suspend fun getStageSets(
        @Path("stageId") stageId: String,
    ): List<SetSummaryDto>

    @GET("content/api/v1/vocabulary/sets/{setId}")
    suspend fun getSet(
        @Path("setId") setId: String,
    ): SetDetailDto

    @GET("content/api/v1/vocabulary/me/stages/{stageId}/sets")
    suspend fun getStageProgress(
        @Path("stageId") stageId: String,
        @Query("tzOffset") tzOffset: Int,
    ): List<SetProgressDto>

    /** One flashcard grade; idempotent per `clientEventId`, so a resend is harmless. */
    @POST("content/api/v1/vocabulary/me/reviews")
    suspend fun submitReview(
        @Body body: SubmitReviewRequest,
    )

    /** Signed-in only; the tab reads just the per-stage progress from it. */
    @GET("content/api/v1/vocabulary/me/summary")
    suspend fun getSummaryStages(
        @Query("tzOffset") tzOffset: Int,
    ): SummaryStagesDto
}
