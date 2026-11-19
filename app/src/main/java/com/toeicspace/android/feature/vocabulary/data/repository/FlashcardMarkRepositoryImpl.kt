package com.toeicspace.android.feature.vocabulary.data.repository

import com.toeicspace.android.core.common.AppError
import com.toeicspace.android.core.common.AppResult
import com.toeicspace.android.core.data.repository.SessionRepository
import com.toeicspace.android.core.model.SessionState
import com.toeicspace.android.core.network.safeApiCall
import com.toeicspace.android.feature.vocabulary.data.local.VocabularyLocalStore
import com.toeicspace.android.feature.vocabulary.data.remote.GRADE_FORGET
import com.toeicspace.android.feature.vocabulary.data.remote.GRADE_GOOD
import com.toeicspace.android.feature.vocabulary.data.remote.MODE_FLASHCARD
import com.toeicspace.android.feature.vocabulary.data.remote.SubmitReviewRequest
import com.toeicspace.android.feature.vocabulary.data.remote.VocabularyApiService
import com.toeicspace.android.feature.vocabulary.domain.FlashcardMark
import com.toeicspace.android.feature.vocabulary.domain.FlashcardMarkRepository
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject

private const val KEY_MARKS = "flashcard_marks_"
private const val KEY_PENDING = "flashcard_pending_reviews"
private const val HTTP_SERVER_ERROR = 500

private val MarksSerializer = MapSerializer(String.serializer(), FlashcardMark.serializer())
private val PendingSerializer = ListSerializer(SubmitReviewRequest.serializer())

/** Worth sending again later: no connection, a server hiccup, or a token being refreshed. */
private val AppError.isWorthRetrying: Boolean
    get() =
        isConnectivity ||
            this == AppError.Unauthorized ||
            (this is AppError.Server && status >= HTTP_SERVER_ERROR)

/**
 * Marks live in [VocabularyLocalStore], one value per set. Reviews for a signed-in learner go
 * through a queue on the device, in order; each has its own `clientEventId`, so sending one the
 * server already took is harmless. A review the server rejects for good (a 4xx: the word is gone)
 * is dropped so it never blocks the queue.
 */
internal class FlashcardMarkRepositoryImpl
    @Inject
    constructor(
        private val api: VocabularyApiService,
        private val store: VocabularyLocalStore,
        private val sessionRepository: SessionRepository,
        private val json: Json,
    ) : FlashcardMarkRepository {
        private val mutex = Mutex()

        override suspend fun getMarks(setId: String): Map<String, FlashcardMark> =
            store.read(KEY_MARKS + setId, MarksSerializer).orEmpty()

        override suspend fun mark(
            setId: String,
            wordId: String,
            mark: FlashcardMark,
        ) {
            mutex.withLock {
                val marks = getMarks(setId) + (wordId to mark)
                store.write(KEY_MARKS + setId, MarksSerializer, marks)
                if (!isSignedIn()) return@withLock
                val review =
                    SubmitReviewRequest(
                        wordId = wordId,
                        mode = MODE_FLASHCARD,
                        grade = if (mark == FlashcardMark.KNOWN) GRADE_GOOD else GRADE_FORGET,
                        clientEventId = UUID.randomUUID().toString(),
                        tzOffset = currentTzOffsetMinutes(),
                    )
                store.write(KEY_PENDING, PendingSerializer, readPending() + review)
            }
            flushPending()
        }

        override suspend fun flushPending() {
            mutex.withLock {
                if (!isSignedIn()) return@withLock
                var pending = readPending()
                while (pending.isNotEmpty()) {
                    val result = safeApiCall(json) { api.submitReview(pending.first()) }
                    if (result is AppResult.Failure && result.error.isWorthRetrying) break
                    pending = pending.drop(1)
                    store.write(KEY_PENDING, PendingSerializer, pending)
                }
            }
        }

        private suspend fun readPending(): List<SubmitReviewRequest> =
            store.read(KEY_PENDING, PendingSerializer).orEmpty()

        private suspend fun isSignedIn(): Boolean =
            sessionRepository.session
                .filterIsInstance<SessionState.Ready>()
                .first()
                .isSignedIn
    }
