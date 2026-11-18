package com.toeicspace.android.feature.checkout.domain

import com.toeicspace.android.core.common.AppResult

/**
 * Buying a paid course by bank transfer. Only for signed-in users: Payment answers 401 otherwise.
 *
 * The backend keeps one pending order per user and course. This repository remembers, per course,
 * the client-generated checkout reference and the order it created, so leaving the screen and
 * coming back resumes that order instead of asking for a new one.
 */
interface CheckoutRepository {
    /** The price the order would have now. A blank [couponCode] means no coupon. */
    suspend fun previewPrice(
        courseId: String,
        couponCode: String?,
    ): AppResult<PricePreview>

    /**
     * Creates the order, or returns the pending one when it already exists. Retrying after a
     * failure reuses the same checkout reference, so a lost response never makes a second order.
     * A full discount comes back already [OrderStatus.PAID].
     */
    suspend fun createOrder(
        courseId: String,
        couponCode: String?,
    ): AppResult<CheckoutOrder>

    suspend fun getOrder(orderId: String): AppResult<CheckoutOrder>

    /**
     * Only a pending order can be cancelled; otherwise the backend answers `INVALID_STATE`. Once
     * cancelled, the checkout for [courseId] is forgotten.
     */
    suspend fun cancelOrder(
        orderId: String,
        courseId: String,
    ): AppResult<Unit>

    /** The order this device created for [courseId] and has not seen settle yet, if any. */
    suspend fun resumableOrderId(courseId: String): String?

    /** Forgets the remembered order and reference, so the next checkout starts a fresh order. */
    suspend fun forgetCheckout(courseId: String)
}
