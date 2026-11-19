package com.toeicspace.android.testing

import com.toeicspace.android.feature.vocabulary.domain.FlashcardMark
import com.toeicspace.android.feature.vocabulary.domain.FlashcardMarkRepository

/** Keeps marks in memory per set and records every call. */
class FakeFlashcardMarkRepository : FlashcardMarkRepository {
    val marks = mutableMapOf<String, MutableMap<String, FlashcardMark>>()
    val marked = mutableListOf<Pair<String, FlashcardMark>>()
    var flushCount = 0
        private set

    override suspend fun getMarks(setId: String): Map<String, FlashcardMark> =
        marks[setId].orEmpty().toMap()

    override suspend fun mark(
        setId: String,
        wordId: String,
        mark: FlashcardMark,
    ) {
        marks.getOrPut(setId) { mutableMapOf() }[wordId] = mark
        marked += wordId to mark
    }

    override suspend fun flushPending() {
        flushCount++
    }
}
