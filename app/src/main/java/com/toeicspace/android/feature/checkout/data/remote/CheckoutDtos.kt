package com.toeicspace.android.feature.checkout.data.remote

import com.toeicspace.android.feature.checkout.domain.BankTransfer
import com.toeicspace.android.feature.checkout.domain.CheckoutOrder
import com.toeicspace.android.feature.checkout.domain.DiscountSource
import com.toeicspace.android.feature.checkout.domain.FulfillmentStatus
import com.toeicspace.android.feature.checkout.domain.OrderAmounts
import com.toeicspace.android.feature.checkout.domain.OrderStatus
import com.toeicspace.android.feature.checkout.domain.PricePreview
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeParseException
import kotlin.math.roundToLong

/*
 * Payment serializes enums as their names (JsonStringEnumConverter): "Pending", "NeedsReview",
 * "Promotion". Amounts are C# decimals, read as Double so "499000" and "499000.0" both parse.
 * Dates are UTC; a value without an offset is read as UTC too.
 */

@Serializable
data class CreateOrderRequestDto(
    val courseId: String,
    val checkoutReference: String,
    val couponCode: String? = null,
)

/** `PricePreview`. */
@Serializable
data class PricePreviewDto(
    val courseId: String,
    val originalAmount: Double,
    val discountAmount: Double = 0.0,
    val finalAmount: Double,
    val currency: String = DEFAULT_CURRENCY,
    val discountSource: String? = null,
    val promotionName: String? = null,
    val promotionExpiresAt: String? = null,
)

/** `OrderResult`. Bank fields are null once the order is settled, or when nothing is left to pay. */
@Serializable
data class OrderResultDto(
    val id: String,
    val orderCode: String,
    val courseId: String,
    val status: String,
    val fulfillment: String? = null,
    val originalAmount: Double,
    val couponCode: String? = null,
    val discountAmount: Double = 0.0,
    val finalAmount: Double,
    val currency: String = DEFAULT_CURRENCY,
    val expiresAt: String,
    val paymentReference: String? = null,
    val bank: String? = null,
    val accountNumber: String? = null,
    val qrUrl: String? = null,
    val discountSource: String? = null,
    val promotionName: String? = null,
)

private const val DEFAULT_CURRENCY = "VND"

private fun String?.blankToNull(): String? = this?.takeIf(String::isNotBlank)

internal fun String?.toInstantOrNull(): Instant? {
    val value = this?.trim().blankToNull() ?: return null
    return try {
        Instant.parse(value)
    } catch (_: DateTimeParseException) {
        try {
            LocalDateTime.parse(value).toInstant(ZoneOffset.UTC)
        } catch (_: DateTimeParseException) {
            null
        }
    }
}

/** An unknown status is a contract break: fail the call rather than guess what the order is. */
internal fun String.toOrderStatus(): OrderStatus =
    when (lowercase()) {
        "pending" -> OrderStatus.PENDING
        "paid" -> OrderStatus.PAID
        "expired" -> OrderStatus.EXPIRED
        "cancelled", "canceled" -> OrderStatus.CANCELLED
        else -> throw SerializationException("Unknown order status")
    }

internal fun String?.toFulfillmentStatus(): FulfillmentStatus =
    when (this?.lowercase()) {
        "pending" -> FulfillmentStatus.PENDING
        "completed" -> FulfillmentStatus.COMPLETED
        "needsreview" -> FulfillmentStatus.NEEDS_REVIEW
        else -> FulfillmentStatus.NOT_STARTED
    }

internal fun String?.toDiscountSource(): DiscountSource =
    when (this?.lowercase()) {
        "coupon" -> DiscountSource.COUPON
        "promotion" -> DiscountSource.PROMOTION
        else -> DiscountSource.NONE
    }

internal fun PricePreviewDto.toDomain() =
    PricePreview(
        courseId = courseId,
        amounts =
            OrderAmounts(
                originalAmount = originalAmount.roundToLong(),
                discountAmount = discountAmount.roundToLong(),
                finalAmount = finalAmount.roundToLong(),
                currency = currency,
                discountSource = discountSource.toDiscountSource(),
                promotionName = promotionName.blankToNull(),
                couponCode = null,
            ),
        promotionExpiresAt = promotionExpiresAt.toInstantOrNull(),
    )

/** Throws [SerializationException] for an unknown status or an unreadable expiry, as a bad body. */
internal fun OrderResultDto.toDomain(): CheckoutOrder {
    val status = status.toOrderStatus()
    val finalAmount = finalAmount.roundToLong()
    val bank = bank.blankToNull()
    val accountNumber = accountNumber.blankToNull()
    val memo = paymentReference.blankToNull()
    val qrUrl = qrUrl.blankToNull()
    val transfer =
        if (status == OrderStatus.PENDING &&
            bank != null &&
            accountNumber != null &&
            memo != null &&
            qrUrl != null
        ) {
            BankTransfer(
                bank = bank,
                accountNumber = accountNumber,
                amount = finalAmount,
                memo = memo,
                qrUrl = qrUrl,
            )
        } else {
            null
        }
    return CheckoutOrder(
        id = id,
        orderCode = orderCode,
        courseId = courseId,
        status = status,
        fulfillment = fulfillment.toFulfillmentStatus(),
        amounts =
            OrderAmounts(
                originalAmount = originalAmount.roundToLong(),
                discountAmount = discountAmount.roundToLong(),
                finalAmount = finalAmount,
                currency = currency,
                discountSource = discountSource.toDiscountSource(),
                promotionName = promotionName.blankToNull(),
                couponCode = couponCode.blankToNull(),
            ),
        expiresAt =
            expiresAt.toInstantOrNull() ?: throw SerializationException("Unreadable expiresAt"),
        transfer = transfer,
    )
}
