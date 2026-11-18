package com.toeicspace.android.feature.checkout.presentation

import androidx.annotation.StringRes
import com.toeicspace.android.R
import com.toeicspace.android.core.common.AppError

/*
 * Payment error codes (PaymentException and the exception handler in Payment's Program.cs),
 * upper-cased by safeApiCall. The backend `message` is English and never shown.
 */
internal const val CODE_ALREADY_PURCHASED = "ALREADY_PURCHASED"
private const val CODE_ALREADY_ENROLLED = "ALREADY_ENROLLED"
internal const val CODE_CHECKOUT_EXPIRED = "CHECKOUT_EXPIRED"
internal const val CODE_PROMOTION_CHANGED = "PROMOTION_CHANGED"
internal const val CODE_INVALID_STATE = "INVALID_STATE"
internal const val CODE_NOT_FOUND = "NOT_FOUND"

private const val HTTP_UNAUTHORIZED = 401
private const val HTTP_SERVER_ERROR = 500

internal val AppError.code: String? get() = (this as? AppError.Server)?.code

internal val AppError.isAlreadyPurchased: Boolean
    get() = code == CODE_ALREADY_PURCHASED || code == CODE_ALREADY_ENROLLED

@StringRes
internal fun AppError.checkoutMessageRes(): Int =
    when (this) {
        AppError.Network -> R.string.common_error_network
        AppError.ServerUnreachable -> R.string.common_error_server_unreachable
        AppError.Unauthorized -> R.string.checkout_error_session_expired
        is AppError.Validation, AppError.Unknown -> R.string.common_error_unknown
        is AppError.Server -> serverMessageRes()
    }

@StringRes
private fun AppError.Server.serverMessageRes(): Int =
    when (code) {
        CODE_ALREADY_PURCHASED, CODE_ALREADY_ENROLLED -> R.string.checkout_error_already_purchased
        "NOT_PURCHASABLE" -> R.string.checkout_error_not_purchasable
        "COURSE_NOT_FOUND" -> R.string.checkout_error_course_not_found
        "COURSE_UNAVAILABLE", "COURSE_FULL" -> R.string.checkout_error_course_unavailable
        "PENDING_CHECKOUT" -> R.string.checkout_error_pending_checkout
        "CHECKOUT_REFERENCE_CONFLICT", "INVALID_CHECKOUT" ->
            R.string.checkout_error_reference_conflict
        CODE_CHECKOUT_EXPIRED -> R.string.checkout_error_checkout_expired
        CODE_PROMOTION_CHANGED, "PROMOTION_UNAVAILABLE" -> R.string.checkout_error_promotion_changed
        "PAYMENT_NOT_CONFIGURED" -> R.string.checkout_error_payment_unavailable
        "CONTENT_UNAVAILABLE", "CONTENT_INVALID" -> R.string.checkout_error_content_unavailable
        "INVALID_COUPON" -> R.string.checkout_error_coupon_not_found
        "COUPON_LIMIT" -> R.string.checkout_error_coupon_limit
        CODE_NOT_FOUND -> R.string.checkout_error_order_not_found
        CODE_INVALID_STATE -> R.string.checkout_error_order_settled
        "CONFLICT", "CONCURRENCY_CONFLICT" -> R.string.checkout_error_conflict
        else ->
            when {
                status == HTTP_UNAUTHORIZED -> R.string.checkout_error_session_expired
                status >= HTTP_SERVER_ERROR -> R.string.common_error_server
                else -> R.string.common_error_unknown
            }
    }

/**
 * A failed coupon preview. Payment reports an unknown coupon as a bare 404 `NOT_FOUND` and an
 * expired, used-up or inapplicable one as a 409 `CONFLICT`, so those two read as coupon problems.
 */
@StringRes
internal fun AppError.couponMessageRes(): Int =
    when (code) {
        CODE_NOT_FOUND, "INVALID_COUPON" -> R.string.checkout_error_coupon_not_found
        "COUPON_LIMIT" -> R.string.checkout_error_coupon_limit
        "CONFLICT" -> R.string.checkout_error_coupon_not_eligible
        else -> checkoutMessageRes()
    }
