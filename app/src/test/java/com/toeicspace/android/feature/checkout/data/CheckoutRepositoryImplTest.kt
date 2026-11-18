package com.toeicspace.android.feature.checkout.data

import com.toeicspace.android.core.common.AppError
import com.toeicspace.android.core.common.AppResult
import com.toeicspace.android.core.model.AuthUser
import com.toeicspace.android.core.model.UserRole
import com.toeicspace.android.feature.checkout.data.local.CheckoutSessionStore
import com.toeicspace.android.feature.checkout.data.local.StoredReference
import com.toeicspace.android.feature.checkout.data.remote.CheckoutApiService
import com.toeicspace.android.feature.checkout.data.repository.CheckoutRepositoryImpl
import com.toeicspace.android.feature.checkout.domain.DiscountSource
import com.toeicspace.android.feature.checkout.domain.FulfillmentStatus
import com.toeicspace.android.feature.checkout.domain.OrderStatus
import com.toeicspace.android.testing.FakeSessionRepository
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.MediaType.Companion.toMediaType
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.time.Instant

private const val USER_ID = "u1"
private const val COURSE_ID = "c1"

/** In-memory [CheckoutSessionStore], keyed like the DataStore one. */
class FakeCheckoutSessionStore : CheckoutSessionStore {
    val references = mutableMapOf<String, StoredReference>()
    val orders = mutableMapOf<String, String>()

    private fun key(
        userId: String,
        courseId: String,
    ) = "$userId:$courseId"

    override suspend fun reference(
        userId: String,
        courseId: String,
    ) = references[key(userId, courseId)]

    override suspend fun saveReference(
        userId: String,
        courseId: String,
        reference: StoredReference,
    ) {
        references[key(userId, courseId)] = reference
    }

    override suspend fun orderId(
        userId: String,
        courseId: String,
    ) = orders[key(userId, courseId)]

    override suspend fun saveOrderId(
        userId: String,
        courseId: String,
        orderId: String,
    ) {
        orders[key(userId, courseId)] = orderId
    }

    override suspend fun clear(
        userId: String,
        courseId: String,
    ) {
        references.remove(key(userId, courseId))
        orders.remove(key(userId, courseId))
    }
}

class CheckoutRepositoryImplTest {
    private val json =
        Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }
    private val server = MockWebServer()
    private val store = FakeCheckoutSessionStore()
    private lateinit var repository: CheckoutRepositoryImpl

    @Before
    fun setUp() {
        server.start()
        val api =
            Retrofit
                .Builder()
                .baseUrl(server.url("/"))
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .build()
                .create(CheckoutApiService::class.java)
        val session =
            FakeSessionRepository(
                isOnboardingCompleted = true,
                user =
                    AuthUser(
                        id = USER_ID,
                        fullName = "Learner",
                        email = "l@example.com",
                        role = UserRole.User,
                    ),
            )
        repository = CheckoutRepositoryImpl(api, json, session, store)
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

    private fun requestReference(): String {
        val body = server.takeRequest().body!!.utf8()
        return json
            .parseToJsonElement(body)
            .jsonObject["checkoutReference"]!!
            .jsonPrimitive.content
    }

    @Test
    fun `preview sends course and normalised coupon and maps the promotion`() =
        runTest {
            respond(
                """{"courseId":"c1","originalAmount":699000.0,"discountAmount":200000,"finalAmount":499000,
                |"currency":"VND","discountSource":"Promotion","promotionName":"Black Friday",
                |"promotionExpiresAt":"2026-11-30T16:59:59"}
                """.trimMargin(),
            )

            val result = repository.previewPrice(COURSE_ID, "  sale10 ")

            val url = server.takeRequest().url
            assertEquals("/payment/api/v1/orders/preview", url.encodedPath)
            assertEquals("c1", url.queryParameter("courseId"))
            assertEquals("SALE10", url.queryParameter("couponCode"))
            val preview = (result as AppResult.Success).data
            assertEquals(699_000L, preview.amounts.originalAmount)
            assertEquals(499_000L, preview.amounts.finalAmount)
            assertEquals(DiscountSource.PROMOTION, preview.amounts.discountSource)
            assertEquals("Black Friday", preview.amounts.promotionName)
            // No offset from the backend means UTC.
            assertEquals(Instant.parse("2026-11-30T16:59:59Z"), preview.promotionExpiresAt)
        }

    @Test
    fun `preview without a coupon leaves the parameter out`() =
        runTest {
            respond("""{"courseId":"c1","originalAmount":100000,"finalAmount":100000}""")

            repository.previewPrice(COURSE_ID, "  ")

            assertNull(server.takeRequest().url.queryParameter("couponCode"))
        }

    @Test
    fun `pending order maps every transfer field and is remembered`() =
        runTest {
            respond(pendingOrder())

            val order =
                (
                    repository.createOrder(
                        COURSE_ID,
                        couponCode = null,
                    ) as AppResult.Success
                ).data

            val request = server.takeRequest()
            assertEquals("POST", request.method)
            assertEquals("/payment/api/v1/orders", request.url.encodedPath)
            assertEquals(OrderStatus.PENDING, order.status)
            assertEquals(FulfillmentStatus.NOT_STARTED, order.fulfillment)
            assertEquals(Instant.parse("2026-11-19T03:15:00Z"), order.expiresAt)
            val transfer = order.transfer!!
            assertEquals("MBBank", transfer.bank)
            assertEquals("0123456789", transfer.accountNumber)
            assertEquals(499_000L, transfer.amount)
            assertEquals("TSABC123", transfer.memo)
            assertEquals("https://vietqr.app/img?acc=0123456789", transfer.qrUrl)
            assertEquals("o1", repository.resumableOrderId(COURSE_ID))
        }

    @Test
    fun `retry after a failure reuses the same checkout reference`() =
        runTest {
            respond("""{"code":"content_unavailable"}""", code = 503)
            respond(pendingOrder())

            val failed = repository.createOrder(COURSE_ID, couponCode = null)
            repository.createOrder(COURSE_ID, couponCode = null)

            assertEquals(
                AppError.Server(code = "CONTENT_UNAVAILABLE", status = 503),
                (failed as AppResult.Failure).error,
            )
            assertEquals(requestReference(), requestReference())
        }

    @Test
    fun `a different coupon gets a new reference`() =
        runTest {
            respond(pendingOrder())
            respond(pendingOrder())

            repository.createOrder(COURSE_ID, couponCode = null)
            repository.createOrder(COURSE_ID, couponCode = "SALE10")

            val first = requestReference()
            val second = server.takeRequest().body!!.utf8()
            assertFalse(second.contains(first))
            assertTrue(second.contains("\"couponCode\":\"SALE10\""))
        }

    @Test
    fun `a remembered reference of an expired order is replaced and the order created again`() =
        runTest {
            respond(pendingOrder(status = "Expired", withBank = false))
            respond(pendingOrder(id = "o2"))

            val order =
                (
                    repository.createOrder(
                        COURSE_ID,
                        couponCode = null,
                    ) as AppResult.Success
                ).data

            assertEquals("o2", order.id)
            assertNotEquals(requestReference(), requestReference())
            assertEquals("o2", repository.resumableOrderId(COURSE_ID))
        }

    @Test
    fun `a reference conflict is retried once with a new reference`() =
        runTest {
            respond("""{"code":"checkout_reference_conflict","message":"x"}""", code = 409)
            respond(pendingOrder())

            val result = repository.createOrder(COURSE_ID, couponCode = null)

            assertTrue(result is AppResult.Success)
            assertNotEquals(requestReference(), requestReference())
        }

    @Test
    fun `full discount comes back paid without bank details and nothing is left to resume`() =
        runTest {
            respond(
                pendingOrder(
                    status = "Paid",
                    fulfillment = "Pending",
                    finalAmount = 0,
                    withBank = false,
                ),
            )

            val order =
                (
                    repository.createOrder(
                        COURSE_ID,
                        couponCode = "FREE100",
                    ) as AppResult.Success
                ).data

            assertEquals(OrderStatus.PAID, order.status)
            assertEquals(FulfillmentStatus.PENDING, order.fulfillment)
            assertNull(order.transfer)
            assertNull(repository.resumableOrderId(COURSE_ID))
            assertTrue(store.references.isEmpty())
        }

    @Test
    fun `reading a settled order forgets the checkout`() =
        runTest {
            respond(pendingOrder())
            respond(pendingOrder(status = "Cancelled", withBank = false))
            repository.createOrder(COURSE_ID, couponCode = null)

            val order = (repository.getOrder("o1") as AppResult.Success).data

            server.takeRequest()
            assertEquals("/payment/api/v1/orders/o1", server.takeRequest().url.encodedPath)
            assertEquals(OrderStatus.CANCELLED, order.status)
            assertNull(repository.resumableOrderId(COURSE_ID))
        }

    @Test
    fun `cancel posts to the order and forgets the checkout`() =
        runTest {
            respond(pendingOrder())
            server.enqueue(MockResponse.Builder().code(200).build())
            repository.createOrder(COURSE_ID, couponCode = null)

            val result = repository.cancelOrder("o1", COURSE_ID)

            server.takeRequest()
            val cancel = server.takeRequest()
            assertEquals("POST", cancel.method)
            assertEquals("/payment/api/v1/orders/o1/cancel", cancel.url.encodedPath)
            assertTrue(result is AppResult.Success)
            assertNull(repository.resumableOrderId(COURSE_ID))
        }

    @Test
    fun `an unknown status is treated as a bad response`() =
        runTest {
            respond(pendingOrder(status = "Refunded"))

            val result = repository.getOrder("o1")

            assertEquals(AppError.Unknown, (result as AppResult.Failure).error)
        }

    @Test
    fun `a pending order without bank details has no transfer`() =
        runTest {
            respond(pendingOrder(withBank = false))

            val order = (repository.getOrder("o1") as AppResult.Success).data

            assertNull(order.transfer)
        }

    private fun pendingOrder(
        id: String = "o1",
        status: String = "Pending",
        fulfillment: String = "NotStarted",
        finalAmount: Long = 499_000,
        withBank: Boolean = true,
    ): String {
        val bank =
            if (withBank) {
                ""","paymentReference":"TSABC123","bank":"MBBank","accountNumber":"0123456789",
                |"qrUrl":"https://vietqr.app/img?acc=0123456789"
                """.trimMargin()
            } else {
                ""
            }
        return """{"id":"$id","orderCode":"ORDX","courseId":"c1","status":"$status",
            |"fulfillment":"$fulfillment","originalAmount":699000,"couponCode":null,
            |"discountAmount":200000,"finalAmount":$finalAmount,"currency":"VND",
            |"expiresAt":"2026-11-19T03:15:00Z","discountSource":"Promotion"$bank}
            """.trimMargin()
    }
}
