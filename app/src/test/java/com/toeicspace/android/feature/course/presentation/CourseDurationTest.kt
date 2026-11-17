package com.toeicspace.android.feature.course.presentation

import com.toeicspace.android.feature.course.presentation.detail.formatLessonDuration
import com.toeicspace.android.feature.course.presentation.detail.splitHoursMinutes
import org.junit.Assert.assertEquals
import org.junit.Test

class CourseDurationTest {
    @Test
    fun `lesson durations read as minutes and seconds`() {
        assertEquals("0:00", formatLessonDuration(0))
        assertEquals("0:45", formatLessonDuration(45))
        assertEquals("12:30", formatLessonDuration(750))
    }

    @Test
    fun `lessons over an hour show the hours`() {
        assertEquals("1:05:09", formatLessonDuration(3909))
    }

    @Test
    fun `a negative duration reads as zero`() {
        assertEquals("0:00", formatLessonDuration(-5))
    }

    @Test
    fun `totals split into hours and minutes, rounded to the minute`() {
        assertEquals(8 to 30, splitHoursMinutes(8 * 3600 + 30 * 60))
        assertEquals(0 to 1, splitHoursMinutes(31))
        assertEquals(0 to 0, splitHoursMinutes(29))
        assertEquals(2 to 0, splitHoursMinutes(7199))
    }
}
