package com.toeicspace.android.feature.vocabulary.data

import com.toeicspace.android.core.common.AppError
import com.toeicspace.android.core.common.AppResult
import com.toeicspace.android.feature.vocabulary.data.remote.VocabularyApiService
import com.toeicspace.android.feature.vocabulary.data.repository.VocabularyRepositoryImpl
import com.toeicspace.android.feature.vocabulary.domain.PartOfSpeech
import com.toeicspace.android.testing.InMemoryVocabularyLocalStore
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
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

class VocabularyRepositoryImplTest {
    private val json =
        Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }
    private val server = MockWebServer()
    private val store = InMemoryVocabularyLocalStore(json)
    private lateinit var repository: VocabularyRepositoryImpl

    @Before
    fun setUp() {
        server.start()
        val api =
            Retrofit
                .Builder()
                .baseUrl(server.url("/"))
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .build()
                .create(VocabularyApiService::class.java)
        repository = VocabularyRepositoryImpl(api, store, json)
    }

    @After
    fun tearDown() = server.close()

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

    @Test
    fun `stages are read from the catalog in roadmap order`() =
        runTest {
            respond(STAGES_JSON)

            val result = repository.getStages()

            assertEquals("/content/api/v1/vocabulary/stages", server.takeRequest().url.encodedPath)
            val fetched = (result as AppResult.Success).data
            assertFalse(fetched.fromCache)
            assertEquals(listOf("Khởi động", "Phản xạ"), fetched.data.map { it.title })
            assertEquals(250, fetched.data.first().toeicTarget)
            assertNull(fetched.data.last().description)
        }

    @Test
    fun `stages fall back to the saved copy when the server cannot be reached`() =
        runTest {
            respond(STAGES_JSON)
            repository.getStages()
            server.close()

            val result = repository.getStages()

            val fetched = (result as AppResult.Success).data
            assertTrue(fetched.fromCache)
            assertEquals(2, fetched.data.size)
        }

    @Test
    fun `without a saved copy a connection failure is passed on`() =
        runTest {
            server.close()

            val result = repository.getStages()

            assertEquals(AppError.ServerUnreachable, (result as AppResult.Failure).error)
        }

    @Test
    fun `stage sets keep word counts and the first and last words`() =
        runTest {
            respond(SETS_JSON)

            val result = repository.getStageSets(STAGE_ID)

            assertEquals(
                "/content/api/v1/vocabulary/stages/$STAGE_ID/sets",
                server.takeRequest().url.encodedPath,
            )
            val sets = (result as AppResult.Success).data.data
            assertEquals(listOf("Bài 01", "Bài 02"), sets.map { it.title })
            assertEquals(50, sets.first().wordCount)
            assertEquals(listOf("agenda", "attend", "budget"), sets.first().previewWords)
            assertEquals("venue", sets.first().lastWord)
        }

    @Test
    fun `set detail maps words and unknown parts of speech`() =
        runTest {
            respond(SET_JSON)

            val result = repository.getSet(SET_ID)

            assertEquals(
                "/content/api/v1/vocabulary/sets/$SET_ID",
                server.takeRequest().url.encodedPath,
            )
            val set = (result as AppResult.Success).data.data
            assertEquals("Khởi động", set.stageTitle)
            assertEquals("set-2", set.nextSetId)
            assertEquals(PartOfSpeech.NOUN, set.words[0].partOfSpeech)
            assertEquals("/əˈdʒendə/", set.words[0].phonetic)
            assertEquals(PartOfSpeech.OTHER, set.words[1].partOfSpeech)
            assertNull(set.words[1].phonetic)
            assertNull(set.words[1].example)
        }

    @Test
    fun `a removed set is reported even when a copy was saved`() =
        runTest {
            respond(SET_JSON)
            repository.getSet(SET_ID)
            respond("""{"status":404,"code":"vocabulary_set_not_found"}""", code = 404)

            val result = repository.getSet(SET_ID)

            val error = (result as AppResult.Failure).error as AppError.Server
            assertEquals(404, error.status)
            assertEquals("VOCABULARY_SET_NOT_FOUND", error.code)
        }

    @Test
    fun `set detail is available offline after one visit`() =
        runTest {
            respond(SET_JSON)
            repository.getSet(SET_ID)
            server.close()

            val result = repository.getSet(SET_ID)

            val fetched = (result as AppResult.Success).data
            assertTrue(fetched.fromCache)
            assertEquals(2, fetched.data.words.size)
        }

    @Test
    fun `progress is keyed by set and sends the time zone`() =
        runTest {
            respond("""[{"setId":"set-1","wordCount":50,"learning":12,"mastered":30,"due":4}]""")

            val result = repository.getStageProgress(STAGE_ID)

            val request = server.takeRequest()
            assertEquals(
                "/content/api/v1/vocabulary/me/stages/$STAGE_ID/sets",
                request.url.encodedPath,
            )
            assertNotNull(request.url.queryParameter("tzOffset"))
            val progress = (result as AppResult.Success).data.getValue("set-1")
            assertEquals(42, progress.learned)
            assertEquals(4, progress.due)
        }

    private companion object {
        const val STAGE_ID = "6f1c2b1e-0000-0000-0000-000000000001"
        const val SET_ID = "set-1"

        val STAGES_JSON =
            """
            [
              {"id":"st2","slug":"phan-xa","title":"Phản xạ","description":"","order":2,"toeicTarget":450,"setCount":16,"wordCount":800},
              {"id":"st1","slug":"khoi-dong","title":"Khởi động","description":"Nền tảng","order":1,"toeicTarget":250,"setCount":18,"wordCount":900}
            ]
            """.trimIndent()

        val SETS_JSON =
            """
            [
              {"id":"set-2","stageId":"st1","slug":"bai-02","title":"Bài 02","order":2,"wordCount":50,"previewWords":["deal"],"lastWord":"yield"},
              {"id":"set-1","stageId":"st1","slug":"bai-01","title":"Bài 01","order":1,"wordCount":50,"previewWords":["agenda","attend","budget"],"lastWord":"venue"}
            ]
            """.trimIndent()

        val SET_JSON =
            """
            {"id":"set-1","stageId":"st1","stageSlug":"khoi-dong","stageTitle":"Khởi động","slug":"bai-01",
             "title":"Bài 01","order":1,"previousSetId":null,"nextSetId":"set-2","words":[
              {"id":"w1","headword":"agenda","phonetic":"/əˈdʒendə/","partOfSpeech":"Noun","meaningVi":"chương trình nghị sự",
               "definitionEn":null,"example":"The agenda is ready.","exampleVi":"Chương trình đã sẵn sàng.","cefr":"B1","synonyms":[]},
              {"id":"w2","headword":"as soon as","phonetic":"","partOfSpeech":"Mystery","meaningVi":"ngay khi",
               "example":" ","synonyms":[],"personal":null}
            ]}
            """.trimIndent()
    }
}
