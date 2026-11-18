package com.toeicspace.android.feature.checkout.presentation

import androidx.annotation.StringRes
import com.toeicspace.android.core.common.AppError
import com.toeicspace.android.feature.checkout.domain.CheckoutOrder
import com.toeicspace.android.feature.checkout.domain.PricePreview

/** Where the checkout is. Exactly one of these fills the screen. */
sealed interface CheckoutPhase {
    data object Loading : CheckoutPhase

    /**
     * Nothing to show yet: the price or the remembered order could not be loaded. [error] picks the
     * mascot and whether retrying makes sense; [messageRes] says what went wrong in checkout terms.
     */
    data class LoadFailed(
        @param:StringRes val messageRes: Int,
        val error: AppError = AppError.Unknown,
    ) : CheckoutPhase

    /** The price before an order exists, with the coupon field. */
    data class Review(
        val preview: PricePreview,
    ) : CheckoutPhase

    /** A pending order: QR, bank details and the countdown. Polled while visible. */
    data class AwaitingTransfer(
        val order: CheckoutOrder,
    ) : CheckoutPhase

    /** Payment confirmed the order. [order] is null when the learner already owned the course. */
    data class Paid(
        val order: CheckoutOrder?,
    ) : CheckoutPhase

    /** Expired or cancelled: the learner can start a new order at today's price. */
    data class Closed(
        val order: CheckoutOrder,
    ) : CheckoutPhase
}

/**
 * The order to keep checking, as the web's `shouldPoll` does: a pending order, or a paid one whose
 * course is still being granted. Null once nothing can change any more.
 */
internal val CheckoutPhase.polledOrder: CheckoutOrder?
    get() =
        when (this) {
            is CheckoutPhase.AwaitingTransfer -> order
            is CheckoutPhase.Paid -> order?.takeIf { it.isActivating }
            else -> null
        }

data class CheckoutUiState(
    val phase: CheckoutPhase = CheckoutPhase.Loading,
    /** From the public catalog, for the heading only; null until (or unless) it loads. */
    val courseTitle: String? = null,
    val couponInput: String = "",
    /** The coupon the shown preview was priced with, as Payment normalises it (upper case). */
    val appliedCoupon: String? = null,
    @param:StringRes val couponErrorRes: Int? = null,
    val isApplyingCoupon: Boolean = false,
    val isCreatingOrder: Boolean = false,
    val showCancelDialog: Boolean = false,
    val isCancelling: Boolean = false,
    val isSavingQr: Boolean = false,
    val isPreparingShare: Boolean = false,
    /** A one-off snackbar; cleared with `onMessageShown`. */
    @param:StringRes val messageRes: Int? = null,
    /** A `content://` uri waiting to be handed to the share sheet; cleared with `onShareHandled`. */
    val pendingShareUri: String? = null,
) {
    /** True while the order can still change on the server, so the screen keeps polling it. */
    val shouldPoll: Boolean get() = phase.polledOrder != null
}
