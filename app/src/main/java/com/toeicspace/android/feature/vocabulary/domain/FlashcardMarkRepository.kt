package com.toeicspace.android.feature.vocabulary.domain

import kotlinx.serialization.Serializable

/** How the learner sorted a flashcard: swiped right (known) or left (still learning). */
@Serializable
enum class FlashcardMark {
    KNOWN,
    LEARNING,
}

/**
 * The learner's flashcard marks. Every mark is kept on the device per set, for guests too, so the
 * cards remember it next time. For a signed-in learner each mark is also sent as a flashcard review
 * (`POST me/reviews`, known = Good, learning = Forget), which moves their progress like on the
 * web; marks made offline wait in a queue and go out later.
 */
interface FlashcardMarkRepository {
    /** The marks saved for [setId], keyed by word id; words never marked are missing. */
    suspend fun getMarks(setId: String): Map<String, FlashcardMark>

    /** Saves the mark and, when signed in, sends it (or queues it when that fails). */
    suspend fun mark(
        setId: String,
        wordId: String,
        mark: FlashcardMark,
    )

    /** Sends the queued reviews in order; the ones that cannot go out yet stay queued. */
    suspend fun flushPending()
}
