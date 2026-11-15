package com.toeicspace.android.feature.course.data

import com.toeicspace.android.core.common.AppError
import com.toeicspace.android.core.common.AppResult
import com.toeicspace.android.feature.course.data.remote.CourseApiService
import com.toeicspace.android.feature.course.data.repository.CourseRepositoryImpl
import com.toeicspace.android.feature.course.domain.CourseExamType
import com.toeicspace.android.feature.course.domain.CourseFilter
import com.toeicspace.android.feature.course.domain.CourseLevel
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

class CourseRepositoryImplTest {
    private val json =
        Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }
    private val server = MockWebServer()
    private lateinit var repository: CourseRepositoryImpl

    @Before
    fun setUp() {
        server.start()
        val api =
            Retrofit
                .Builder()
                .baseUrl(server.url("/"))
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .build()
                .create(CourseApiService::class.java)
        repository = CourseRepositoryImpl(api, json)
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
    fun `catalog request sends only the filters that are set`() =
        runTest {
            respond(PAGE_JSON)

            repository.getCourses(
                CourseFilter(
                    search = "  listening ",
                    examType = CourseExamType.LISTENING_READING,
                    level = CourseLevel.INTERMEDIATE,
                ),
                page = 2,
                pageSize = 10,
            )

            val url = server.takeRequest().url
            assertEquals("/content/api/v1/public/courses", url.encodedPath)
            assertEquals("listening", url.queryParameter("search"))
            assertEquals("ListeningReading", url.queryParameter("examType"))
            assertEquals("Intermediate", url.queryParameter("level"))
            assertNull(url.queryParameter("targetScore"))
            assertEquals("2", url.queryParameter("page"))
            assertEquals("10", url.queryParameter("pageSize"))
        }

    @Test
    fun `blank search is left out of the request`() =
        runTest {
            respond(PAGE_JSON)

            repository.getCourses(CourseFilter(search = "   "), page = 1)

            val url = server.takeRequest().url
            assertNull(url.queryParameter("search"))
            assertEquals("20", url.queryParameter("pageSize"))
        }

    @Test
    fun `catalog page maps prices, seats and enums`() =
        runTest {
            respond(PAGE_JSON)

            val page = (repository.getCourses(CourseFilter(), page = 1) as AppResult.Success).data

            assertEquals(3, page.totalCount)
            assertTrue(page.hasNextPage)
            val (free, paid, unknown) = page.items
            assertTrue(free.price.isFree)
            assertNull(free.remainingSeats)
            assertEquals(CourseExamType.LISTENING_READING, free.examType)
            assertEquals(CourseLevel.BEGINNER, free.level)
            assertNull(free.shortDescription)
            assertEquals(499_000L, paid.price.amount)
            assertEquals(40, paid.remainingSeats)
            assertFalse(paid.price.isFree)
            // Values this app version does not know do not break the whole page.
            assertNull(unknown.examType)
            assertNull(unknown.level)
        }

    @Test
    fun `promotion price is what the learner pays`() =
        runTest {
            respond(PAGE_JSON)

            val paid =
                (
                    repository.getCourses(
                        CourseFilter(),
                        page = 1,
                    ) as AppResult.Success
                ).data.items[1]

            val promotion = paid.price.promotion!!
            assertEquals("Black Friday", promotion.name)
            assertEquals(399_000L, paid.price.payable)
            assertEquals(Instant.parse("2026-11-30T17:00:00Z"), promotion.expiresAt)
        }

    @Test
    fun `detail keeps modules and lessons in teaching order`() =
        runTest {
            respond(DETAIL_JSON)

            val detail = (repository.getCourse("course-2") as AppResult.Success).data

            assertEquals(
                "/content/api/v1/public/courses/course-2",
                server.takeRequest().url.encodedPath,
            )
            assertEquals(listOf("Part 3", "Part 4"), detail.modules.map { it.title })
            assertEquals(
                listOf(
                    "Đọc trước câu hỏi",
                    "Bẫy paraphrase",
                ),
                detail.modules[0].lessons.map {
                    it.title
                },
            )
            assertTrue(detail.modules[0].lessons[0].isFreePreview)
            assertEquals(listOf("Nghe hiểu hội thoại"), detail.skills)
            assertEquals(3, detail.lessonCount)
            assertEquals(600 + 720, detail.totalDurationSeconds)
            assertEquals(CourseLevel.INTERMEDIATE, detail.summary.level)
        }

    @Test
    fun `unpublished course fails with not found`() =
        runTest {
            respond("""{"title":"Not Found","status":404,"code":"COURSE_NOT_FOUND"}""", code = 404)

            val result = repository.getCourse("draft")

            assertEquals(
                AppResult.Failure(AppError.Server(code = "COURSE_NOT_FOUND", status = 404)),
                result,
            )
        }

    @Test
    fun `unreachable server is reported`() =
        runTest {
            server.close()

            val result = repository.getCourses(CourseFilter(), page = 1)

            assertEquals(AppResult.Failure(AppError.ServerUnreachable), result)
        }

    private companion object {
        val PAGE_JSON =
            """
            {"items":[
              {"id":"course-1","title":"TOEIC 450+","slug":"toeic-450","shortDescription":" ","thumbnailUrl":null,
               "examType":"ListeningReading","level":"Beginner","targetScore":450,"price":0,"capacity":null,
               "currency":"VND","remainingSeats":null,"promotion":null},
              {"id":"course-2","title":"Listening Part 3-4","slug":"listening","shortDescription":"Bắt bẫy hội thoại",
               "examType":"ListeningReading","level":"Intermediate","targetScore":650,"price":499000.0,"capacity":40,
               "currency":"VND","remainingSeats":40,
               "promotion":{"promotionId":"p-1","name":"Black Friday","originalPrice":499000,"discountAmount":100000,
                            "sellingPrice":399000,"expiresAt":"2026-11-30T17:00:00Z"}},
              {"id":"course-3","title":"New exam","slug":"new","examType":"Ielts","level":"Expert","price":0,"currency":"VND"}
            ],"page":1,"pageSize":2,"totalCount":3}
            """.trimIndent()

        val DETAIL_JSON =
            """
            {"id":"course-2","title":"Listening Part 3-4","slug":"listening","thumbnailUrl":null,
             "shortDescription":"Bắt bẫy hội thoại","description":"Tập trung Part 3 và Part 4.",
             "examType":"ListeningReading","level":"Intermediate","targetScore":650,
             "skills":["Nghe hiểu hội thoại"," "],"objectives":["Tăng 100+ điểm"],"prerequisites":[],
             "modules":[
               {"id":"m-2","title":"Part 4","description":null,"sortOrder":2,
                "lessons":[{"id":"l-3","title":"Thông báo","description":null,"durationSeconds":null,"sortOrder":1,"isFreePreview":false}]},
               {"id":"m-1","title":"Part 3","description":null,"sortOrder":1,
                "lessons":[
                  {"id":"l-2","title":"Bẫy paraphrase","durationSeconds":720,"sortOrder":2,"isFreePreview":false},
                  {"id":"l-1","title":"Đọc trước câu hỏi","durationSeconds":600,"sortOrder":1,"isFreePreview":true}]}
             ],
             "price":499000,"capacity":40,"currency":"VND","remainingSeats":40,"promotion":null}
            """.trimIndent()
    }
}
