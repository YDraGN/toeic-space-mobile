package com.toeicspace.android.feature.vocabulary.domain

import com.toeicspace.android.core.common.AppResult

/**
 * The public roadmap: anyone can browse and study it. Every successful read is saved on the
 * device, and a read that fails for lack of network returns that copy instead
 * ([Fetched.fromCache]).
 */
interface VocabularyRepository {
    suspend fun getStages(): AppResult<Fetched<List<VocabularyStage>>>

    suspend fun getStageSets(stageId: String): AppResult<Fetched<List<VocabularySet>>>

    /** A set that is hidden or does not exist fails with a 404 server error. */
    suspend fun getSet(setId: String): AppResult<Fetched<VocabularySetDetail>>

    /**
     * Signed-in only: the learner's progress in the sets of one stage, keyed by set id. Sets
     * without progress are missing from the map.
     */
    suspend fun getStageProgress(stageId: String): AppResult<Map<String, SetProgress>>

    /** Signed-in only: the learner's progress in every stage, keyed by stage id. */
    suspend fun getStagesProgress(): AppResult<Map<String, StageProgress>>
}
