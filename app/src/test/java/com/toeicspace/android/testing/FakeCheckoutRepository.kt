package com.toeicspace.android.testing

import com.toeicspace.android.core.common.AppError
import com.toeicspace.android.core.common.AppResult
import com.toeicspace.android.feature.checkout.domain.BankTransfer
import com.toeicspace.android.feature.checkout.domain.CheckoutOrder
import com.toeicspace.android.feature.checkout.domain.CheckoutRepository
import com.toeicspace.android.feature.checkout.domain.DiscountSource
import com.toeicspace.android.feature.checkout.domain.FulfillmentStatus
import com.toeicspace.android.feature.checkout.domain.OrderAmounts
import com.toeicspace.android.feature.checkout.domain.OrderStatus
import com.toeicspace.android.feature.checkout.domain.PricePreview
import com.toeicspace.android.feature.checkout.domain.QrImageExporter
import java.time.Instant

fun testAmounts(final: Long = 499_000) =
    OrderAmounts(
        originalAmount = 499_000,
        discountAmount = 499_000 - final,
        finalAmount = final,
        currency = "VND",
        discountSource = if (final < 499_000) DiscountSource.COUPON else DiscountSource.NONE,
        promotionName = null,
        couponCode = null,
    )

fun testPreview(final: Long = 499_000) =
    PricePreview(courseId = "c1", amounts = testAmounts(final), promotionExpiresAt = null)

fun testOrder(
    id: String = "o1",
    status: OrderStatus = OrderStatus.PENDING,
    final: Long = 499_000,
    fulfillment: FulfillmentStatus? = null,
) = CheckoutOrder(
    id = id,
    orderCode = "ORD$id",
    courseId = "c1",
    status = status,
    fulfillment =
        fulfillment ?: if (status ==
            OrderStatus.PAID
        ) {
            FulfillmentStatus.COMPLETED
        } else {
            FulfillmentStatus.NOT_STARTED
        },
    amounts = testAmounts(final),
    expiresAt = Instant.parse("2026-11-19T03:15:00Z"),
    transfer =
        if (status == OrderStatus.PENDING) {
            BankTransfer(
                bank = "MBBank",
                accountNumber = "0123456789",
                amount = final,
                memo = "TS$id",
                qrUrl = "https://vietqr.app/img?$id",
            )
        } else {
            null
        },
)

/**
 * Scriptable [CheckoutRepository]. [orderResults] are served one per [getOrder] call, the last one
 * repeating, so a test can play pending, pending, paid.
 */
class FakeCheckoutRepository : CheckoutRepository {
    var preview: AppResult<PricePreview> = AppResult.Success(testPreview())
    var created: AppResult<CheckoutOrder> = AppResult.Success(testOrder())
    var orderResults: MutableList<AppResult<CheckoutOrder>> =
        mutableListOf(AppResult.Success(testOrder()))
    var cancelResult: AppResult<Unit> = AppResult.Success(Unit)
    var rememberedOrderId: String? = null

    val previewCoupons = mutableListOf<String?>()
    val createdCoupons = mutableListOf<String?>()
    val getOrderIds = mutableListOf<String>()
    val cancelledIds = mutableListOf<String>()
    var forgetCount = 0
        private set

    override suspend fun previewPrice(
        courseId: String,
        couponCode: String?,
    ): AppResult<PricePreview> {
        previewCoupons += couponCode
        return preview
    }

    override suspend fun createOrder(
        courseId: String,
        couponCode: String?,
    ): AppResult<CheckoutOrder> {
        createdCoupons += couponCode
        return created
    }

    override suspend fun getOrder(orderId: String): AppResult<CheckoutOrder> {
        getOrderIds += orderId
        return if (orderResults.size > 1) orderResults.removeAt(0) else orderResults.first()
    }

    override suspend fun cancelOrder(
        orderId: String,
        courseId: String,
    ): AppResult<Unit> {
        cancelledIds += orderId
        return cancelResult
    }

    override suspend fun resumableOrderId(courseId: String): String? = rememberedOrderId

    override suspend fun forgetCheckout(courseId: String) {
        forgetCount++
        rememberedOrderId = null
    }

    fun failPreview(error: AppError) {
        preview = AppResult.Failure(error)
    }
}

class FakeQrImageExporter(
    var saveSucceeds: Boolean = true,
    var shareUri: String? = "content://qr",
    override val needsStoragePermission: Boolean = false,
) : QrImageExporter {
    val saved = mutableListOf<Pair<String, String>>()

    override suspend fun saveToGallery(
        qrUrl: String,
        fileName: String,
    ): Boolean {
        saved += qrUrl to fileName
        return saveSucceeds
    }

    override suspend fun prepareShare(
        qrUrl: String,
        fileName: String,
    ): String? = shareUri
}
