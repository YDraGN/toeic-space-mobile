package com.toeicspace.android.feature.vocabulary.domain

/** The learner's grade of one review, as the backend names them. */
enum class ReviewGrade {
    FORGET,
    HARD,
    GOOD,
    EASY,
    SEEN,
    ;

    val isRemembered: Boolean get() = this == GOOD || this == EASY
}

/** What a review session studies: today's due words, pending mistakes or a given word list. */
enum class ReviewSource {
    DUE,
    MISTAKES,
    WORDS,
}

/** The open session shown as "Ôn tiếp" on the tab. */
data class ActiveReview(
    val id: String,
    val source: ReviewSource,
    val position: Int,
    val total: Int,
)

/** The signed-in learner's numbers for today (`me/summary`). */
data class ReviewSummary(
    val due: Int,
    val reviewedToday: Int,
    val dailyGoal: Int,
    val streakDays: Int,
    val mistakes: Int,
    val learned: Int,
    /** An unfinished quiz review of today's due words, if any. */
    val activeReview: ActiveReview?,
)

data class QuizOption(
    val wordId: String,
    val headword: String,
)

/** "Pick the word for this meaning": [prompt] is the Vietnamese meaning of [wordId]. */
data class QuizQuestion(
    val wordId: String,
    val prompt: String,
    val partOfSpeech: PartOfSpeech,
    val options: List<QuizOption>,
)

/** Words to review again and how many were remembered. */
data class ReviewResult(
    val remembered: Int,
    val needReview: Int,
    val wordIds: List<String>,
)

/**
 * A quiz review in progress. [queue] holds word ids in study order, re-queued cards included, so
 * the card at [position] is the next question. [results] is the last grade of each word.
 */
data class ReviewSession(
    val id: String,
    val source: ReviewSource,
    val queue: List<String>,
    val position: Int,
    val results: Map<String, ReviewGrade>,
    val correctCount: Int,
    val reviewCount: Int,
    val words: Map<String, VocabularyWord>,
    val questions: Map<String, QuizQuestion>,
    /** Set once the server has closed the session. */
    val result: ReviewResult? = null,
) {
    val currentWordId: String? get() = queue.getOrNull(position)

    val isFinished: Boolean get() = position >= queue.size

    /** The words not answered right the last time they came up. */
    val wrongWordIds: List<String>
        get() = results.filterValues { !it.isRemembered }.keys.toList()

    /** The result the server will report, worked out on the device for offline display. */
    val localResult: ReviewResult
        get() {
            val wrong = wrongWordIds
            return ReviewResult(
                remembered = results.size - wrong.size,
                needReview = wrong.size,
                wordIds = wrong,
            )
        }

    /**
     * Grades the current card exactly as the backend does (`StudySession.Answer`): a word's first
     * Forget or Hard comes back [REQUEUE_GAP] cards later, or at the end, and the session moves on.
     * Doing the same here lets the learner keep going while the answers wait for the network.
     */
    fun answered(
        wordId: String,
        grade: ReviewGrade,
    ): ReviewSession {
        if (currentWordId != wordId) return this
        val requeue =
            (grade == ReviewGrade.FORGET || grade == ReviewGrade.HARD) && !isRequeued(wordId)
        val nextQueue =
            if (requeue) {
                queue.toMutableList().apply { add(minOf(position + 1 + REQUEUE_GAP, size), wordId) }
            } else {
                queue
            }
        return copy(
            queue = nextQueue,
            position = position + 1,
            results = results + (wordId to grade),
            correctCount = correctCount + if (grade.isRemembered) 1 else 0,
            reviewCount = reviewCount + 1,
        )
    }

    /** A word appears twice in the queue once it has been re-queued; each word is re-queued once. */
    private fun isRequeued(wordId: String): Boolean = queue.count { it == wordId } > 1

    companion object {
        const val REQUEUE_GAP = 4
    }
}
