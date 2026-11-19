package com.toeicspace.android.testing

import com.toeicspace.android.core.common.AppError
import com.toeicspace.android.core.common.AppResult
import com.toeicspace.android.feature.vocabulary.domain.Fetched
import com.toeicspace.android.feature.vocabulary.domain.PartOfSpeech
import com.toeicspace.android.feature.vocabulary.domain.SetProgress
import com.toeicspace.android.feature.vocabulary.domain.StageProgress
import com.toeicspace.android.feature.vocabulary.domain.VocabularyRepository
import com.toeicspace.android.feature.vocabulary.domain.VocabularySet
import com.toeicspace.android.feature.vocabulary.domain.VocabularySetDetail
import com.toeicspace.android.feature.vocabulary.domain.VocabularyStage
import com.toeicspace.android.feature.vocabulary.domain.VocabularyWord

fun testStage(
    id: String,
    order: Int = 1,
) = VocabularyStage(
    id = id,
    title = "Stage $id",
    description = null,
    order = order,
    toeicTarget = 250 * order,
    setCount = 2,
    wordCount = 100,
)

fun testSet(
    id: String,
    stageId: String = "s1",
    order: Int = 1,
) = VocabularySet(
    id = id,
    stageId = stageId,
    title = "Set $id",
    order = order,
    wordCount = 50,
    previewWords = listOf("a", "b", "c"),
    lastWord = "z",
)

fun testWord(id: String) =
    VocabularyWord(
        id = id,
        headword = "word-$id",
        phonetic = "/$id/",
        partOfSpeech = PartOfSpeech.NOUN,
        meaningVi = "nghĩa $id",
        definitionEn = null,
        example = "Example $id.",
        exampleVi = null,
    )

fun testSetDetail(
    id: String = "set1",
    wordCount: Int = 3,
) = VocabularySetDetail(
    id = id,
    stageId = "s1",
    stageTitle = "Stage s1",
    title = "Set $id",
    order = 1,
    previousSetId = null,
    nextSetId = null,
    words = (1..wordCount).map { testWord("w$it") },
)

/** Answers from the fields below; set a field to a failure to make that call fail. */
class FakeVocabularyRepository : VocabularyRepository {
    var stages: AppResult<Fetched<List<VocabularyStage>>> =
        AppResult.Success(Fetched(listOf(testStage("s1", 1), testStage("s2", 2))))
    val stageSets =
        mutableMapOf<String, AppResult<Fetched<List<VocabularySet>>>>(
            "s1" to AppResult.Success(Fetched(listOf(testSet("set1"), testSet("set2", order = 2)))),
            "s2" to AppResult.Success(Fetched(listOf(testSet("set3", stageId = "s2")))),
        )
    var set: AppResult<Fetched<VocabularySetDetail>> = AppResult.Success(Fetched(testSetDetail()))
    val progress =
        mutableMapOf<String, AppResult<Map<String, SetProgress>>>(
            "s1" to AppResult.Success(mapOf("set1" to SetProgress("set1", 50, 10, 5, 2))),
        )

    var stagesProgress: AppResult<Map<String, StageProgress>> =
        AppResult.Success(mapOf("s1" to StageProgress("s1", 100, 20, 10, 2, 1)))
    var stagesProgressRequests = 0
        private set

    val stageSetRequests = mutableListOf<String>()
    val progressRequests = mutableListOf<String>()
    var stageRequests = 0
        private set

    override suspend fun getStages(): AppResult<Fetched<List<VocabularyStage>>> {
        stageRequests++
        return stages
    }

    override suspend fun getStageSets(stageId: String): AppResult<Fetched<List<VocabularySet>>> {
        stageSetRequests += stageId
        return stageSets[stageId] ?: AppResult.Failure(AppError.Server(code = null, status = 404))
    }

    override suspend fun getSet(setId: String): AppResult<Fetched<VocabularySetDetail>> = set

    override suspend fun getStageProgress(stageId: String): AppResult<Map<String, SetProgress>> {
        progressRequests += stageId
        return progress[stageId] ?: AppResult.Success(emptyMap())
    }

    override suspend fun getStagesProgress(): AppResult<Map<String, StageProgress>> {
        stagesProgressRequests++
        return stagesProgress
    }
}
