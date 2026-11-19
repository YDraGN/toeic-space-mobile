package com.toeicspace.android.feature.vocabulary.domain

import com.toeicspace.android.core.common.AppResult
import kotlinx.coroutines.flow.StateFlow

/**
 * Signed-in review sessions (quiz mode: pick the word for a meaning). Answers are kept on the
 * device first and sent in order, so a lost connection never loses one: they go out as soon as
 * the network is back, and the open session can be continued offline meanwhile.
 */
interface ReviewRepository {
    /** Answers recorded on the device and not yet accepted by the server. */
    val pendingAnswerCount: StateFlow<Int>

    /** Today's numbers; the last copy when offline. */
    suspend fun getSummary(): AppResult<Fetched<ReviewSummary>>

    /**
     * Resumes the open session of [source] (on the server, or on the device when it holds answers
     * the server has not seen yet), or starts a new one. [wordIds] is required for
     * [ReviewSource.WORDS], which always starts fresh. Nothing to review fails with a 400
     * `STUDY_SESSION_EMPTY` server error.
     */
    suspend fun startSession(
        source: ReviewSource,
        wordIds: List<String> = emptyList(),
    ): AppResult<ReviewSession>

    /** Records the answer to the current card on the device, then tries to send what is waiting. */
    suspend fun answer(
        session: ReviewSession,
        wordId: String,
        grade: ReviewGrade,
    ): ReviewSession

    /** Sends the waiting answers in order. Fails with a connectivity error while offline. */
    suspend fun flushPendingAnswers(): AppResult<Unit>

    /** Sends what is waiting, then closes the session and returns the server's result. */
    suspend fun completeSession(session: ReviewSession): AppResult<ReviewResult>
}
