package com.toeicspace.android.testing

import com.toeicspace.android.core.common.AppResult
import com.toeicspace.android.feature.vocabulary.domain.Fetched
import com.toeicspace.android.feature.vocabulary.domain.QuizOption
import com.toeicspace.android.feature.vocabulary.domain.QuizQuestion
import com.toeicspace.android.feature.vocabulary.domain.ReviewGrade
import com.toeicspace.android.feature.vocabulary.domain.ReviewRepository
import com.toeicspace.android.feature.vocabulary.domain.ReviewResult
import com.toeicspace.android.feature.vocabulary.domain.ReviewSession
import com.toeicspace.android.feature.vocabulary.domain.ReviewSource
import com.toeicspace.android.feature.vocabulary.domain.ReviewSummary
import kotlinx.coroutines.flow.MutableStateFlow

/** A session over [wordIds] where each question offers its own word and "decoy". */
fun testReviewSession(
    vararg wordIds: String,
    source: ReviewSource = ReviewSource.DUE,
) = ReviewSession(
    id = "r1",
    source = source,
    queue = wordIds.toList(),
    position = 0,
    results = emptyMap(),
    correctCount = 0,
    reviewCount = 0,
    words = wordIds.associateWith { testWord(it) },
    questions =
        wordIds.associateWith {
            QuizQuestion(
                wordId = it,
                prompt = "nghĩa $it",
                partOfSpeech = testWord(it).partOfSpeech,
                options = listOf(QuizOption(it, "word-$it"), QuizOption("decoy", "decoy")),
            )
        },
)

class FakeReviewRepository : ReviewRepository {
    override val pendingAnswerCount = MutableStateFlow(0)

    var summary: AppResult<Fetched<ReviewSummary>> =
        AppResult.Success(
            Fetched(
                ReviewSummary(
                    due = 5,
                    reviewedToday = 2,
                    dailyGoal = 20,
                    streakDays = 3,
                    mistakes = 1,
                    learned = 40,
                    activeReview = null,
                ),
            ),
        )
    var start: AppResult<ReviewSession> = AppResult.Success(testReviewSession("w1", "w2"))
    var complete: AppResult<ReviewResult>? = null

    val starts = mutableListOf<Pair<ReviewSource, List<String>>>()
    val answers = mutableListOf<Pair<String, ReviewGrade>>()
    var summaryRequests = 0
        private set
    var completeRequests = 0
        private set

    override suspend fun getSummary(): AppResult<Fetched<ReviewSummary>> {
        summaryRequests++
        return summary
    }

    override suspend fun startSession(
        source: ReviewSource,
        wordIds: List<String>,
    ): AppResult<ReviewSession> {
        starts += source to wordIds
        return start
    }

    override suspend fun answer(
        session: ReviewSession,
        wordId: String,
        grade: ReviewGrade,
    ): ReviewSession {
        answers += wordId to grade
        return session.answered(wordId, grade)
    }

    override suspend fun flushPendingAnswers(): AppResult<Unit> = AppResult.Success(Unit)

    /** Uses [complete] when set, else the session's own result. */
    override suspend fun completeSession(session: ReviewSession): AppResult<ReviewResult> {
        completeRequests++
        return complete ?: AppResult.Success(session.localResult)
    }
}
