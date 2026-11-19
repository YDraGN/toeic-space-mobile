package com.toeicspace.android.feature.vocabulary.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/** The signed-in learner's summary, quiz builder and server-side study sessions (Content service). */
interface ReviewApiService {
    @GET("content/api/v1/vocabulary/me/summary")
    suspend fun getSummary(
        @Query("tzOffset") tzOffset: Int,
    ): LearnerSummaryDto

    /** Returns 201 for a new session and 200 when the open one for the same source is resumed. */
    @POST("content/api/v1/vocabulary/me/sessions")
    suspend fun startSession(
        @Body body: StartSessionRequest,
    ): StudySessionDto

    @GET("content/api/v1/vocabulary/me/sessions/{id}")
    suspend fun getSession(
        @Path("id") id: String,
        @Query("tzOffset") tzOffset: Int,
    ): StudySessionDto

    @POST("content/api/v1/vocabulary/me/sessions/{id}/answers")
    suspend fun answer(
        @Path("id") id: String,
        @Body body: SessionAnswerRequest,
    ): StudySessionDto

    @POST("content/api/v1/vocabulary/me/sessions/{id}/complete")
    suspend fun complete(
        @Path("id") id: String,
        @Query("tzOffset") tzOffset: Int,
    ): StudySessionDto

    /** One question per word id, in the same order (1 to 50 ids). */
    @POST("content/api/v1/vocabulary/quizzes")
    suspend fun createQuiz(
        @Body body: QuizRequest,
    ): List<QuizQuestionDto>
}
