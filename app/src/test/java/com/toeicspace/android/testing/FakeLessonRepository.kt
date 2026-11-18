package com.toeicspace.android.testing

import com.toeicspace.android.core.common.AppError
import com.toeicspace.android.core.common.AppResult
import com.toeicspace.android.feature.lesson.domain.LessonAttachment
import com.toeicspace.android.feature.lesson.domain.LessonContent
import com.toeicspace.android.feature.lesson.domain.LessonRepository
import com.toeicspace.android.feature.lesson.domain.LessonVideo

/**
 * Serves any lesson id. Each call bumps [version], which ends up in the media URLs, so a test can
 * tell a fresh link from the old one.
 */
class FakeLessonRepository : LessonRepository {
    var failure: AppError? = null
    var version = 0
        private set
    val requests = mutableListOf<Triple<String, String, Boolean>>()

    override suspend fun getLesson(
        courseId: String,
        lessonId: String,
        preview: Boolean,
    ): AppResult<LessonContent> {
        requests += Triple(courseId, lessonId, preview)
        failure?.let { return AppResult.Failure(it) }
        version++
        return AppResult.Success(
            LessonContent(
                id = lessonId,
                title = "Lesson $lessonId",
                textContent = null,
                videos =
                    listOf(
                        LessonVideo("v1", "a.mp4", 60, "https://cdn/$lessonId/v1?sig=$version"),
                        LessonVideo("v2", "b.mp4", 60, "https://cdn/$lessonId/v2?sig=$version"),
                    ),
                attachments =
                    listOf(
                        LessonAttachment(
                            "a1",
                            "a.pdf",
                            "application/pdf",
                            1,
                            "https://cdn/$lessonId/a1?sig=$version",
                        ),
                    ),
            ),
        )
    }
}
