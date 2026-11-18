package com.toeicspace.android.feature.checkout.presentation

import androidx.lifecycle.SavedStateHandle
import com.toeicspace.android.R
import com.toeicspace.android.core.common.AppError
import com.toeicspace.android.core.common.AppResult
import com.toeicspace.android.feature.checkout.domain.FulfillmentStatus
import com.toeicspace.android.feature.checkout.domain.OrderStatus
import com.toeicspace.android.feature.checkout.navigation.CheckoutDestination
import com.toeicspace.android.feature.course.domain.CourseDetail
import com.toeicspace.android.testing.FakeCheckoutRepository
import com.toeicspace.android.testing.FakeCourseRepository
import com.toeicspace.android.testing.FakeQrImageExporter
import com.toeicspace.android.testing.MainDispatcherRule
import com.toeicspace.android.testing.testCourse
import com.toeicspace.android.testing.testOrder
import com.toeicspace.android.testing.testPreview
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CheckoutViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val repository = FakeCheckoutRepository()
    private val exporter = FakeQrImageExporter()
    private val courses =
        FakeCourseRepository().apply {
            detail =
                AppResult.Success(
                    CourseDetail(
                        summary = testCourse("c1"),
                        description = null,
                        skills = emptyList(),
                        objectives = emptyList(),
                        prerequisites = emptyList(),
                        modules = emptyList(),
                    ),
                )
        }

    private fun TestScope.started(): CheckoutViewModel =
        CheckoutViewModel(
            savedStateHandle = SavedStateHandle(mapOf(CheckoutDestination.ARG_COURSE_ID to "c1")),
            repository = repository,
            qrImageExporter = exporter,
            courseRepository = courses,
        ).also { runCurrent() }

    private val CheckoutViewModel.phase get() = uiState.value.phase

    private fun TestScope.awaitingTransfer(): CheckoutViewModel =
        started().also {
            it.onCreateOrder()
            runCurrent()
            assertTrue(it.phase is CheckoutPhase.AwaitingTransfer)
        }

    @Test
    fun `opens on the price preview with the course title`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = started()

            assertEquals(CheckoutPhase.Review(testPreview()), viewModel.phase)
            assertEquals("Course c1", viewModel.uiState.value.courseTitle)
            assertEquals(listOf<String?>(null), repository.previewCoupons)
        }

    @Test
    fun `reopening resumes the remembered pending order instead of creating one`() =
        runTest(mainDispatcherRule.testDispatcher) {
            repository.rememberedOrderId = "o1"

            val viewModel = started()

            assertEquals(CheckoutPhase.AwaitingTransfer(testOrder()), viewModel.phase)
            assertTrue(repository.createdCoupons.isEmpty())
            assertTrue(repository.previewCoupons.isEmpty())
        }

    @Test
    fun `a remembered order that expired meanwhile is forgotten and the price shown`() =
        runTest(mainDispatcherRule.testDispatcher) {
            repository.rememberedOrderId = "o1"
            repository.orderResults =
                mutableListOf(AppResult.Success(testOrder(status = OrderStatus.EXPIRED)))

            val viewModel = started()

            assertTrue(viewModel.phase is CheckoutPhase.Review)
            assertEquals(1, repository.forgetCount)
        }

    @Test
    fun `a remembered order the server no longer knows is forgotten`() =
        runTest(mainDispatcherRule.testDispatcher) {
            repository.rememberedOrderId = "o1"
            repository.orderResults =
                mutableListOf(AppResult.Failure(AppError.Server("NOT_FOUND", 404)))

            val viewModel = started()

            assertTrue(viewModel.phase is CheckoutPhase.Review)
        }

    @Test
    fun `offline while resuming shows an error with retry`() =
        runTest(mainDispatcherRule.testDispatcher) {
            repository.rememberedOrderId = "o1"
            repository.orderResults = mutableListOf(AppResult.Failure(AppError.Network))

            val viewModel = started()
            assertEquals(
                CheckoutPhase.LoadFailed(R.string.common_error_network, AppError.Network),
                viewModel.phase,
            )

            repository.orderResults = mutableListOf(AppResult.Success(testOrder()))
            viewModel.onRetry()
            runCurrent()
            assertTrue(viewModel.phase is CheckoutPhase.AwaitingTransfer)
        }

    @Test
    fun `polling stops once the order is paid`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = awaitingTransfer()
            repository.orderResults =
                mutableListOf(
                    AppResult.Success(testOrder()),
                    AppResult.Failure(AppError.Network),
                    AppResult.Success(testOrder(status = OrderStatus.PAID)),
                )

            val poll = launch { viewModel.pollPendingOrder() }
            advanceTimeBy(ORDER_POLL_INTERVAL_MS + 1)
            assertEquals(1, repository.getOrderIds.size)
            assertTrue(viewModel.phase is CheckoutPhase.AwaitingTransfer)
            // A failed check does not stop the polling.
            advanceTimeBy(ORDER_POLL_INTERVAL_MS * 2)
            assertEquals(3, repository.getOrderIds.size)

            assertTrue(viewModel.phase is CheckoutPhase.Paid)
            assertTrue(poll.isCompleted)
            advanceTimeBy(ORDER_POLL_INTERVAL_MS * 3)
            assertEquals(3, repository.getOrderIds.size)
        }

    @Test
    fun `polling continues while a paid order is still being activated`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = awaitingTransfer()
            repository.orderResults =
                mutableListOf(
                    AppResult.Success(
                        testOrder(
                            status = OrderStatus.PAID,
                            fulfillment = FulfillmentStatus.PENDING,
                        ),
                    ),
                    AppResult.Success(
                        testOrder(
                            status = OrderStatus.PAID,
                            fulfillment = FulfillmentStatus.PENDING,
                        ),
                    ),
                    AppResult.Success(
                        testOrder(
                            status = OrderStatus.PAID,
                            fulfillment = FulfillmentStatus.COMPLETED,
                        ),
                    ),
                )

            val poll = launch { viewModel.pollPendingOrder() }
            advanceTimeBy(ORDER_POLL_INTERVAL_MS * 2 + 1)
            assertEquals(2, repository.getOrderIds.size)
            assertTrue(viewModel.uiState.value.shouldPoll)
            assertTrue(!poll.isCompleted)

            advanceTimeBy(ORDER_POLL_INTERVAL_MS)
            assertEquals(3, repository.getOrderIds.size)
            assertTrue(!viewModel.uiState.value.shouldPoll)
            assertTrue(poll.isCompleted)
        }

    @Test
    fun `polling stops when activation needs review`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = awaitingTransfer()
            repository.orderResults =
                mutableListOf(
                    AppResult.Success(
                        testOrder(
                            status = OrderStatus.PAID,
                            fulfillment = FulfillmentStatus.PENDING,
                        ),
                    ),
                    AppResult.Success(
                        testOrder(
                            status = OrderStatus.PAID,
                            fulfillment = FulfillmentStatus.NEEDS_REVIEW,
                        ),
                    ),
                )

            val poll = launch { viewModel.pollPendingOrder() }
            advanceTimeBy(ORDER_POLL_INTERVAL_MS * 2 + 1)
            assertEquals(2, repository.getOrderIds.size)
            assertTrue(poll.isCompleted)
            advanceTimeBy(ORDER_POLL_INTERVAL_MS * 3)
            assertEquals(2, repository.getOrderIds.size)
        }

    @Test
    fun `an expired order offers a new order at the current price`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = awaitingTransfer()
            repository.orderResults =
                mutableListOf(AppResult.Success(testOrder(status = OrderStatus.EXPIRED)))

            val poll = launch { viewModel.pollPendingOrder() }
            advanceTimeBy(ORDER_POLL_INTERVAL_MS + 1)
            assertTrue(poll.isCompleted)
            assertEquals(
                OrderStatus.EXPIRED,
                (viewModel.phase as CheckoutPhase.Closed).order.status,
            )

            repository.created = AppResult.Success(testOrder(id = "o2"))
            viewModel.onStartNewOrder()
            runCurrent()
            assertTrue(viewModel.phase is CheckoutPhase.Review)
            assertEquals(1, repository.forgetCount)

            viewModel.onCreateOrder()
            runCurrent()
            assertEquals("o2", (viewModel.phase as CheckoutPhase.AwaitingTransfer).order.id)
        }

    @Test
    fun `cancel asks first, then cancels and shows the cancelled order`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = awaitingTransfer()

            viewModel.onCancelClick()
            assertTrue(viewModel.uiState.value.showCancelDialog)
            assertTrue(repository.cancelledIds.isEmpty())

            repository.orderResults =
                mutableListOf(AppResult.Success(testOrder(status = OrderStatus.CANCELLED)))
            viewModel.onConfirmCancel()
            runCurrent()

            assertEquals(listOf("o1"), repository.cancelledIds)
            assertFalse(viewModel.uiState.value.showCancelDialog)
            assertEquals(
                OrderStatus.CANCELLED,
                (viewModel.phase as CheckoutPhase.Closed).order.status,
            )
        }

    @Test
    fun `dismissing the dialog keeps the order`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = awaitingTransfer()

            viewModel.onCancelClick()
            viewModel.onDismissCancel()

            assertFalse(viewModel.uiState.value.showCancelDialog)
            assertTrue(repository.cancelledIds.isEmpty())
            assertTrue(viewModel.phase is CheckoutPhase.AwaitingTransfer)
        }

    @Test
    fun `a refused cancel on an order that was just paid shows the success`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = awaitingTransfer()
            repository.cancelResult = AppResult.Failure(AppError.Server("INVALID_STATE", 409))
            repository.orderResults =
                mutableListOf(AppResult.Success(testOrder(status = OrderStatus.PAID)))

            viewModel.onCancelClick()
            viewModel.onConfirmCancel()
            runCurrent()

            assertTrue(viewModel.phase is CheckoutPhase.Paid)
            assertNull(viewModel.uiState.value.messageRes)
        }

    @Test
    fun `a full discount order goes straight to success`() =
        runTest(mainDispatcherRule.testDispatcher) {
            repository.preview = AppResult.Success(testPreview(final = 0))
            repository.created = AppResult.Success(testOrder(status = OrderStatus.PAID, final = 0))
            val viewModel = started()

            viewModel.onCreateOrder()
            runCurrent()

            assertTrue((viewModel.phase as CheckoutPhase.Paid).order != null)
        }

    @Test
    fun `an applied coupon is used for the order`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = started()
            repository.preview = AppResult.Success(testPreview(final = 399_000))

            viewModel.onCouponInputChange(" sale10 ")
            viewModel.onApplyCoupon()
            runCurrent()
            viewModel.onCreateOrder()
            runCurrent()

            assertEquals("SALE10", viewModel.uiState.value.appliedCoupon)
            assertEquals(listOf<String?>("SALE10"), repository.createdCoupons)
        }

    @Test
    fun `an unknown coupon shows an error under the field and keeps the old price`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = started()
            repository.failPreview(AppError.Server("NOT_FOUND", 404))

            viewModel.onCouponInputChange("NOPE")
            viewModel.onApplyCoupon()
            runCurrent()

            assertEquals(
                R.string.checkout_error_coupon_not_found,
                viewModel.uiState.value.couponErrorRes,
            )
            assertNull(viewModel.uiState.value.appliedCoupon)
            assertEquals(CheckoutPhase.Review(testPreview()), viewModel.phase)
        }

    @Test
    fun `an owned course skips the payment`() =
        runTest(mainDispatcherRule.testDispatcher) {
            repository.failPreview(AppError.Server("ALREADY_PURCHASED", 409))

            val viewModel = started()

            assertEquals(CheckoutPhase.Paid(order = null), viewModel.phase)
        }

    @Test
    fun `a failed order creation keeps the price and explains why`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = started()
            repository.created = AppResult.Failure(AppError.Server("PENDING_CHECKOUT", 409))

            viewModel.onCreateOrder()
            runCurrent()

            assertTrue(viewModel.phase is CheckoutPhase.Review)
            assertEquals(
                R.string.checkout_error_pending_checkout,
                viewModel.uiState.value.messageRes,
            )
            assertFalse(viewModel.uiState.value.isCreatingOrder)
        }

    @Test
    fun `saving and sharing the QR use the backend image`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = awaitingTransfer()

            viewModel.onSaveQr()
            runCurrent()
            assertEquals(
                listOf("https://vietqr.app/img?o1" to "ToeicSpace-QR-ORDo1.png"),
                exporter.saved,
            )
            assertEquals(R.string.checkout_msg_qr_saved, viewModel.uiState.value.messageRes)

            viewModel.onShareQr()
            runCurrent()
            assertEquals("content://qr", viewModel.uiState.value.pendingShareUri)
            viewModel.onShareHandled()
            assertNull(viewModel.uiState.value.pendingShareUri)
        }

    @Test
    fun `a failed save says so`() =
        runTest(mainDispatcherRule.testDispatcher) {
            exporter.saveSucceeds = false
            val viewModel = awaitingTransfer()

            viewModel.onSaveQr()
            runCurrent()

            assertEquals(R.string.checkout_error_qr_save, viewModel.uiState.value.messageRes)
        }
}
