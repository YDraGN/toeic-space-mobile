package com.toeicspace.android.core.analytics

import org.junit.Assert.assertEquals
import org.junit.Test

class ScreenTrackingTest {
    @Test
    fun `screen names drop the package and the arguments`() {
        assertEquals(
            "CourseDetailDestination",
            screenNameOf(
                "com.toeicspace.android.feature.course.navigation.CourseDetailDestination/{courseId}",
            ),
        )
        assertEquals(
            "SignInDestination",
            screenNameOf(
                "com.toeicspace.android.feature.auth.navigation.SignInDestination?email={email}",
            ),
        )
        assertEquals(
            "HomeTabRoute",
            screenNameOf("com.toeicspace.android.feature.main.navigation.HomeTabRoute"),
        )
    }

    @Test
    fun `purchase carries the amount as a double for Firebase revenue`() {
        val event =
            AnalyticsEvent.purchase(
                courseId = "c1",
                orderCode = "TS1",
                amount = 299_000,
                currency = "VND",
            )

        assertEquals("purchase", event.name)
        assertEquals(299_000.0, event.params[AnalyticsEvent.Params.VALUE])
        assertEquals("VND", event.params[AnalyticsEvent.Params.CURRENCY])
        assertEquals("TS1", event.params[AnalyticsEvent.Params.TRANSACTION_ID])
    }
}
