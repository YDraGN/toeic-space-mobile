package com.toeicspace.android.feature.classes.data

import com.toeicspace.android.core.common.AppError
import com.toeicspace.android.core.common.AppResult
import com.toeicspace.android.core.model.AuthUser
import com.toeicspace.android.core.model.UserRole
import com.toeicspace.android.feature.classes.data.remote.ClassroomApiService
import com.toeicspace.android.feature.classes.data.repository.StudentClassRepositoryImpl
import com.toeicspace.android.feature.classes.domain.StudentClassStatus
import com.toeicspace.android.testing.FakeSessionRepository
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
import java.time.LocalDate

class StudentClassRepositoryImplTest {
    private val json =
        Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }
    private val server = MockWebServer()
    private val student =
        AuthUser(id = "u1", fullName = "Minh Anh", email = "a@x.vn", role = UserRole.User)
    private val session = FakeSessionRepository(isOnboardingCompleted = true, user = student)
    private lateinit var repository: StudentClassRepositoryImpl

    @Before
    fun setUp() {
        server.start()
        val api =
            Retrofit
                .Builder()
                .baseUrl(server.url("/"))
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .build()
                .create(ClassroomApiService::class.java)
        repository = StudentClassRepositoryImpl(api, session, json)
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
    fun `list asks the enrolled endpoint for the largest page`() =
        runTest {
            respond(page(listOf(ACTIVE_CLASS), total = 1))

            repository.getMyClasses()

            val url = server.takeRequest().url
            assertEquals("/classroom/api/v1/classes/me/enrolled", url.encodedPath)
            assertEquals("1", url.queryParameter("page"))
            assertEquals("100", url.queryParameter("pageSize"))
        }

    @Test
    fun `list maps status, dates and teacher`() =
        runTest {
            respond(page(listOf(ACTIVE_CLASS, DRAFT_CLASS, UNKNOWN_CLASS), total = 3))

            val classes = (repository.getMyClasses() as AppResult.Success).data

            val (active, draft, unknown) = classes
            assertEquals("TOEIC-650-K12", active.code)
            assertEquals(StudentClassStatus.ACTIVE, active.status)
            assertEquals(LocalDate.of(2026, 9, 7), active.startDate)
            assertEquals(LocalDate.of(2026, 12, 20), active.endDate)
            assertEquals("Trần Thu Hà", active.teacherName)
            assertTrue(active.hasTeacher)
            assertFalse(active.isReadOnly)
            // No teacher and no dates yet.
            assertEquals(StudentClassStatus.DRAFT, draft.status)
            assertNull(draft.startDate)
            assertNull(draft.teacherName)
            assertFalse(draft.hasTeacher)
            assertNull(draft.description)
            // A status this version does not know, and a teacher whose name has not synced.
            assertNull(unknown.status)
            assertNull(unknown.teacherName)
            assertTrue(unknown.hasTeacher)
        }

    @Test
    fun `list reads every page until the total is reached`() =
        runTest {
            respond(page(listOf(ACTIVE_CLASS), total = 2, pageSize = 1))
            respond(page(listOf(DRAFT_CLASS), total = 2, page = 2, pageSize = 1))

            val classes = (repository.getMyClasses() as AppResult.Success).data

            assertEquals(listOf("c1", "c2"), classes.map { it.id })
            assertEquals("1", server.takeRequest().url.queryParameter("page"))
            assertEquals("2", server.takeRequest().url.queryParameter("page"))
        }

    @Test
    fun `detail reads the class inside the detail envelope`() =
        runTest {
            respond("""{"class":$ACTIVE_CLASS,"instructorHistory":[]}""")

            val detail = (repository.getMyClass("c1") as AppResult.Success).data

            assertEquals(
                "/classroom/api/v1/classes/me/enrolled/c1",
                server.takeRequest().url.encodedPath,
            )
            assertEquals("Luyện thi TOEIC 650+", detail.name)
            assertEquals("Lớp buổi tối.", detail.description)
        }

    @Test
    fun `a class the student left is a 404`() =
        runTest {
            respond("""{"status":404,"code":"class_not_found"}""", code = 404)

            val error = (repository.getMyClass("c9") as AppResult.Failure).error

            assertEquals(AppError.Server(code = "CLASS_NOT_FOUND", status = 404), error)
        }

    @Test
    fun `last loaded list and classes stay available for the same account`() =
        runTest {
            assertNull(repository.cachedClasses())
            respond(page(listOf(ACTIVE_CLASS, DRAFT_CLASS), total = 2))
            repository.getMyClasses()
            respond("", code = 503)

            val failed = repository.getMyClasses()

            assertTrue(failed is AppResult.Failure)
            assertEquals(listOf("c1", "c2"), repository.cachedClasses()?.map { it.id })
            assertEquals("SW-K03", repository.cachedClass("c2")?.code)
            assertNull(repository.cachedClass("missing"))
        }

    @Test
    fun `a fresh detail replaces the row cached from the list`() =
        runTest {
            respond(page(listOf(ACTIVE_CLASS), total = 1))
            repository.getMyClasses()
            respond("""{"class":${ACTIVE_CLASS.replace("Lớp buổi tối.", "Đổi sang buổi sáng.")}}""")

            repository.getMyClass("c1")

            assertEquals("Đổi sang buổi sáng.", repository.cachedClass("c1")?.description)
            assertEquals("Đổi sang buổi sáng.", repository.cachedClasses()?.single()?.description)
        }

    @Test
    fun `nothing cached shows after signing out or for another account`() =
        runTest {
            respond(page(listOf(ACTIVE_CLASS), total = 1))
            repository.getMyClasses()

            session.signOut()

            assertNull(repository.cachedClasses())
            assertNull(repository.cachedClass("c1"))
        }

    private companion object {
        val ACTIVE_CLASS =
            """
            {"id":"c1","code":"TOEIC-650-K12","name":"Luyện thi TOEIC 650+","description":"Lớp buổi tối.",
             "status":"Active","startDate":"2026-09-07","endDate":"2026-12-20",
             "createdAt":"2026-08-01T02:00:00Z","createdBy":"a1","updatedAt":"2026-08-02T02:00:00Z",
             "updatedBy":"a1","teacherId":"t1","archivedFromStatus":null,"teacherName":"Trần Thu Hà"}
            """.trimIndent()
        val DRAFT_CLASS =
            """
            {"id":"c2","code":"SW-K03","name":"Speaking & Writing","description":"  ",
             "status":"Draft","startDate":null,"endDate":null,"teacherId":null,"teacherName":null}
            """.trimIndent()
        val UNKNOWN_CLASS =
            """
            {"id":"c3","code":"X-1","name":"Lớp mới","status":"Paused","teacherId":"t2","teacherName":" "}
            """.trimIndent()

        fun page(
            items: List<String>,
            total: Int,
            page: Int = 1,
            pageSize: Int = 100,
        ) = """{"items":[${items.joinToString(
            ",",
        )}],"page":$page,"pageSize":$pageSize,"totalCount":$total}"""
    }
}
