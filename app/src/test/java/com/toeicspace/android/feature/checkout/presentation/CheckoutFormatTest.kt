package com.toeicspace.android.feature.checkout.presentation

import com.toeicspace.android.R
import com.toeicspace.android.core.common.AppError
import org.junit.Assert.assertEquals
import org.junit.Test

class CheckoutFormatTest {
    @Test
    fun `countdown shows minutes and seconds`() {
        assertEquals("15:00", formatCountdown(900))
        assertEquals("00:09", formatCountdown(9))
        assertEquals("00:00", formatCountdown(-3))
    }

    @Test
    fun `payment codes map to their own messages`() {
        assertEquals(
            R.string.checkout_error_payment_unavailable,
            AppError.Server("PAYMENT_NOT_CONFIGURED", 503).checkoutMessageRes(),
        )
        assertEquals(
            R.string.checkout_error_session_expired,
            AppError.Unauthorized.checkoutMessageRes(),
        )
        assertEquals(R.string.common_error_server, AppError.Server(null, 502).checkoutMessageRes())
    }

    @Test
    fun `coupon problems read as coupon messages`() {
        assertEquals(
            R.string.checkout_error_coupon_not_found,
            AppError.Server("NOT_FOUND", 404).couponMessageRes(),
        )
        assertEquals(
            R.string.checkout_error_coupon_not_eligible,
            AppError.Server("CONFLICT", 409).couponMessageRes(),
        )
        assertEquals(R.string.common_error_network, AppError.Network.couponMessageRes())
    }
}
