package com.toeicspace.android.feature.vocabulary.data.repository

import com.toeicspace.android.core.common.AppError
import com.toeicspace.android.core.common.AppResult
import com.toeicspace.android.core.common.map
import com.toeicspace.android.core.di.ApplicationScope
import com.toeicspace.android.core.network.NetworkMonitor
import com.toeicspace.android.core.network.safeApiCall
import com.toeicspace.android.feature.vocabulary.data.local.PendingAnswer
import com.toeicspace.android.feature.vocabulary.data.local.ReviewSnapshot
import com.toeicspace.android.feature.vocabulary.data.local.VocabularyLocalStore
import com.toeicspace.android.feature.vocabulary.data.local.toDomain
import com.toeicspace.android.feature.vocabulary.data.remote.LearnerSummaryDto
import com.toeicspace.android.feature.vocabulary.data.remote.MODE_QUIZ
import com.toeicspace.android.feature.vocabulary.data.remote.QuizOptionDto
import com.toeicspace.android.feature.vocabulary.data.remote.QuizQuestionDto
import com.toeicspace.android.feature.vocabulary.data.remote.QuizRequest
import com.toeicspace.android.feature.vocabulary.data.remote.ReviewApiService
import com.toeicspace.android.feature.vocabulary.data.remote.SessionAnswerRequest
import com.toeicspace.android.feature.vocabulary.data.remote.StartSessionRequest
import com.toeicspace.android.feature.vocabulary.data.remote.StudySessionDto
import com.toeicspace.android.feature.vocabulary.data.remote.apiName
import com.toeicspace.android.feature.vocabulary.data.remote.toDomain
import com.toeicspace.android.feature.vocabulary.data.remote.toReviewSource
import com.toeicspace.android.feature.vocabulary.domain.Fetched
import com.toeicspace.android.feature.vocabulary.domain.ReviewGrade
import com.toeicspace.android.feature.vocabulary.domain.ReviewRepository
import com.toeicspace.android.feature.vocabulary.domain.ReviewResult
import com.toeicspace.android.feature.vocabulary.domain.ReviewSession
import com.toeicspace.android.feature.vocabulary.domain.ReviewSource
import com.toeicspace.android.feature.vocabulary.domain.ReviewSummary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject

private const val HTTP_CONFLICT = 409
private const val HTTP_NOT_FOUND = 404
private const val HTTP_SERVER_ERROR = 500

/** Answers that may succeed later stay queued; any other refusal is final for that answer. */
private val AppError.isWorthRetrying: Boolean
    get() =
        isConnectivity ||
            this == AppError.Unauthorized ||
            (this is AppError.Server && status >= HTTP_SERVER_ERROR)

/**
 * Keeps one open review session and the unsent answers on the device ([VocabularyLocalStore]).
 * Answers go out strictly in order, because the server only accepts an answer for its current
 * card; each carries a `clientEventId`, so resending one the server already took is harmless.
 * Whenever the device comes back online the queue is sent, wherever the learner is in the app.
 */
internal class ReviewRepositoryImpl
    @Inject
    constructor(
        private val api: ReviewApiService,
        private val store: VocabularyLocalStore,
        private val json: Json,
        networkMonitor: NetworkMonitor,
        @ApplicationScope scope: CoroutineScope,
    ) : ReviewRepository {
        private val mutex = Mutex()
        private val pendingCount = MutableStateFlow(0)
        override val pendingAnswerCount: StateFlow<Int> = pendingCount.asStateFlow()

        init {
            scope.launch {
                pendingCount.value = readPending().size
                networkMonitor.isOnline.filter { it }.collect { flushPendingAnswers() }
            }
        }

        override suspend fun getSummary(): AppResult<Fetched<ReviewSummary>> =
            when (val result = safeApiCall(json) { api.getSummary(currentTzOffsetMinutes()) }) {
                is AppResult.Success -> {
                    store.write(KEY_SUMMARY, LearnerSummaryDto.serializer(), result.data)
                    AppResult.Success(Fetched(result.data.toDomain()))
                }
                is AppResult.Failure -> {
                    val saved =
                        if (result.error.isConnectivity) {
                            store.read(
                                KEY_SUMMARY,
                                LearnerSummaryDto.serializer(),
                            )
                        } else {
                            null
                        }
                    if (saved !=
                        null
                    ) {
                        AppResult.Success(Fetched(saved.toDomain(), fromCache = true))
                    } else {
                        result
                    }
                }
            }

        override suspend fun startSession(
            source: ReviewSource,
            wordIds: List<String>,
        ): AppResult<ReviewSession> {
            val saved =
                readSnapshot()?.takeIf {
                    source != ReviewSource.WORDS &&
                        it.source == source.name
                }
            if (saved != null) {
                // The server only knows this session up to the answers it has received.
                val flush = flushPendingAnswers()
                if (flush is AppResult.Failure && flush.error.isWorthRetrying) {
                    return AppResult.Success(saved.toDomain())
                }
            }
            val request =
                StartSessionRequest(
                    mode = MODE_QUIZ,
                    tzOffset = currentTzOffsetMinutes(),
                    due = true.takeIf { source == ReviewSource.DUE },
                    limit = DUE_LIMIT.takeIf { source == ReviewSource.DUE },
                    mistakes = true.takeIf { source == ReviewSource.MISTAKES },
                    wordIds =
                        wordIds
                            .take(
                                MAX_SESSION_WORDS,
                            ).takeIf { source == ReviewSource.WORDS },
                )
            return when (val started = safeApiCall(json) { api.startSession(request) }) {
                is AppResult.Success ->
                    snapshotOf(started.data, previous = readSnapshot()).map { snapshot ->
                        store.write(KEY_SNAPSHOT, ReviewSnapshot.serializer(), snapshot)
                        snapshot.toDomain()
                    }
                is AppResult.Failure ->
                    // Offline with a session left on the device for this source: continue it.
                    readSnapshot()
                        ?.takeIf {
                            started.error.isConnectivity &&
                                it.source == source.name &&
                                source != ReviewSource.WORDS
                        }?.let { AppResult.Success(it.toDomain()) }
                        ?: started
            }
        }

        override suspend fun answer(
            session: ReviewSession,
            wordId: String,
            grade: ReviewGrade,
        ): ReviewSession {
            val next = session.answered(wordId, grade)
            if (next === session) return session
            mutex.withLock {
                val pending =
                    readPending() +
                        PendingAnswer(
                            sessionId = session.id,
                            wordId = wordId,
                            grade = grade.apiName,
                            clientEventId = UUID.randomUUID().toString(),
                            tzOffset = currentTzOffsetMinutes(),
                        )
                store.write(KEY_PENDING, PendingSerializer, pending)
                pendingCount.value = pending.size
                readSnapshot()?.takeIf { it.sessionId == session.id }?.let { saved ->
                    store.write(KEY_SNAPSHOT, ReviewSnapshot.serializer(), saved.advancedTo(next))
                }
            }
            flushPendingAnswers()
            // A refused answer makes the device re-read the session; show what the server has.
            return readSnapshot()?.takeIf { it.sessionId == session.id }?.toDomain() ?: next
        }

        override suspend fun flushPendingAnswers(): AppResult<Unit> {
            val resync = mutableSetOf<String>()
            val outcome =
                mutex.withLock {
                    var pending = readPending()
                    while (pending.isNotEmpty()) {
                        val answer = pending.first()
                        val result =
                            safeApiCall(json) {
                                api.answer(
                                    answer.sessionId,
                                    SessionAnswerRequest(
                                        wordId = answer.wordId,
                                        grade = answer.grade,
                                        clientEventId = answer.clientEventId,
                                        tzOffset = answer.tzOffset,
                                    ),
                                )
                            }
                        if (result is AppResult.Failure) {
                            if (result.error.isWorthRetrying) {
                                return@withLock AppResult.Failure(
                                    result.error,
                                )
                            }
                            // Not the current card, session closed or gone: the device was out of step.
                            resync += answer.sessionId
                        }
                        pending = pending.drop(1)
                        store.write(KEY_PENDING, PendingSerializer, pending)
                        pendingCount.value = pending.size
                    }
                    AppResult.Success(Unit)
                }
            resync.forEach { resyncSnapshot(it) }
            return outcome
        }

        override suspend fun completeSession(session: ReviewSession): AppResult<ReviewResult> {
            val flush = flushPendingAnswers()
            if (flush is AppResult.Failure) return flush
            return when (
                val result =
                    safeApiCall(json) {
                        api.complete(session.id, currentTzOffsetMinutes())
                    }
            ) {
                is AppResult.Success -> {
                    clearSnapshot(session.id)
                    AppResult.Success(result.data.summary?.toDomain() ?: session.localResult)
                }
                is AppResult.Failure -> {
                    val status = (result.error as? AppError.Server)?.status
                    if (status == HTTP_CONFLICT || status == HTTP_NOT_FOUND) {
                        // Expired or already closed elsewhere: the grades are recorded anyway.
                        clearSnapshot(session.id)
                        AppResult.Success(session.localResult)
                    } else {
                        result
                    }
                }
            }
        }

        private suspend fun resyncSnapshot(sessionId: String) {
            val saved = readSnapshot()?.takeIf { it.sessionId == sessionId } ?: return
            val result = safeApiCall(json) { api.getSession(sessionId, currentTzOffsetMinutes()) }
            if (result !is AppResult.Success) return
            if (!result.data.status.equals(STATUS_ACTIVE, ignoreCase = true)) {
                clearSnapshot(sessionId)
                return
            }
            val fresh = snapshotOf(result.data, previous = saved)
            if (fresh is AppResult.Success) {
                store.write(
                    KEY_SNAPSHOT,
                    ReviewSnapshot.serializer(),
                    fresh.data,
                )
            }
        }

        /** Builds the device copy of [session], reusing the questions [previous] already has. */
        private suspend fun snapshotOf(
            session: StudySessionDto,
            previous: ReviewSnapshot?,
        ): AppResult<ReviewSnapshot> {
            val words = session.items.map { it.word }.distinctBy { it.id }
            val known =
                previous
                    ?.takeIf {
                        it.sessionId == session.id
                    }?.questions
                    .orEmpty()
                    .associateBy { it.wordId }
            val missing = words.map { it.id }.filterNot(known::containsKey)
            val fetched = mutableListOf<QuizQuestionDto>()
            for (chunk in missing.chunked(QUIZ_CHUNK)) {
                when (val quiz = safeApiCall(json) { api.createQuiz(QuizRequest(chunk)) }) {
                    is AppResult.Success -> fetched += quiz.data
                    is AppResult.Failure -> return quiz
                }
            }
            val questions = known + fetched.associateBy { it.wordId }
            return AppResult.Success(
                ReviewSnapshot(
                    sessionId = session.id,
                    source = session.source.toReviewSource().name,
                    queue = session.items.map { it.word.id },
                    position = session.position,
                    results = session.results.associate { it.wordId to it.grade },
                    correctCount = session.correctCount,
                    reviewCount = session.reviewCount,
                    words = words,
                    // A word the quiz builder skipped still gets a question: just its own headword.
                    questions =
                        words.map { word ->
                            questions[word.id] ?: QuizQuestionDto(
                                wordId = word.id,
                                prompt = word.meaningVi,
                                partOfSpeech = word.partOfSpeech,
                                options = listOf(QuizOptionDto(word.id, word.headword)),
                            )
                        },
                ),
            )
        }

        private fun ReviewSnapshot.advancedTo(session: ReviewSession) =
            copy(
                queue = session.queue,
                position = session.position,
                results = session.results.mapValues { (_, grade) -> grade.apiName },
                correctCount = session.correctCount,
                reviewCount = session.reviewCount,
            )

        private suspend fun readPending(): List<PendingAnswer> =
            store.read(KEY_PENDING, PendingSerializer).orEmpty()

        private suspend fun readSnapshot(): ReviewSnapshot? =
            store.read(KEY_SNAPSHOT, ReviewSnapshot.serializer())

        private suspend fun clearSnapshot(sessionId: String) {
            if (readSnapshot()?.sessionId == sessionId) store.delete(KEY_SNAPSHOT)
        }

        private companion object {
            const val KEY_SUMMARY = "review_summary"
            const val KEY_PENDING = "review_pending_answers"
            const val KEY_SNAPSHOT = "review_open_session"
            const val STATUS_ACTIVE = "Active"
            const val DUE_LIMIT = 20
            const val MAX_SESSION_WORDS = 100
            const val QUIZ_CHUNK = 50
            val PendingSerializer = ListSerializer(PendingAnswer.serializer())
        }
    }
