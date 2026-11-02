package com.toeicspace.android.core.network

import com.toeicspace.android.core.common.Clock
import com.toeicspace.android.core.datastore.AccessToken
import com.toeicspace.android.core.datastore.SessionTokens
import com.toeicspace.android.testing.FakeSessionTokenStore
import com.toeicspace.android.testing.TestUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.GET

private const val NOW = 1_000_000L

private fun authResponseBody(accessToken: String) =
    """
    {"accessToken":"$accessToken","tokenType":"Bearer","expiresIn":600,
     "expiresAt":"2026-10-30T10:10:00+00:00",
     "user":{"id":"user-1","fullName":"Oysteic","email":"oysteic@toeicspace.vn","role":"User"},
     "refreshToken":"refresh-2","refreshTokenExpiresAt":"2099-01-01T00:00:00+00:00"}
    """.trimIndent()

private interface ProtectedApi {
    @GET("protected")
    suspend fun get()
}

class TokenRefresherTest {
    private val server = MockWebServer()
    private val json = Json { ignoreUnknownKeys = true }
    private val clock = Clock { NOW }
    private val store = FakeSessionTokenStore { NOW }
    private lateinit var refresher: TokenRefresher

    @Before
    fun setUp() {
        server.start()
        refresher =
            TokenRefresher(
                retrofit(OkHttpClient()).create(SessionApiService::class.java),
                store,
                clock,
            )
    }

    @After
    fun tearDown() {
        server.close()
    }

    private fun retrofit(client: OkHttpClient): Retrofit =
        Retrofit
            .Builder()
            .baseUrl(server.url("/"))
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    private fun signIn(
        accessToken: String = "access-1",
        accessExpiresAt: Long = NOW + 600_000,
    ) = runBlocking {
        store.save(
            SessionTokens(
                accessToken = AccessToken(accessToken, accessExpiresAt),
                refreshToken = "refresh-1",
                refreshTokenExpiresAtMillis = NOW + 86_400_000,
            ),
            TestUser,
        )
    }

    @Test
    fun `concurrent refreshes send a single request`() =
        runBlocking {
            signIn(accessExpiresAt = NOW)
            server.enqueue(
                MockResponse
                    .Builder()
                    .code(200)
                    .body(authResponseBody("access-2"))
                    .build(),
            )

            val results =
                (1..5)
                    .map { async(Dispatchers.IO) { refresher.refresh(staleToken = "access-1") } }
                    .awaitAll()

            assertEquals(1, server.requestCount)
            assertTrue(
                results.all {
                    it ==
                        RefreshResult.Refreshed(AccessToken("access-2", NOW + 600_000))
                },
            )
            assertEquals("refresh-2", store.refreshToken())
        }

    @Test
    fun `rejected refresh token ends the session`() =
        runBlocking {
            signIn(accessExpiresAt = NOW)
            server.enqueue(MockResponse.Builder().code(401).build())

            val result = refresher.refresh(staleToken = "access-1")

            assertEquals(RefreshResult.SessionEnded, result)
            assertNull(store.tokens)
        }

    @Test
    fun `server outage keeps the session`() =
        runBlocking {
            signIn(accessExpiresAt = NOW)
            server.enqueue(MockResponse.Builder().code(503).build())

            val result = refresher.refresh(staleToken = "access-1")

            assertEquals(RefreshResult.Unavailable, result)
            assertEquals("refresh-1", store.refreshToken())
        }

    @Test
    fun `without a stored session nothing is sent`() =
        runBlocking {
            val result = refresher.refresh(staleToken = null)

            assertEquals(RefreshResult.SessionEnded, result)
            assertEquals(0, server.requestCount)
        }

    @Test
    fun `missing access token is refreshed before the protected request`() =
        runBlocking {
            signIn(accessExpiresAt = NOW)
            server.enqueue(
                MockResponse
                    .Builder()
                    .code(200)
                    .body(authResponseBody("access-2"))
                    .build(),
            )
            server.enqueue(MockResponse.Builder().code(204).build())

            protectedApi().get()

            assertEquals(2, server.requestCount)
            server.takeRequest()
            assertEquals("Bearer access-2", server.takeRequest().headers["Authorization"])
        }

    @Test
    fun `401 on a protected request refreshes once and retries`() =
        runBlocking {
            signIn()
            server.enqueue(MockResponse.Builder().code(401).build())
            server.enqueue(
                MockResponse
                    .Builder()
                    .code(200)
                    .body(authResponseBody("access-2"))
                    .build(),
            )
            server.enqueue(MockResponse.Builder().code(204).build())

            protectedApi().get()

            assertEquals("Bearer access-1", server.takeRequest().headers["Authorization"])
            server.takeRequest()
            assertEquals("Bearer access-2", server.takeRequest().headers["Authorization"])
        }

    private fun protectedApi(): ProtectedApi {
        val client =
            OkHttpClient
                .Builder()
                .addInterceptor(AuthInterceptor(store, refresher))
                .authenticator(TokenAuthenticator(refresher))
                .build()
        return retrofit(client).create(ProtectedApi::class.java)
    }
}
