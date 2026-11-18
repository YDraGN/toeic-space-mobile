package com.toeicspace.android.feature.lesson.data

import com.toeicspace.android.core.common.AppError
import com.toeicspace.android.core.common.AppResult
import com.toeicspace.android.feature.lesson.data.remote.LessonApiService
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.MediaType.Companion.toMediaType
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class LessonRepositoryImplTest {
    private val json =
        Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }
    private val server = MockWebServer()
    private lateinit var repository: LessonRepositoryImpl

    @Before
    fun setUp() {
        server.start()
        val api =
            Retrofit
                .Builder()
                .baseUrl(server.url("/"))
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .build()
                .create(LessonApiService::class.java)
        repository = LessonRepositoryImpl(api, json)
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
    fun `an enrolled lesson comes from the enrollment endpoint`() =
        runTest {
            respond(LESSON_JSON)

            val lesson =
                (
                    repository.getLesson(
                        "c1",
                        "l1",
                        preview = false,
                    ) as AppResult.Success
                ).data

            assertEquals(
                "/content/api/v1/enrollments/c1/lessons/l1",
                server.takeRequest().url.encodedPath,
            )
            assertEquals("Câu hỏi WH-", lesson.title)
            assertEquals("## Mục tiêu", lesson.textContent)
            // Sorted by sortOrder, not by the order they arrive in.
            assertEquals(listOf("v1", "v2"), lesson.videos.map { it.id })
            assertEquals("https://cdn/v1?sig", lesson.videos.first().url)
            assertEquals("a.pdf", lesson.attachments.single().fileName)
            assertEquals(2048L, lesson.attachments.single().fileSize)
        }

    @Test
    fun `a preview comes from the public endpoint`() =
        runTest {
            respond("""{"id":"l1","title":"T","textContent":"  ","videos":[],"attachments":[]}""")

            val lesson =
                (
                    repository.getLesson(
                        "c1",
                        "l1",
                        preview = true,
                    ) as AppResult.Success
                ).data

            assertEquals(
                "/content/api/v1/public/courses/c1/lessons/l1/preview",
                server.takeRequest().url.encodedPath,
            )
            assertNull(lesson.textContent)
        }

    @Test
    fun `not enrolled is a 404`() =
        runTest {
            respond("""{"status":404,"code":"not_found"}""", code = 404)

            val error =
                (
                    repository.getLesson(
                        "c1",
                        "l1",
                        preview = false,
                    ) as AppResult.Failure
                ).error

            assertEquals(AppError.Server(code = "NOT_FOUND", status = 404), error)
        }

    private companion object {
        const val LESSON_JSON =
            """{"id":"l1","title":"Câu hỏi WH-","textContent":"## Mục tiêu",
               "videos":[
                 {"id":"v2","fileName":"b.mp4","contentType":"video/mp4","fileSize":10,"durationSeconds":null,
                  "sortOrder":2,"url":"https://cdn/v2?sig"},
                 {"id":"v1","fileName":"a.mp4","contentType":"video/mp4","fileSize":10,"durationSeconds":60,
                  "sortOrder":1,"url":"https://cdn/v1?sig"}],
               "attachments":[{"id":"a1","fileName":"a.pdf","contentType":"application/pdf","fileSize":2048,
                  "url":"https://cdn/a1?sig"}]}"""
    }
}
