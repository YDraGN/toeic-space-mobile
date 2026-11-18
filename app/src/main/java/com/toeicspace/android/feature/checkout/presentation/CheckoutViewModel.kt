package com.toeicspace.android.feature.checkout.presentation

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toeicspace.android.R
import com.toeicspace.android.core.common.AppError
import com.toeicspace.android.core.common.AppResult
import com.toeicspace.android.feature.checkout.domain.CheckoutOrder
import com.toeicspace.android.feature.checkout.domain.CheckoutRepository
import com.toeicspace.android.feature.checkout.domain.OrderStatus
import com.toeicspace.android.feature.checkout.domain.QrImageExporter
import com.toeicspace.android.feature.checkout.navigation.CheckoutDestination
import com.toeicspace.android.feature.course.domain.CourseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

/** How often a pending order is checked while the screen is visible. */
internal const val ORDER_POLL_INTERVAL_MS = 5_000L

/**
 * Checkout for one course. The app only shows what Payment says: it never builds a QR or memo and
 * never decides that an order is paid; the SePay webhook does that on the backend.
 */
@HiltViewModel
class CheckoutViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val repository: CheckoutRepository,
        private val qrImageExporter: QrImageExporter,
        private val courseRepository: CourseRepository,
    ) : ViewModel() {
        // Read by key, not toRoute(), so plain JVM tests can build the handle from a map.
        val courseId: String =
            checkNotNull(savedStateHandle[CheckoutDestination.ARG_COURSE_ID]) {
                "CheckoutDestination needs a courseId"
            }

        private val _uiState = MutableStateFlow(CheckoutUiState())
        val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

        val needsStoragePermission: Boolean get() = qrImageExporter.needsStoragePermission

        private val phase: CheckoutPhase get() = _uiState.value.phase

        init {
            loadCourseTitle()
            start()
        }

        fun onRetry() {
            if (phase !is CheckoutPhase.LoadFailed) return
            start()
        }

        fun onCouponInputChange(value: String) {
            _uiState.update { it.copy(couponInput = value, couponErrorRes = null) }
        }

        fun onApplyCoupon() {
            val state = _uiState.value
            val code = state.couponInput.trim()
            if (code.isEmpty() || state.isApplyingCoupon || phase !is CheckoutPhase.Review) return
            _uiState.update { it.copy(isApplyingCoupon = true, couponErrorRes = null) }
            viewModelScope.launch {
                when (val result = repository.previewPrice(courseId, code)) {
                    is AppResult.Success ->
                        _uiState.update {
                            it.copy(
                                phase = CheckoutPhase.Review(result.data),
                                appliedCoupon = code.uppercase(Locale.ROOT),
                                couponInput = code.uppercase(Locale.ROOT),
                                isApplyingCoupon = false,
                            )
                        }
                    is AppResult.Failure ->
                        _uiState.update {
                            it.copy(
                                isApplyingCoupon = false,
                                couponErrorRes = result.error.couponMessageRes(),
                            )
                        }
                }
            }
        }

        fun onRemoveCoupon() {
            if (_uiState.value.isApplyingCoupon) return
            _uiState.update {
                it.copy(
                    couponInput = "",
                    appliedCoupon = null,
                    couponErrorRes = null,
                    isApplyingCoupon = true,
                )
            }
            viewModelScope.launch {
                val result = repository.previewPrice(courseId, couponCode = null)
                _uiState.update {
                    when (result) {
                        is AppResult.Success ->
                            it.copy(
                                phase = CheckoutPhase.Review(result.data),
                                isApplyingCoupon = false,
                            )
                        is AppResult.Failure ->
                            it.copy(
                                isApplyingCoupon = false,
                                messageRes = result.error.checkoutMessageRes(),
                            )
                    }
                }
            }
        }

        fun onCreateOrder() {
            val state = _uiState.value
            if (state.isCreatingOrder || phase !is CheckoutPhase.Review) return
            _uiState.update { it.copy(isCreatingOrder = true) }
            viewModelScope.launch {
                when (val result = repository.createOrder(courseId, state.appliedCoupon)) {
                    is AppResult.Success -> {
                        _uiState.update { it.copy(isCreatingOrder = false) }
                        showOrder(result.data)
                    }
                    is AppResult.Failure -> onCreateOrderFailed(result.error)
                }
            }
        }

        /**
         * Checks the order every [ORDER_POLL_INTERVAL_MS] while it is pending, and after it is paid
         * until the course is granted (fulfilment Completed or NeedsReview), like the web. The screen
         * runs it inside `repeatOnLifecycle(STARTED)`, so it pauses in the background and resumes on
         * return.
         */
        suspend fun pollPendingOrder() {
            while (true) {
                if (phase.polledOrder == null) return
                delay(ORDER_POLL_INTERVAL_MS)
                val order = phase.polledOrder ?: return
                // A failed check (offline, server busy) is retried on the next tick.
                val result = repository.getOrder(order.id)
                if (result is AppResult.Success && phase.polledOrder != null) {
                    showOrder(result.data)
                }
            }
        }

        fun onCancelClick() {
            if (phase !is CheckoutPhase.AwaitingTransfer) return
            _uiState.update { it.copy(showCancelDialog = true) }
        }

        fun onDismissCancel() {
            _uiState.update { it.copy(showCancelDialog = false) }
        }

        fun onConfirmCancel() {
            val order = (phase as? CheckoutPhase.AwaitingTransfer)?.order ?: return
            if (_uiState.value.isCancelling) return
            _uiState.update { it.copy(showCancelDialog = false, isCancelling = true) }
            viewModelScope.launch {
                val cancelled = repository.cancelOrder(order.id, courseId)
                // Read the order back either way: after a cancel it shows the real status, and when
                // the cancel was refused it may have just been paid or expired.
                val latest = repository.getOrder(order.id)
                _uiState.update { it.copy(isCancelling = false) }
                when {
                    latest is AppResult.Success -> showOrder(latest.data)
                    cancelled is AppResult.Success ->
                        showOrder(order.copy(status = OrderStatus.CANCELLED, transfer = null))
                }
                // A refused cancel only needs explaining when the order is still waiting.
                val settled = latest is AppResult.Success && latest.data.status.isFinal
                if (cancelled is AppResult.Failure && !settled) {
                    _uiState.update { it.copy(messageRes = cancelled.error.checkoutMessageRes()) }
                }
            }
        }

        /** From an expired or cancelled order: price the course again before a new order. */
        fun onStartNewOrder() {
            if (phase !is CheckoutPhase.Closed) return
            viewModelScope.launch {
                repository.forgetCheckout(courseId)
                _uiState.update {
                    CheckoutUiState(phase = CheckoutPhase.Loading, courseTitle = it.courseTitle)
                }
                loadPreview()
            }
        }

        fun onSaveQr() {
            val order = (phase as? CheckoutPhase.AwaitingTransfer)?.order ?: return
            val transfer = order.transfer ?: return
            if (_uiState.value.isSavingQr) return
            _uiState.update { it.copy(isSavingQr = true) }
            viewModelScope.launch {
                val saved = qrImageExporter.saveToGallery(transfer.qrUrl, order.qrFileName())
                _uiState.update {
                    it.copy(
                        isSavingQr = false,
                        messageRes =
                            if (saved) {
                                R.string.checkout_msg_qr_saved
                            } else {
                                R.string.checkout_error_qr_save
                            },
                    )
                }
            }
        }

        fun onStoragePermissionDenied() {
            _uiState.update { it.copy(messageRes = R.string.checkout_error_qr_permission) }
        }

        fun onShareQr() {
            val order = (phase as? CheckoutPhase.AwaitingTransfer)?.order ?: return
            val transfer = order.transfer ?: return
            if (_uiState.value.isPreparingShare) return
            _uiState.update { it.copy(isPreparingShare = true) }
            viewModelScope.launch {
                val uri = qrImageExporter.prepareShare(transfer.qrUrl, order.qrFileName())
                _uiState.update {
                    if (uri != null) {
                        it.copy(isPreparingShare = false, pendingShareUri = uri)
                    } else {
                        it.copy(
                            isPreparingShare = false,
                            messageRes = R.string.checkout_error_qr_share,
                        )
                    }
                }
            }
        }

        fun onShareHandled() {
            _uiState.update { it.copy(pendingShareUri = null) }
        }

        fun onCopied(
            @StringRes messageRes: Int,
        ) {
            _uiState.update { it.copy(messageRes = messageRes) }
        }

        fun onMessageShown() {
            _uiState.update { it.copy(messageRes = null) }
        }

        /** Only for the heading; the price always comes from Payment, never from the catalog. */
        private fun loadCourseTitle() {
            viewModelScope.launch {
                val result = courseRepository.getCourse(courseId)
                if (result is AppResult.Success) {
                    _uiState.update { it.copy(courseTitle = result.data.summary.title) }
                }
            }
        }

        private fun start() {
            _uiState.update { it.copy(phase = CheckoutPhase.Loading) }
            viewModelScope.launch {
                if (!resumeRememberedOrder()) loadPreview()
            }
        }

        /** True when a remembered order is shown; false when the checkout should start at the price. */
        private suspend fun resumeRememberedOrder(): Boolean {
            val orderId = repository.resumableOrderId(courseId) ?: return false
            return when (val result = repository.getOrder(orderId)) {
                is AppResult.Success -> {
                    val order = result.data
                    // An order that ended while the learner was away needs a new one at today's price.
                    if (order.status == OrderStatus.PENDING || order.status == OrderStatus.PAID) {
                        showOrder(order)
                        true
                    } else {
                        repository.forgetCheckout(courseId)
                        false
                    }
                }
                is AppResult.Failure ->
                    if (result.error.code == CODE_NOT_FOUND) {
                        repository.forgetCheckout(courseId)
                        false
                    } else {
                        _uiState.update {
                            it.copy(
                                phase = result.error.toLoadFailed(),
                            )
                        }
                        true
                    }
            }
        }

        private suspend fun loadPreview() {
            val coupon = _uiState.value.appliedCoupon
            when (val result = repository.previewPrice(courseId, coupon)) {
                is AppResult.Success ->
                    _uiState.update { it.copy(phase = CheckoutPhase.Review(result.data)) }
                is AppResult.Failure ->
                    _uiState.update {
                        it.copy(
                            phase =
                                if (result.error.isAlreadyPurchased) {
                                    CheckoutPhase.Paid(order = null)
                                } else {
                                    result.error.toLoadFailed()
                                },
                        )
                    }
            }
        }

        private fun showOrder(order: CheckoutOrder) {
            val next =
                when (order.status) {
                    OrderStatus.PENDING ->
                        if (order.transfer != null) {
                            CheckoutPhase.AwaitingTransfer(order)
                        } else {
                            // Pending with something to pay but no bank details: Payment is misconfigured.
                            CheckoutPhase.LoadFailed(R.string.checkout_error_payment_unavailable)
                        }
                    OrderStatus.PAID -> CheckoutPhase.Paid(order)
                    OrderStatus.EXPIRED, OrderStatus.CANCELLED -> CheckoutPhase.Closed(order)
                }
            _uiState.update { it.copy(phase = next, showCancelDialog = false) }
        }

        private suspend fun onCreateOrderFailed(error: AppError) {
            _uiState.update { it.copy(isCreatingOrder = false) }
            when {
                error.isAlreadyPurchased ->
                    _uiState.update { it.copy(phase = CheckoutPhase.Paid(order = null)) }
                // The price moved under the learner: show the new one before they try again.
                error.code == CODE_CHECKOUT_EXPIRED || error.code == CODE_PROMOTION_CHANGED -> {
                    _uiState.update { it.copy(messageRes = error.checkoutMessageRes()) }
                    loadPreview()
                }
                else -> _uiState.update { it.copy(messageRes = error.checkoutMessageRes()) }
            }
        }
    }

private fun AppError.toLoadFailed() = CheckoutPhase.LoadFailed(checkoutMessageRes(), this)

/** File name of the saved or shared QR. The order code is not the memo, so nothing secret is in it. */
internal fun CheckoutOrder.qrFileName(): String = "ToeicSpace-QR-$orderCode.png"
