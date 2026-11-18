package com.toeicspace.android.feature.enrollment.data

import com.toeicspace.android.core.common.AppError
import com.toeicspace.android.core.common.AppResult
import com.toeicspace.android.feature.enrollment.data.remote.EnrollmentApiService
import com.toeicspace.android.feature.enrollment.data.repository.EnrollmentRepositoryImpl
import com.toeicspace.android.feature.enrollment.domain.EnrollmentRepository
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.MediaType.Companion.toMediaType
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.time.Instant

class EnrollmentRepositoryImplTest {
    private val json =
        Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }
    private val server = MockWebServer()
    private lateinit var repository: EnrollmentRepositoryImpl

    @Before
    fun setUp() {
        server.start()
        val api =
            Retrofit
                .Builder()
                .baseUrl(server.url("/"))
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .build()
                .create(EnrollmentApiService::class.java)
        repository = EnrollmentRepositoryImpl(api, json)
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
    fun `enroll posts to the course and reads the enrollment`() =
        runTest {
            respond(ENROLLMENT_JSON)

            val result = repository.enroll("c1")

            val request = server.takeRequest()
            assertEquals("POST", request.method)
            assertEquals("/content/api/v1/enrollments/c1", request.url.encodedPath)
            val enrollment = (result as AppResult.Success).data
            assertEquals("c1", enrollment.courseId)
            assertEquals(Instant.parse("2026-11-10T08:00:00Z"), enrollment.enrolledAt)
        }

    @Test
    fun `a full course fails with its error code`() =
        runTest {
            respond("""{"status":409,"code":"course_full"}""", code = 409)

            val error = (repository.enroll("c1") as AppResult.Failure).error

            assertEquals(
                AppError.Server(code = EnrollmentRepository.ERROR_COURSE_FULL, status = 409),
                error,
            )
        }

    @Test
    fun `status reads an enrollment`() =
        runTest {
            respond(ENROLLMENT_JSON)

            val result = repository.getEnrollment("c1")

            assertEquals("/content/api/v1/enrollments/c1", server.takeRequest().url.encodedPath)
            assertEquals("c1", (result as AppResult.Success).data?.courseId)
        }

    @Test
    fun `no content means not enrolled`() =
        runTest {
            server.enqueue(MockResponse.Builder().code(204).build())

            val result = repository.getEnrollment("c1")

            assertNull((result as AppResult.Success).data)
        }

    @Test
    fun `status errors become failures`() =
        runTest {
            respond("""{"status":401}""", code = 401)

            val result = repository.getEnrollment("c1")

            assertEquals(
                AppError.Server(code = null, status = 401),
                (result as AppResult.Failure).error,
            )
        }

    @Test
    fun `my courses sends search and paging, and reads the page`() =
        runTest {
            respond(MY_COURSES_JSON)

            val result = repository.getMyCourses(search = "  toeic ", page = 2, pageSize = 10)

            val url = server.takeRequest().url
            assertEquals("/content/api/v1/enrollments/me", url.encodedPath)
            assertEquals("toeic", url.queryParameter("search"))
            assertEquals("2", url.queryParameter("page"))
            assertEquals("10", url.queryParameter("pageSize"))
            val page = (result as AppResult.Success).data
            assertEquals(2, page.items.size)
            assertEquals("https://cdn/thumb.png?sig=1", page.items[0].thumbnailUrl)
            assertNull(page.items[1].thumbnailUrl)
            assertNull(page.items[1].shortDescription)
            assertEquals(Instant.parse("2026-11-09T01:02:03Z"), page.items[1].enrolledAt)
            assertTrue(page.hasNextPage)
        }

    @Test
    fun `blank search is left out and page size is capped`() =
        runTest {
            respond("""{"items":[],"page":1,"pageSize":100,"totalCount":0}""")

            val result = repository.getMyCourses(search = " ", page = 1, pageSize = 500)

            val url = server.takeRequest().url
            assertNull(url.queryParameter("search"))
            assertEquals("100", url.queryParameter("pageSize"))
            assertFalse((result as AppResult.Success).data.hasNextPage)
        }

    private companion object {
        const val ENROLLMENT_JSON =
            """{"id":"e1","userId":"u1","courseId":"c1","status":"Active",
               "enrolledAt":"2026-11-10T08:00:00Z"}"""

        // The second item's date has no zone, as EF reads DateTime back: it is UTC.
        const val MY_COURSES_JSON =
            """{"items":[
                {"courseId":"c1","title":"TOEIC 650+","slug":"toeic-650","shortDescription":"Lộ trình",
                 "enrollmentStatus":"Active","enrolledAt":"2026-11-10T08:00:00Z",
                 "thumbnailUrl":"https://cdn/thumb.png?sig=1"},
                {"courseId":"c2","title":"Ngữ pháp","slug":"ngu-phap","shortDescription":" ",
                 "enrollmentStatus":"Active","enrolledAt":"2026-11-09T01:02:03"}
               ],"page":2,"pageSize":2,"totalCount":5}"""
    }
}
