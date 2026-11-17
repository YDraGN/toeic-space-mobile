package com.toeicspace.android.feature.course.presentation

import com.toeicspace.android.feature.course.presentation.components.formatCourseAmount
import org.junit.Assert.assertEquals
import org.junit.Test

class CourseAmountFormatTest {
    @Test
    fun `thousands are grouped with dots`() {
        assertEquals("0", formatCourseAmount(0))
        assertEquals("999", formatCourseAmount(999))
        assertEquals("1.000", formatCourseAmount(1_000))
        assertEquals("499.000", formatCourseAmount(499_000))
        assertEquals("12.500.000", formatCourseAmount(12_500_000))
    }

    @Test
    fun `negative amounts keep their sign`() {
        assertEquals("-50.000", formatCourseAmount(-50_000))
    }
}
