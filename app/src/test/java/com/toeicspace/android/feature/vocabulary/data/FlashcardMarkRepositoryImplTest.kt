package com.toeicspace.android.feature.vocabulary.data

import com.toeicspace.android.core.model.AuthUser
import com.toeicspace.android.core.model.UserRole
import com.toeicspace.android.feature.vocabulary.data.remote.VocabularyApiService
import com.toeicspace.android.feature.vocabulary.data.repository.FlashcardMarkRepositoryImpl
import com.toeicspace.android.feature.vocabulary.domain.FlashcardMark
import com.toeicspace.android.testing.FakeSessionRepository
import com.toeicspace.android.testing.InMemoryVocabularyLocalStore
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.MediaType.Companion.toMediaType
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class FlashcardMarkRepositoryImplTest {
    private val json =
        Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }
    private val server = MockWebServer()
    private val store = InMemoryVocabularyLocalStore(json)
    private lateinit var api: VocabularyApiService

    private val user =
        AuthUser(id = "u1", fullName = "Lan", email = "lan@example.com", role = UserRole.User)

    @Before
    fun setUp() {
        server.start()
        api =
            Retrofit
                .Builder()
                .baseUrl(server.url("/"))
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .build()
                .create(VocabularyApiService::class.java)
    }

    @After
    fun tearDown() = server.close()

    private fun repository(signedIn: Boolean) =
        FlashcardMarkRepositoryImpl(
            api = api,
            store = store,
            sessionRepository =
                FakeSessionRepository(
                    isOnboardingCompleted = true,
                    user = if (signedIn) user else null,
                    isGuest = !signedIn,
                ),
            json = json,
        )

    private fun respond(code: Int) =
        server.enqueue(
            MockResponse
                .Builder()
                .code(code)
                .addHeader("Content-Type", "application/json")
                .body("{}")
                .build(),
        )

    private fun pendingCount(): Int =
        store.values["flashcard_pending_reviews"]
            ?.let { json.parseToJsonElement(it) }
            ?.let { (it as JsonArray).size } ?: 0

    @Test
    fun `a guest's marks stay on the device and nothing is sent`() =
        runTest {
            val repository = repository(signedIn = false)

            repository.mark("set1", "w1", FlashcardMark.KNOWN)
            repository.mark("set1", "w2", FlashcardMark.LEARNING)

            assertEquals(
                mapOf("w1" to FlashcardMark.KNOWN, "w2" to FlashcardMark.LEARNING),
                repository.getMarks("set1"),
            )
            assertTrue(repository.getMarks("set2").isEmpty())
            assertEquals(0, server.requestCount)
            assertEquals(0, pendingCount())
        }

    @Test
    fun `a signed-in mark is sent as a flashcard review`() =
        runTest {
            respond(200)
            respond(200)
            val repository = repository(signedIn = true)

            repository.mark("set1", "w1", FlashcardMark.KNOWN)
            repository.mark("set1", "w2", FlashcardMark.LEARNING)

            val first = server.takeRequest()
            assertEquals("/content/api/v1/vocabulary/me/reviews", first.url.encodedPath)
            val body = json.parseToJsonElement(first.body!!.utf8()).jsonObject
            assertEquals("w1", body.getValue("wordId").jsonPrimitive.content)
            assertEquals("Flashcard", body.getValue("mode").jsonPrimitive.content)
            assertEquals("Good", body.getValue("grade").jsonPrimitive.content)
            assertFalse(
                body
                    .getValue("clientEventId")
                    .jsonPrimitive.content
                    .isBlank(),
            )
            body.getValue("tzOffset").jsonPrimitive.int
            val second = json.parseToJsonElement(server.takeRequest().body!!.utf8()).jsonObject
            assertEquals("Forget", second.getValue("grade").jsonPrimitive.content)
            assertNotEquals(
                body.getValue("clientEventId").jsonPrimitive.content,
                second.getValue("clientEventId").jsonPrimitive.content,
            )
            assertEquals(0, pendingCount())
        }

    @Test
    fun `a review that cannot go out waits in the queue and is sent later`() =
        runTest {
            respond(503)
            val repository = repository(signedIn = true)

            repository.mark("set1", "w1", FlashcardMark.KNOWN)
            assertEquals(1, pendingCount())
            assertEquals(FlashcardMark.KNOWN, repository.getMarks("set1")["w1"])

            respond(200)
            repository.flushPending()

            assertEquals(2, server.requestCount)
            assertEquals(0, pendingCount())
        }

    @Test
    fun `a review the server rejects for good is dropped so the queue moves on`() =
        runTest {
            respond(404)
            val repository = repository(signedIn = true)

            repository.mark("set1", "gone", FlashcardMark.LEARNING)

            assertEquals(1, server.requestCount)
            assertEquals(0, pendingCount())
        }
}
