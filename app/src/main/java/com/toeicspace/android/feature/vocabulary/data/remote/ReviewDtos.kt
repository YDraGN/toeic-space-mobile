package com.toeicspace.android.feature.vocabulary.data.remote

import com.toeicspace.android.feature.vocabulary.domain.ActiveReview
import com.toeicspace.android.feature.vocabulary.domain.QuizOption
import com.toeicspace.android.feature.vocabulary.domain.QuizQuestion
import com.toeicspace.android.feature.vocabulary.domain.ReviewGrade
import com.toeicspace.android.feature.vocabulary.domain.ReviewResult
import com.toeicspace.android.feature.vocabulary.domain.ReviewSource
import com.toeicspace.android.feature.vocabulary.domain.ReviewSummary
import kotlinx.serialization.Serializable

internal const val MODE_QUIZ = "Quiz"

/** `LearnerSummaryDto` (only what the tab shows). */
@Serializable
data class LearnerSummaryDto(
    val learned: Int = 0,
    val due: Int = 0,
    val streakDays: Int = 0,
    val reviewedToday: Int = 0,
    val dailyGoal: Int = 20,
    val mistakes: Int = 0,
    val activeSession: ActiveSessionDto? = null,
)

/** `ActiveSessionDto`. */
@Serializable
data class ActiveSessionDto(
    val id: String,
    val mode: String,
    val source: StudySourceDto,
    val position: Int = 0,
    val total: Int = 0,
)

/** `StudySourceDto`: kind is Set, Due, Folder, Library, Words or Mistakes. */
@Serializable
data class StudySourceDto(
    val kind: String,
    val id: String? = null,
    val title: String? = null,
)

/**
 * Body of `POST me/sessions`: exactly one source is set. The app's Json leaves out nulls and
 * default values, so [mode] and [tzOffset] have no defaults and are always sent.
 */
@Serializable
data class StartSessionRequest(
    val mode: String,
    val tzOffset: Int,
    val due: Boolean? = null,
    val mistakes: Boolean? = null,
    val wordIds: List<String>? = null,
    val limit: Int? = null,
)

/** Body of `POST me/sessions/{id}/answers`. [clientEventId] makes a resend harmless. */
@Serializable
data class SessionAnswerRequest(
    val wordId: String,
    val grade: String,
    val clientEventId: String,
    val tzOffset: Int,
)

@Serializable
data class QuizRequest(
    val wordIds: List<String>,
)

@Serializable
data class QuizOptionDto(
    val wordId: String,
    val headword: String,
)

/** `QuizQuestionDto`. */
@Serializable
data class QuizQuestionDto(
    val wordId: String,
    val prompt: String,
    val partOfSpeech: String? = null,
    val options: List<QuizOptionDto> = emptyList(),
)

/** `StudyItemDto`; progress and preview are not shown during a quiz. */
@Serializable
data class StudyItemDto(
    val word: WordDto,
)

@Serializable
data class SessionResultDto(
    val wordId: String,
    val grade: String,
)

@Serializable
data class SessionSummaryDto(
    val remembered: Int = 0,
    val needReview: Int = 0,
    val wordIds: List<String> = emptyList(),
)

/** `StudySessionDto`: [items] is the queue in order, re-queued cards included. */
@Serializable
data class StudySessionDto(
    val id: String,
    val mode: String = MODE_QUIZ,
    val source: StudySourceDto,
    val position: Int = 0,
    val total: Int = 0,
    val correctCount: Int = 0,
    val reviewCount: Int = 0,
    val status: String = "Active",
    val items: List<StudyItemDto> = emptyList(),
    val results: List<SessionResultDto> = emptyList(),
    val summary: SessionSummaryDto? = null,
)

internal val ReviewGrade.apiName: String
    get() = name.lowercase().replaceFirstChar(Char::uppercaseChar)

internal fun String.toReviewGrade(): ReviewGrade =
    ReviewGrade.entries.firstOrNull { it.name.equals(this, ignoreCase = true) } ?: ReviewGrade.SEEN

internal fun StudySourceDto.toReviewSource(): ReviewSource =
    when (kind.lowercase()) {
        "due" -> ReviewSource.DUE
        "mistakes" -> ReviewSource.MISTAKES
        else -> ReviewSource.WORDS
    }

internal fun QuizQuestionDto.toDomain() =
    QuizQuestion(
        wordId = wordId,
        prompt = prompt,
        partOfSpeech = partOfSpeech.toPartOfSpeech(),
        options = options.map { QuizOption(it.wordId, it.headword) },
    )

internal fun SessionSummaryDto.toDomain() =
    ReviewResult(remembered = remembered, needReview = needReview, wordIds = wordIds)

internal fun LearnerSummaryDto.toDomain() =
    ReviewSummary(
        due = due,
        reviewedToday = reviewedToday,
        dailyGoal = dailyGoal,
        streakDays = streakDays,
        mistakes = mistakes,
        learned = learned,
        activeReview =
            activeSession
                ?.takeIf { it.mode.equals(MODE_QUIZ, ignoreCase = true) }
                ?.takeIf { it.source.kind.equals("Due", ignoreCase = true) }
                ?.let { ActiveReview(it.id, ReviewSource.DUE, it.position, it.total) },
    )
