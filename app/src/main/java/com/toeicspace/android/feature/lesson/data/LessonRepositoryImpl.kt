package com.toeicspace.android.feature.lesson.data

import com.toeicspace.android.core.common.AppResult
import com.toeicspace.android.core.common.map
import com.toeicspace.android.core.network.safeApiCall
import com.toeicspace.android.feature.lesson.data.remote.LessonApiService
import com.toeicspace.android.feature.lesson.data.remote.toDomain
import com.toeicspace.android.feature.lesson.domain.LessonContent
import com.toeicspace.android.feature.lesson.domain.LessonRepository
import kotlinx.serialization.json.Json
import javax.inject.Inject

/** Never cached: the signed media links inside expire within minutes. */
internal class LessonRepositoryImpl
    @Inject
    constructor(
        private val lessonApiService: LessonApiService,
        private val json: Json,
    ) : LessonRepository {
        override suspend fun getLesson(
            courseId: String,
            lessonId: String,
            preview: Boolean,
        ): AppResult<LessonContent> =
            safeApiCall(json) {
                if (preview) {
                    lessonApiService.getPreviewLesson(courseId, lessonId)
                } else {
                    lessonApiService.getEnrolledLesson(courseId, lessonId)
                }
            }.map { it.toDomain() }
    }
