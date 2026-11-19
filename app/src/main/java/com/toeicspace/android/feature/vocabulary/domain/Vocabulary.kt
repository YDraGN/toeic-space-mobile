package com.toeicspace.android.feature.vocabulary.domain

/** One step of the TOEIC roadmap ("Khởi động", "Phản xạ"...), cut into numbered lessons (sets). */
data class VocabularyStage(
    val id: String,
    val title: String,
    val description: String?,
    val order: Int,
    val toeicTarget: Int?,
    val setCount: Int,
    val wordCount: Int,
)

/** A lesson of up to 50 words. [previewWords] are its first headwords, [lastWord] the last one. */
data class VocabularySet(
    val id: String,
    val stageId: String,
    val title: String,
    val order: Int,
    val wordCount: Int,
    val previewWords: List<String>,
    val lastWord: String?,
)

/** What a signed-in learner has done in one set. Sets they never opened have no progress. */
data class SetProgress(
    val setId: String,
    val wordCount: Int,
    val learning: Int,
    val mastered: Int,
    val due: Int,
) {
    /** Words with any progress: being learned or already mastered. */
    val learned: Int get() = (learning + mastered).coerceAtMost(wordCount)
}

/** What a signed-in learner has done in one stage (`me/summary` stages). */
data class StageProgress(
    val stageId: String,
    val wordCount: Int,
    val learning: Int,
    val mastered: Int,
    val setCount: Int,
    /** Sets where every word has been studied at least once. */
    val setsCompleted: Int,
) {
    /** Share of the stage's sets completed, 0 to 1, for the bar on the stage card. */
    val fraction: Float
        get() = if (setCount <= 0) 0f else (setsCompleted.toFloat() / setCount).coerceIn(0f, 1f)
}

enum class PartOfSpeech {
    NOUN,
    VERB,
    ADJECTIVE,
    ADVERB,
    PREPOSITION,
    CONJUNCTION,
    PRONOUN,
    DETERMINER,
    INTERJECTION,
    PHRASE,
    OTHER,
}

data class VocabularyWord(
    val id: String,
    val headword: String,
    val phonetic: String?,
    val partOfSpeech: PartOfSpeech,
    val meaningVi: String,
    val definitionEn: String?,
    val example: String?,
    val exampleVi: String?,
)

data class VocabularySetDetail(
    val id: String,
    val stageId: String,
    val stageTitle: String,
    val title: String,
    val order: Int,
    val previousSetId: String?,
    val nextSetId: String?,
    val words: List<VocabularyWord>,
)

/**
 * A result the repository may have read from the device instead of the network. [fromCache] is
 * true when the network failed and the last saved copy was used, so the screen can say it is
 * offline while still showing the data.
 */
data class Fetched<out T>(
    val data: T,
    val fromCache: Boolean = false,
)
