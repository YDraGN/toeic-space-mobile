package com.toeicspace.android.feature.lesson.data.remote

import com.toeicspace.android.feature.lesson.domain.LessonAttachment
import com.toeicspace.android.feature.lesson.domain.LessonContent
import com.toeicspace.android.feature.lesson.domain.LessonVideo
import kotlinx.serialization.Serializable

/** `PublicLessonPreviewDto`, returned by both the enrolled and the free-preview endpoint. */
@Serializable
data class LessonContentDto(
    val id: String,
    val title: String,
    val textContent: String? = null,
    val videos: List<LessonVideoDto> = emptyList(),
    val attachments: List<LessonAttachmentDto> = emptyList(),
)

/** `PublicVideoDto`. */
@Serializable
data class LessonVideoDto(
    val id: String,
    val fileName: String,
    val contentType: String? = null,
    val fileSize: Long = 0,
    val durationSeconds: Int? = null,
    val sortOrder: Int = 0,
    val url: String,
)

/** `PublicAttachmentDto`. */
@Serializable
data class LessonAttachmentDto(
    val id: String,
    val fileName: String,
    val contentType: String? = null,
    val fileSize: Long = 0,
    val url: String,
)

internal fun LessonContentDto.toDomain() =
    LessonContent(
        id = id,
        title = title,
        textContent = textContent?.takeIf(String::isNotBlank),
        videos =
            videos.sortedBy { it.sortOrder }.map {
                LessonVideo(
                    id = it.id,
                    fileName = it.fileName,
                    durationSeconds = it.durationSeconds,
                    url = it.url,
                )
            },
        attachments =
            attachments.map {
                LessonAttachment(
                    id = it.id,
                    fileName = it.fileName,
                    contentType = it.contentType,
                    fileSize = it.fileSize,
                    url = it.url,
                )
            },
    )
