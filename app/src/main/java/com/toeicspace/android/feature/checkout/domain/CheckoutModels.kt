package com.toeicspace.android.feature.checkout.domain

import java.time.Instant

/** `OrderStatus` on Payment. Only [PENDING] can still change; the others are final. */
enum class OrderStatus {
    PENDING,
    PAID,
    EXPIRED,
    CANCELLED,
    ;

    val isFinal: Boolean get() = this != PENDING
}

/** `FulfillmentStatus`: whether Content has granted the course after the order was paid. */
enum class FulfillmentStatus {
    NOT_STARTED,
    PENDING,
    COMPLETED,

    /** Paid, but the course could not claim a seat; support has to look at it. */
    NEEDS_REVIEW,
    ;

    /** Granting has ended, one way or the other, so there is nothing left to wait for. */
    val isSettled: Boolean get() = this == COMPLETED || this == NEEDS_REVIEW
}

/** Which discount Payment applied. It keeps the larger one; a promotion wins a tie. */
enum class DiscountSource {
    NONE,
    COUPON,
    PROMOTION,
}

/** Amounts are whole VND (C# decimals on the backend, rounded). */
data class OrderAmounts(
    val originalAmount: Long,
    val discountAmount: Long,
    val finalAmount: Long,
    val currency: String,
    val discountSource: DiscountSource,
    val promotionName: String?,
    val couponCode: String?,
) {
    val hasDiscount: Boolean get() = discountAmount > 0
}

/** `PricePreview`: the authoritative price before an order exists. It is not a commitment. */
data class PricePreview(
    val courseId: String,
    val amounts: OrderAmounts,
    val promotionExpiresAt: Instant?,
)

/**
 * What the learner types into the banking app. All of it comes from the backend as is: the app
 * never builds its own QR code or memo, because SePay matches the transfer on these exact values.
 */
data class BankTransfer(
    val bank: String,
    val accountNumber: String,
    val amount: Long,
    val memo: String,
    val qrUrl: String,
)

/** `OrderResult`. [transfer] is only present while the order waits for a transfer. */
data class CheckoutOrder(
    val id: String,
    val orderCode: String,
    val courseId: String,
    val status: OrderStatus,
    val fulfillment: FulfillmentStatus,
    val amounts: OrderAmounts,
    val expiresAt: Instant,
    val transfer: BankTransfer?,
) {
    /** Payment has the money but Content has not granted the course yet. */
    val isActivating: Boolean get() = status == OrderStatus.PAID && !fulfillment.isSettled
}
