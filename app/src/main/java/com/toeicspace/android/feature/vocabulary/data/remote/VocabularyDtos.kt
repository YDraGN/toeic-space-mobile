package com.toeicspace.android.feature.vocabulary.data.remote

import com.toeicspace.android.feature.vocabulary.domain.PartOfSpeech
import com.toeicspace.android.feature.vocabulary.domain.SetProgress
import com.toeicspace.android.feature.vocabulary.domain.StageProgress
import com.toeicspace.android.feature.vocabulary.domain.VocabularySet
import com.toeicspace.android.feature.vocabulary.domain.VocabularySetDetail
import com.toeicspace.android.feature.vocabulary.domain.VocabularyStage
import com.toeicspace.android.feature.vocabulary.domain.VocabularyWord
import kotlinx.serialization.Serializable

/*
 * Content sends enums as their names ("Noun", "Flashcard") and Guids as strings. The same classes
 * are written to the device as the offline copy, so they stay plain serializable data.
 */

/** `StageDto`. */
@Serializable
data class StageDto(
    val id: String,
    val slug: String = "",
    val title: String,
    val description: String? = null,
    val order: Int = 0,
    val toeicTarget: Int? = null,
    val setCount: Int = 0,
    val wordCount: Int = 0,
)

/** `SetSummaryDto`. */
@Serializable
data class SetSummaryDto(
    val id: String,
    val stageId: String,
    val slug: String = "",
    val title: String,
    val order: Int = 0,
    val wordCount: Int = 0,
    val previewWords: List<String> = emptyList(),
    val lastWord: String? = null,
)

/** `SetProgressDto`. */
@Serializable
data class SetProgressDto(
    val setId: String,
    val wordCount: Int = 0,
    val learning: Int = 0,
    val mastered: Int = 0,
    val due: Int = 0,
)

/** `StageProgressDto`, one entry of `me/summary` `stages`. */
@Serializable
data class StageProgressDto(
    val stageId: String,
    val wordCount: Int = 0,
    val learning: Int = 0,
    val mastered: Int = 0,
    val setCount: Int = 0,
    val setsCompleted: Int = 0,
)

/**
 * Body of `POST me/reviews` (`SubmitReviewCommand`). The app's Json leaves out default values, so
 * nothing here has a default and every field is always sent.
 */
@Serializable
data class SubmitReviewRequest(
    val wordId: String,
    val mode: String,
    val grade: String,
    val clientEventId: String,
    val tzOffset: Int,
)

internal const val MODE_FLASHCARD = "Flashcard"
internal const val GRADE_GOOD = "Good"
internal const val GRADE_FORGET = "Forget"

/** Only the `stages` of `LearnerSummaryDto`; the review card reads the rest. */
@Serializable
data class SummaryStagesDto(
    val stages: List<StageProgressDto> = emptyList(),
)

/** `WordDto`; the personal-edit fields (`note`, `personal`, `original`) are not shown on mobile yet. */
@Serializable
data class WordDto(
    val id: String,
    val headword: String,
    val phonetic: String? = null,
    val partOfSpeech: String? = null,
    val meaningVi: String = "",
    val definitionEn: String? = null,
    val example: String? = null,
    val exampleVi: String? = null,
)

/** `SetDetailDto`. */
@Serializable
data class SetDetailDto(
    val id: String,
    val stageId: String,
    val stageSlug: String = "",
    val stageTitle: String = "",
    val slug: String = "",
    val title: String,
    val order: Int = 0,
    val previousSetId: String? = null,
    val nextSetId: String? = null,
    val words: List<WordDto> = emptyList(),
)

private fun String?.blankToNull(): String? = this?.takeIf(String::isNotBlank)

internal fun String?.toPartOfSpeech(): PartOfSpeech =
    PartOfSpeech.entries.firstOrNull { it.name.equals(this, ignoreCase = true) }
        ?: PartOfSpeech.OTHER

internal fun StageDto.toDomain() =
    VocabularyStage(
        id = id,
        title = title,
        description = description.blankToNull(),
        order = order,
        toeicTarget = toeicTarget,
        setCount = setCount,
        wordCount = wordCount,
    )

internal fun SetSummaryDto.toDomain() =
    VocabularySet(
        id = id,
        stageId = stageId,
        title = title,
        order = order,
        wordCount = wordCount,
        previewWords = previewWords.filter(String::isNotBlank),
        lastWord = lastWord.blankToNull(),
    )

internal fun SetProgressDto.toDomain() =
    SetProgress(
        setId = setId,
        wordCount = wordCount,
        learning = learning,
        mastered = mastered,
        due = due,
    )

internal fun StageProgressDto.toDomain() =
    StageProgress(
        stageId = stageId,
        wordCount = wordCount,
        learning = learning,
        mastered = mastered,
        setCount = setCount,
        setsCompleted = setsCompleted,
    )

internal fun WordDto.toDomain() =
    VocabularyWord(
        id = id,
        headword = headword,
        phonetic = phonetic.blankToNull(),
        partOfSpeech = partOfSpeech.toPartOfSpeech(),
        meaningVi = meaningVi,
        definitionEn = definitionEn.blankToNull(),
        example = example.blankToNull(),
        exampleVi = exampleVi.blankToNull(),
    )

internal fun SetDetailDto.toDomain() =
    VocabularySetDetail(
        id = id,
        stageId = stageId,
        stageTitle = stageTitle,
        title = title,
        order = order,
        previousSetId = previousSetId,
        nextSetId = nextSetId,
        words = words.map { it.toDomain() },
    )
