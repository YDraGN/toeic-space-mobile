package com.toeicspace.android.core.ui.components

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class StreakWeekTest {
    private val wednesday = LocalDate.of(2026, 11, 4)
    private val monday = LocalDate.of(2026, 11, 2)

    @Test
    fun `no streak marks only today`() {
        val week = streakWeek(wednesday)

        assertEquals(StreakDay.TODAY, week[2])
        assertEquals(6, week.count { it == StreakDay.UPCOMING })
    }

    @Test
    fun `the streak fills the days right before today`() {
        val week = streakWeek(wednesday, streakDays = 2)

        assertEquals(
            listOf(
                StreakDay.DONE,
                StreakDay.DONE,
                StreakDay.TODAY,
            ) + List(4) { StreakDay.UPCOMING },
            week,
        )
    }

    @Test
    fun `days of the streak in last week are not drawn`() {
        val week = streakWeek(monday, streakDays = 2)

        assertEquals(StreakDay.TODAY, week.first())
        assertEquals(0, week.count { it == StreakDay.DONE })
    }
}
