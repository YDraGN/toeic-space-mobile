package com.toeicspace.android.feature.checkout.data.repository

import com.toeicspace.android.core.common.AppError
import com.toeicspace.android.core.common.AppResult
import com.toeicspace.android.core.common.map
import com.toeicspace.android.core.data.repository.SessionRepository
import com.toeicspace.android.core.model.SessionState
import com.toeicspace.android.core.network.safeApiCall
import com.toeicspace.android.feature.checkout.data.local.CheckoutSessionStore
import com.toeicspace.android.feature.checkout.data.local.StoredReference
import com.toeicspace.android.feature.checkout.data.remote.CheckoutApiService
import com.toeicspace.android.feature.checkout.data.remote.CreateOrderRequestDto
import com.toeicspace.android.feature.checkout.data.remote.toDomain
import com.toeicspace.android.feature.checkout.domain.CheckoutOrder
import com.toeicspace.android.feature.checkout.domain.CheckoutRepository
import com.toeicspace.android.feature.checkout.domain.OrderStatus
import com.toeicspace.android.feature.checkout.domain.PricePreview
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

/** Payment answers this when a reference was first used for another course or coupon. */
internal const val CHECKOUT_REFERENCE_CONFLICT = "CHECKOUT_REFERENCE_CONFLICT"

internal class CheckoutRepositoryImpl
    @Inject
    constructor(
        private val api: CheckoutApiService,
        private val json: Json,
        private val sessionRepository: SessionRepository,
        private val store: CheckoutSessionStore,
    ) : CheckoutRepository {
        override suspend fun previewPrice(
            courseId: String,
            couponCode: String?,
        ): AppResult<PricePreview> =
            safeApiCall(json) {
                api.previewPrice(courseId = courseId, couponCode = couponCode.normalizedCoupon())
            }.map { it.toDomain() }

        override suspend fun createOrder(
            courseId: String,
            couponCode: String?,
        ): AppResult<CheckoutOrder> {
            val userId = currentUserId() ?: return AppResult.Failure(AppError.Unauthorized)
            val coupon = couponCode.normalizedCoupon()
            val first = submitOrder(userId, courseId, coupon, reuseReference = true)
            // A remembered reference can point at an order that has since expired or been
            // cancelled; Payment then returns that old order. Start over once with a new reference.
            return if (first.needsFreshReference()) {
                store.clear(userId, courseId)
                submitOrder(userId, courseId, coupon, reuseReference = false)
            } else {
                first
            }
        }

        override suspend fun getOrder(orderId: String): AppResult<CheckoutOrder> {
            val result = safeApiCall(json) { api.getOrder(orderId).toDomain() }
            if (result is AppResult.Success) currentUserId()?.let { remember(it, result.data) }
            return result
        }

        override suspend fun cancelOrder(
            orderId: String,
            courseId: String,
        ): AppResult<Unit> {
            val result = safeApiCall(json) { api.cancelOrder(orderId) }
            if (result is AppResult.Success) forgetCheckout(courseId)
            return result
        }

        override suspend fun resumableOrderId(courseId: String): String? =
            currentUserId()?.let { store.orderId(it, courseId) }

        override suspend fun forgetCheckout(courseId: String) {
            currentUserId()?.let { store.clear(it, courseId) }
        }

        private suspend fun submitOrder(
            userId: String,
            courseId: String,
            coupon: String?,
            reuseReference: Boolean,
        ): AppResult<CheckoutOrder> {
            // A reference belongs to one coupon choice: Payment rejects it with another coupon.
            val reference =
                store
                    .reference(userId, courseId)
                    ?.takeIf { reuseReference && it.couponCode == coupon }
                    ?: StoredReference(UUID.randomUUID().toString(), coupon).also {
                        // Saved before the call, so a retry after a lost response reuses it.
                        store.saveReference(userId, courseId, it)
                    }
            val result =
                safeApiCall(json) {
                    api
                        .createOrder(
                            CreateOrderRequestDto(
                                courseId = courseId,
                                checkoutReference = reference.reference,
                                couponCode = coupon,
                            ),
                        ).toDomain()
                }
            if (result is AppResult.Success) remember(userId, result.data)
            return result
        }

        /** A pending order is kept for resuming; a settled one has nothing left to resume. */
        private suspend fun remember(
            userId: String,
            order: CheckoutOrder,
        ) {
            if (order.status.isFinal) {
                store.clear(userId, order.courseId)
            } else {
                store.saveOrderId(userId, order.courseId, order.id)
            }
        }

        private fun AppResult<CheckoutOrder>.needsFreshReference(): Boolean =
            when (this) {
                is AppResult.Success ->
                    data.status == OrderStatus.EXPIRED || data.status == OrderStatus.CANCELLED
                is AppResult.Failure ->
                    (error as? AppError.Server)?.code == CHECKOUT_REFERENCE_CONFLICT
            }

        private suspend fun currentUserId(): String? =
            sessionRepository.session
                .filterIsInstance<SessionState.Ready>()
                .first()
                .user
                ?.id
    }

/** Payment upper-cases coupon codes; doing the same here keeps references matching. */
private fun String?.normalizedCoupon(): String? =
    this
        ?.trim()
        ?.uppercase(Locale.ROOT)
        ?.takeIf(String::isNotEmpty)
