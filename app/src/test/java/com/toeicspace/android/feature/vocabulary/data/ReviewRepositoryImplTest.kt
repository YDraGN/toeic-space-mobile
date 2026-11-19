package com.toeicspace.android.feature.vocabulary.data

import com.toeicspace.android.core.common.AppError
import com.toeicspace.android.core.common.AppResult
import com.toeicspace.android.feature.vocabulary.data.remote.ReviewApiService
import com.toeicspace.android.feature.vocabulary.data.repository.ReviewRepositoryImpl
import com.toeicspace.android.feature.vocabulary.domain.ReviewGrade
import com.toeicspace.android.feature.vocabulary.domain.ReviewSession
import com.toeicspace.android.feature.vocabulary.domain.ReviewSource
import com.toeicspace.android.testing.FakeNetworkMonitor
import com.toeicspace.android.testing.InMemoryVocabularyLocalStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.MediaType.Companion.toMediaType
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class ReviewRepositoryImplTest {
    private val json =
        Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }
    private val server = MockWebServer()
    private val store = InMemoryVocabularyLocalStore(json)
    private lateinit var api: ReviewApiService

    @Before
    fun setUp() {
        server.start()
        api =
            Retrofit
                .Builder()
                .baseUrl(server.url("/"))
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .build()
                .create(ReviewApiService::class.java)
    }

    @After
    fun tearDown() = server.close()

    private fun TestScope.repository(scope: CoroutineScope = backgroundScope) =
        ReviewRepositoryImpl(api, store, json, FakeNetworkMonitor(isOnline = false), scope)

    private fun respond(
        body: String,
        code: Int = 200,
    ) = server.enqueue(
        MockResponse
            .Builder()
            .code(code)
            .addHeader("Content-Type", "application/json")
            .body(body)
            .build(),
    )

    private fun bodyOf(request: mockwebserver3.RecordedRequest) =
        json.parseToJsonElement(request.body!!.utf8()).jsonObject

    private suspend fun ReviewRepositoryImpl.started(): ReviewSession {
        respond(SESSION_JSON)
        respond(QUIZ_JSON)
        return (startSession(ReviewSource.DUE) as AppResult.Success).data
    }

    @Test
    fun `starting a due review sends the quiz mode and builds questions for its words`() =
        runTest {
            val session = repository().started()

            val start = server.takeRequest()
            assertEquals("/content/api/v1/vocabulary/me/sessions", start.url.encodedPath)
            val body = bodyOf(start)
            assertEquals("Quiz", body.getValue("mode").jsonPrimitive.content)
            assertTrue(body.getValue("due").jsonPrimitive.boolean)
            assertEquals(20, body.getValue("limit").jsonPrimitive.int)
            assertTrue("tzOffset" in body)
            assertFalse("mistakes" in body)

            val quiz = server.takeRequest()
            assertEquals("/content/api/v1/vocabulary/quizzes", quiz.url.encodedPath)
            assertEquals(
                listOf("w1", "w2"),
                bodyOf(quiz).getValue("wordIds").jsonArray.map {
                    it.jsonPrimitive.content
                },
            )

            assertEquals(listOf("w1", "w2"), session.queue)
            assertEquals("w1", session.currentWordId)
            assertEquals(
                3,
                session.questions
                    .getValue("w1")
                    .options.size,
            )
            assertEquals("agenda", session.words.getValue("w1").headword)
        }

    @Test
    fun `an answer is sent with its grade and a client event id`() =
        runTest {
            val repository = repository()
            val session = repository.started()
            server.takeRequest()
            server.takeRequest()
            respond(SESSION_JSON)

            val next = repository.answer(session, "w1", ReviewGrade.GOOD)

            val answer = server.takeRequest()
            assertEquals(
                "/content/api/v1/vocabulary/me/sessions/r1/answers",
                answer.url.encodedPath,
            )
            val body = bodyOf(answer)
            assertEquals("w1", body.getValue("wordId").jsonPrimitive.content)
            assertEquals("Good", body.getValue("grade").jsonPrimitive.content)
            assertTrue(
                body
                    .getValue("clientEventId")
                    .jsonPrimitive.content
                    .isNotBlank(),
            )
            assertEquals(1, next.position)
            assertEquals(0, repository.pendingAnswerCount.value)
        }

    @Test
    fun `answers given while the server is down wait and go out in order later`() =
        runTest {
            val repository = repository()
            var session = repository.started()
            server.takeRequest()
            server.takeRequest()
            // Each answer tries to send the queue once; both tries fail on the first answer.
            respond("""{"status":503}""", code = 503)
            respond("""{"status":503}""", code = 503)

            session = repository.answer(session, "w1", ReviewGrade.FORGET)
            session = repository.answer(session, "w2", ReviewGrade.GOOD)

            assertEquals(2, repository.pendingAnswerCount.value)
            assertEquals(listOf("w1", "w2", "w1"), session.queue)
            assertEquals(2, session.position)

            server.takeRequest()
            server.takeRequest()
            respond(SESSION_JSON)
            respond(SESSION_JSON)
            val flushed = repository.flushPendingAnswers()

            assertTrue(flushed is AppResult.Success)
            assertEquals(0, repository.pendingAnswerCount.value)
            val first = bodyOf(server.takeRequest())
            val second = bodyOf(server.takeRequest())
            assertEquals("w1", first.getValue("wordId").jsonPrimitive.content)
            assertEquals("Forget", first.getValue("grade").jsonPrimitive.content)
            assertEquals("w2", second.getValue("wordId").jsonPrimitive.content)
        }

    @Test
    fun `an unfinished session with unsent answers is resumed from the device`() =
        runTest {
            val first = repository()
            val session = first.started()
            server.takeRequest()
            server.takeRequest()
            respond("""{"status":503}""", code = 503)
            first.answer(session, "w1", ReviewGrade.GOOD)
            server.takeRequest()

            // The app was closed; the server is still down when the learner comes back.
            respond("""{"status":503}""", code = 503)
            val resumed = (repository().startSession(ReviewSource.DUE) as AppResult.Success).data

            assertEquals("r1", resumed.id)
            assertEquals(1, resumed.position)
            assertEquals(ReviewGrade.GOOD, resumed.results["w1"])
        }

    @Test
    fun `an answer the server refuses is dropped and the session is read again`() =
        runTest {
            val repository = repository()
            val session = repository.started()
            server.takeRequest()
            server.takeRequest()
            respond("""{"status":409,"code":"study_session_word_mismatch"}""", code = 409)
            respond(SESSION_JSON)

            val next = repository.answer(session, "w1", ReviewGrade.GOOD)

            assertEquals(0, repository.pendingAnswerCount.value)
            server.takeRequest()
            assertEquals(
                "/content/api/v1/vocabulary/me/sessions/r1",
                server.takeRequest().url.encodedPath,
            )
            // The server's copy wins: it never received that answer.
            assertEquals(0, next.position)
        }

    @Test
    fun `completing returns the server summary`() =
        runTest {
            val repository = repository()
            val session = repository.started()
            server.takeRequest()
            server.takeRequest()
            respond(
                SESSION_JSON.replace(
                    "\"summary\":null",
                    "\"summary\":{\"remembered\":1,\"needReview\":1,\"wordIds\":[\"w2\"]}",
                ),
            )

            val result = repository.completeSession(session)

            assertEquals(
                "/content/api/v1/vocabulary/me/sessions/r1/complete",
                server.takeRequest().url.encodedPath,
            )
            val summary = (result as AppResult.Success).data
            assertEquals(listOf("w2"), summary.wordIds)
            assertEquals(1, summary.remembered)
        }

    @Test
    fun `completing while answers cannot be sent fails without closing the session`() =
        runTest {
            val repository = repository()
            val session = repository.started()
            server.takeRequest()
            server.takeRequest()
            respond("""{"status":503}""", code = 503)
            val answered = repository.answer(session, "w1", ReviewGrade.GOOD)
            server.takeRequest()
            respond("""{"status":503}""", code = 503)

            val result = repository.completeSession(answered)

            assertEquals(503, ((result as AppResult.Failure).error as AppError.Server).status)
            assertEquals(1, repository.pendingAnswerCount.value)
        }

    @Test
    fun `nothing to review is reported as an empty session`() =
        runTest {
            respond("""{"status":400,"code":"study_session_empty"}""", code = 400)

            val result = repository().startSession(ReviewSource.MISTAKES)

            val body = bodyOf(server.takeRequest())
            assertTrue(body.getValue("mistakes").jsonPrimitive.boolean)
            assertNull(body["due"])
            assertEquals(
                "STUDY_SESSION_EMPTY",
                ((result as AppResult.Failure).error as AppError.Server).code,
            )
        }

    @Test
    fun `the summary keeps an open due quiz and falls back to the saved copy`() =
        runTest {
            val repository = repository()
            respond(SUMMARY_JSON)
            val fresh = (repository.getSummary() as AppResult.Success).data
            assertEquals(12, fresh.data.due)
            assertNotNull(fresh.data.activeReview)
            assertEquals(3, fresh.data.activeReview?.position)

            server.close()
            val saved = (repository.getSummary() as AppResult.Success).data
            assertTrue(saved.fromCache)
            assertEquals(12, saved.data.due)
        }

    private companion object {
        val SESSION_JSON =
            """
            {"id":"r1","mode":"Quiz","source":{"kind":"Due","id":null,"title":null},"position":0,"total":2,
             "correctCount":0,"reviewCount":0,"status":"Active","startedAt":"2026-11-18T01:00:00Z",
             "updatedAt":"2026-11-18T01:00:00Z","completedAt":null,
             "items":[
               {"word":{"id":"w1","headword":"agenda","phonetic":"/əˈdʒendə/","partOfSpeech":"Noun","meaningVi":"chương trình nghị sự","synonyms":[]},
                "progress":{"status":"Learning","intervalDays":1,"repetitions":1,"dueOn":"2026-11-18","isDue":true},
                "preview":{"forget":1,"hard":1,"good":6,"easy":6},"savedWordId":null},
               {"word":{"id":"w2","headword":"venue","phonetic":"","partOfSpeech":"Noun","meaningVi":"địa điểm","synonyms":[]},
                "progress":{"status":"Learning","intervalDays":1,"repetitions":1,"dueOn":"2026-11-18","isDue":true},
                "preview":{"forget":1,"hard":1,"good":6,"easy":6},"savedWordId":null}
             ],
             "results":[],"summary":null,"review":null}
            """.trimIndent()

        val QUIZ_JSON =
            """
            [
              {"wordId":"w1","prompt":"chương trình nghị sự","partOfSpeech":"Noun",
               "options":[{"wordId":"w9","headword":"budget"},{"wordId":"w1","headword":"agenda"},{"wordId":"w8","headword":"memo"}]},
              {"wordId":"w2","prompt":"địa điểm","partOfSpeech":"Noun",
               "options":[{"wordId":"w2","headword":"venue"},{"wordId":"w7","headword":"vendor"}]}
            ]
            """.trimIndent()

        val SUMMARY_JSON =
            """
            {"learned":120,"learning":80,"mastered":40,"due":12,"streakDays":4,"reviewedToday":5,"lessonCount":100,
             "lessonsCompleted":2,"libraryWordCount":0,"nextSet":null,"stages":[],"dailyGoal":20,"mistakes":3,
             "activeSession":{"id":"r1","mode":"Quiz","source":{"kind":"Due","id":null,"title":null},"position":3,"total":12}}
            """.trimIndent()
    }
}
