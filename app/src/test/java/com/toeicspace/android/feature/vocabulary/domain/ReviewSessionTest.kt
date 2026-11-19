package com.toeicspace.android.feature.vocabulary.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/** The device must move through a session exactly like `StudySession.Answer` on the server. */
class ReviewSessionTest {
    private fun session(vararg ids: String) =
        ReviewSession(
            id = "r1",
            source = ReviewSource.DUE,
            queue = ids.toList(),
            position = 0,
            results = emptyMap(),
            correctCount = 0,
            reviewCount = 0,
            words = emptyMap(),
            questions = emptyMap(),
        )

    @Test
    fun `a first wrong answer comes back four cards later`() {
        val next = session("a", "b", "c", "d", "e", "f", "g").answered("a", ReviewGrade.FORGET)

        assertEquals(listOf("a", "b", "c", "d", "e", "a", "f", "g"), next.queue)
        assertEquals(1, next.position)
        assertEquals(0, next.correctCount)
        assertEquals(1, next.reviewCount)
    }

    @Test
    fun `near the end the wrong card goes last, and only once`() {
        var next = session("a", "b").answered("a", ReviewGrade.FORGET)
        assertEquals(listOf("a", "b", "a"), next.queue)

        next = next.answered("b", ReviewGrade.GOOD).answered("a", ReviewGrade.FORGET)

        assertEquals(listOf("a", "b", "a"), next.queue)
        assertTrue(next.isFinished)
        assertEquals(listOf("a"), next.localResult.wordIds)
        assertEquals(1, next.localResult.remembered)
    }

    @Test
    fun `the last grade of a word decides the result`() {
        val next =
            session("a", "b")
                .answered("a", ReviewGrade.FORGET)
                .answered("b", ReviewGrade.GOOD)
                .answered("a", ReviewGrade.GOOD)

        assertEquals(
            ReviewResult(remembered = 2, needReview = 0, wordIds = emptyList()),
            next.localResult,
        )
        assertEquals(2, next.correctCount)
        assertEquals(3, next.reviewCount)
    }

    @Test
    fun `an answer for another card changes nothing`() {
        val start = session("a", "b")

        assertSame(start, start.answered("b", ReviewGrade.GOOD))
    }
}
