package com.toeicspace.android.feature.vocabulary.data.local

import com.toeicspace.android.feature.vocabulary.data.remote.QuizQuestionDto
import com.toeicspace.android.feature.vocabulary.data.remote.WordDto
import com.toeicspace.android.feature.vocabulary.data.remote.toDomain
import com.toeicspace.android.feature.vocabulary.data.remote.toReviewGrade
import com.toeicspace.android.feature.vocabulary.domain.ReviewSession
import com.toeicspace.android.feature.vocabulary.domain.ReviewSource
import kotlinx.serialization.Serializable

/** An answer recorded on the device, waiting to be sent. Sent in the order they were given. */
@Serializable
data class PendingAnswer(
    val sessionId: String,
    val wordId: String,
    val grade: String,
    val clientEventId: String,
    val tzOffset: Int,
)

/**
 * The open review session as the device last knew it, so it can be continued offline or after
 * the app was closed: queue, position and grades, plus the words and quiz questions it needs.
 */
@Serializable
data class ReviewSnapshot(
    val sessionId: String,
    val source: String,
    val queue: List<String>,
    val position: Int,
    val results: Map<String, String> = emptyMap(),
    val correctCount: Int = 0,
    val reviewCount: Int = 0,
    val words: List<WordDto> = emptyList(),
    val questions: List<QuizQuestionDto> = emptyList(),
)

internal fun ReviewSnapshot.toDomain() =
    ReviewSession(
        id = sessionId,
        source = ReviewSource.entries.firstOrNull { it.name == source } ?: ReviewSource.WORDS,
        queue = queue,
        position = position,
        results = results.mapValues { (_, grade) -> grade.toReviewGrade() },
        correctCount = correctCount,
        reviewCount = reviewCount,
        words = words.associate { it.id to it.toDomain() },
        questions = questions.associate { it.wordId to it.toDomain() },
    )
