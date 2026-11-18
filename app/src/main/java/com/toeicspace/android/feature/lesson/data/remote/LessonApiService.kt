package com.toeicspace.android.feature.lesson.data.remote

import com.toeicspace.android.core.network.PublicEndpoint
import retrofit2.http.GET
import retrofit2.http.Path

/** Lesson content from the content service. Video and attachment URLs are signed for 5 minutes. */
interface LessonApiService {
    /** Any published lesson of a course the learner is enrolled in; 404 otherwise. */
    @GET("content/api/v1/enrollments/{courseId}/lessons/{lessonId}")
    suspend fun getEnrolledLesson(
        @Path("courseId") courseId: String,
        @Path("lessonId") lessonId: String,
    ): LessonContentDto

    /** Free-preview lessons only, open to anyone. */
    @PublicEndpoint
    @GET("content/api/v1/public/courses/{courseId}/lessons/{lessonId}/preview")
    suspend fun getPreviewLesson(
        @Path("courseId") courseId: String,
        @Path("lessonId") lessonId: String,
    ): LessonContentDto
}
