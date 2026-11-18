package com.toeicspace.android.feature.lesson.domain

import com.toeicspace.android.core.common.AppResult

/** A lesson as the learner studies it. Media [LessonVideo.url]s expire five minutes after loading. */
data class LessonContent(
    val id: String,
    val title: String,
    /** Markdown written by the teacher. */
    val textContent: String?,
    /** In teaching order. */
    val videos: List<LessonVideo>,
    val attachments: List<LessonAttachment>,
)

data class LessonVideo(
    val id: String,
    val fileName: String,
    val durationSeconds: Int?,
    val url: String,
)

data class LessonAttachment(
    val id: String,
    val fileName: String,
    val contentType: String?,
    val fileSize: Long,
    val url: String,
)

interface LessonRepository {
    /**
     * [preview] reads a free-preview lesson, open to anyone; otherwise the learner must be enrolled.
     * Both fail with a 404 server error when the lesson cannot be opened.
     */
    suspend fun getLesson(
        courseId: String,
        lessonId: String,
        preview: Boolean,
    ): AppResult<LessonContent>
}
