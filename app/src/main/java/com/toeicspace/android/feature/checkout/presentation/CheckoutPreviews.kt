package com.toeicspace.android.feature.checkout.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.toeicspace.android.R
import com.toeicspace.android.core.common.AppError
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme
import com.toeicspace.android.feature.checkout.domain.BankTransfer
import com.toeicspace.android.feature.checkout.domain.CheckoutOrder
import com.toeicspace.android.feature.checkout.domain.DiscountSource
import com.toeicspace.android.feature.checkout.domain.FulfillmentStatus
import com.toeicspace.android.feature.checkout.domain.OrderAmounts
import com.toeicspace.android.feature.checkout.domain.OrderStatus
import com.toeicspace.android.feature.checkout.domain.PricePreview
import java.time.Instant

private val PreviewActions =
    CheckoutActions(
        onBackClick = {},
        onRetry = {},
        onCouponInputChange = {},
        onApplyCoupon = {},
        onRemoveCoupon = {},
        onCreateOrder = {},
        onSaveQr = {},
        onShareQr = {},
        onCopied = {},
        onCancelClick = {},
        onDismissCancel = {},
        onConfirmCancel = {},
        onStartNewOrder = {},
        onStartLearning = {},
    )

private val PreviewAmounts =
    OrderAmounts(
        originalAmount = 699_000,
        discountAmount = 200_000,
        finalAmount = 499_000,
        currency = "VND",
        discountSource = DiscountSource.PROMOTION,
        promotionName = "Ưu đãi tháng 11",
        couponCode = null,
    )

private const val PREVIEW_TITLE = "TOEIC 650+ Nghe Đọc cấp tốc"

private val PreviewReview =
    CheckoutUiState(
        phase =
            CheckoutPhase.Review(
                PricePreview(
                    courseId = "c1",
                    amounts = PreviewAmounts,
                    promotionExpiresAt = Instant.parse("2026-11-30T16:59:59Z"),
                ),
            ),
        courseTitle = PREVIEW_TITLE,
        couponInput = "SALE10",
        couponErrorRes = R.string.checkout_error_coupon_not_found,
    )

private val PreviewOrder =
    CheckoutOrder(
        id = "o1",
        orderCode = "ORD3F9A1C2B7D4E",
        courseId = "c1",
        status = OrderStatus.PENDING,
        fulfillment = FulfillmentStatus.NOT_STARTED,
        amounts = PreviewAmounts,
        expiresAt = Instant.now().plusSeconds(14 * 60L + 32),
        transfer =
            BankTransfer(
                bank = "MBBank",
                accountNumber = "0123456789",
                amount = 499_000,
                memo = "TS8C21F0A9B3E1",
                qrUrl = "https://vietqr.app/img?preview",
            ),
    )

private val PreviewAwaiting =
    CheckoutUiState(
        phase = CheckoutPhase.AwaitingTransfer(PreviewOrder),
        courseTitle = PREVIEW_TITLE,
    )

@Composable
private fun Preview(state: CheckoutUiState) {
    ToeicSpaceTheme { CheckoutScreen(state = state, actions = PreviewActions) }
}

@Preview(name = "Review · phone", widthDp = 390, heightDp = 900)
@Composable
private fun CheckoutReviewPhonePreview() = Preview(PreviewReview)

@Preview(name = "Review · tablet", widthDp = 1280, heightDp = 800)
@Composable
private fun CheckoutReviewTabletPreview() = Preview(PreviewReview)

@Preview(name = "Transfer · phone", widthDp = 390, heightDp = 1500)
@Composable
private fun CheckoutTransferPhonePreview() = Preview(PreviewAwaiting)

@Preview(name = "Transfer · tablet", widthDp = 1280, heightDp = 900)
@Composable
private fun CheckoutTransferTabletPreview() = Preview(PreviewAwaiting)

@Preview(name = "Cancel dialog · phone", widthDp = 390, heightDp = 800)
@Composable
private fun CheckoutCancelDialogPreview() = Preview(PreviewAwaiting.copy(showCancelDialog = true))

@Preview(name = "Paid · phone", widthDp = 390, heightDp = 800)
@Composable
private fun CheckoutPaidPhonePreview() =
    Preview(
        CheckoutUiState(
            phase =
                CheckoutPhase.Paid(
                    PreviewOrder.copy(
                        status = OrderStatus.PAID,
                        fulfillment = FulfillmentStatus.COMPLETED,
                        transfer = null,
                    ),
                ),
        ),
    )

@Preview(name = "Paid · tablet", widthDp = 1280, heightDp = 800)
@Composable
private fun CheckoutPaidTabletPreview() =
    Preview(CheckoutUiState(phase = CheckoutPhase.Paid(order = null)))

@Preview(name = "Expired · phone", widthDp = 390, heightDp = 800)
@Composable
private fun CheckoutExpiredPhonePreview() =
    Preview(
        CheckoutUiState(
            phase =
                CheckoutPhase.Closed(
                    PreviewOrder.copy(status = OrderStatus.EXPIRED, transfer = null),
                ),
        ),
    )

@Preview(name = "Cancelled · tablet", widthDp = 1280, heightDp = 800)
@Composable
private fun CheckoutCancelledTabletPreview() =
    Preview(
        CheckoutUiState(
            phase =
                CheckoutPhase.Closed(
                    PreviewOrder.copy(status = OrderStatus.CANCELLED, transfer = null),
                ),
        ),
    )

@Preview(name = "Loading · phone", widthDp = 390, heightDp = 800)
@Composable
private fun CheckoutLoadingPreview() = Preview(CheckoutUiState())

@Preview(name = "Error · phone", widthDp = 390, heightDp = 800)
@Composable
private fun CheckoutErrorPhonePreview() =
    Preview(
        CheckoutUiState(
            phase = CheckoutPhase.LoadFailed(R.string.common_error_network, AppError.Network),
        ),
    )

@Preview(name = "Error · tablet", widthDp = 1280, heightDp = 800)
@Composable
private fun CheckoutErrorTabletPreview() =
    Preview(
        CheckoutUiState(
            phase = CheckoutPhase.LoadFailed(R.string.checkout_error_payment_unavailable),
        ),
    )
