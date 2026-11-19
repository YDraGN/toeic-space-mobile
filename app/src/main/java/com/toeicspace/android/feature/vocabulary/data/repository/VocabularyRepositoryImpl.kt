package com.toeicspace.android.feature.vocabulary.data.repository

import com.toeicspace.android.core.common.AppError
import com.toeicspace.android.core.common.AppResult
import com.toeicspace.android.core.common.map
import com.toeicspace.android.core.network.safeApiCall
import com.toeicspace.android.feature.vocabulary.data.local.VocabularyLocalStore
import com.toeicspace.android.feature.vocabulary.data.remote.SetDetailDto
import com.toeicspace.android.feature.vocabulary.data.remote.SetSummaryDto
import com.toeicspace.android.feature.vocabulary.data.remote.StageDto
import com.toeicspace.android.feature.vocabulary.data.remote.VocabularyApiService
import com.toeicspace.android.feature.vocabulary.data.remote.toDomain
import com.toeicspace.android.feature.vocabulary.domain.Fetched
import com.toeicspace.android.feature.vocabulary.domain.SetProgress
import com.toeicspace.android.feature.vocabulary.domain.StageProgress
import com.toeicspace.android.feature.vocabulary.domain.VocabularyRepository
import com.toeicspace.android.feature.vocabulary.domain.VocabularySet
import com.toeicspace.android.feature.vocabulary.domain.VocabularySetDetail
import com.toeicspace.android.feature.vocabulary.domain.VocabularyStage
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

/** Minutes east of UTC on this device right now, the `tzOffset` the backend counts days with. */
internal fun currentTzOffsetMinutes(): Int =
    ZoneId
        .systemDefault()
        .rules
        .getOffset(Instant.now())
        .totalSeconds / SECONDS_PER_MINUTE

private const val SECONDS_PER_MINUTE = 60

/** True for failures where the last saved copy is worth showing instead of an error. */
internal val AppError.isConnectivity: Boolean
    get() = this == AppError.Network || this == AppError.ServerUnreachable

/**
 * Network first; each success is saved on the device and each connectivity failure falls back to
 * that copy. Server errors (a removed set) are passed on, so a stale copy never hides them.
 */
internal class VocabularyRepositoryImpl
    @Inject
    constructor(
        private val api: VocabularyApiService,
        private val store: VocabularyLocalStore,
        private val json: Json,
    ) : VocabularyRepository {
        override suspend fun getStages(): AppResult<Fetched<List<VocabularyStage>>> =
            cached(KEY_STAGES, ListSerializer(StageDto.serializer())) { api.getStages() }
                .map { fetched ->
                    Fetched(
                        fetched.data.map { it.toDomain() }.sortedBy { it.order },
                        fetched.fromCache,
                    )
                }

        override suspend fun getStageSets(
            stageId: String,
        ): AppResult<Fetched<List<VocabularySet>>> =
            cached("$KEY_STAGE_SETS$stageId", ListSerializer(SetSummaryDto.serializer())) {
                api.getStageSets(stageId)
            }.map { fetched ->
                Fetched(fetched.data.map { it.toDomain() }.sortedBy { it.order }, fetched.fromCache)
            }

        override suspend fun getSet(setId: String): AppResult<Fetched<VocabularySetDetail>> =
            cached("$KEY_SET$setId", SetDetailDto.serializer()) { api.getSet(setId) }
                .map { fetched -> Fetched(fetched.data.toDomain(), fetched.fromCache) }

        override suspend fun getStageProgress(
            stageId: String,
        ): AppResult<Map<String, SetProgress>> =
            safeApiCall(json) { api.getStageProgress(stageId, currentTzOffsetMinutes()) }
                .map { list -> list.map { it.toDomain() }.associateBy { it.setId } }

        override suspend fun getStagesProgress(): AppResult<Map<String, StageProgress>> =
            safeApiCall(json) { api.getSummaryStages(currentTzOffsetMinutes()) }
                .map { summary -> summary.stages.map { it.toDomain() }.associateBy { it.stageId } }

        private suspend fun <T> cached(
            key: String,
            serializer: KSerializer<T>,
            call: suspend () -> T,
        ): AppResult<Fetched<T>> =
            when (val result = safeApiCall(json, call)) {
                is AppResult.Success -> {
                    store.write(key, serializer, result.data)
                    AppResult.Success(Fetched(result.data))
                }
                is AppResult.Failure -> {
                    val saved =
                        if (result.error.isConnectivity) {
                            store.read(
                                key,
                                serializer,
                            )
                        } else {
                            null
                        }
                    if (saved !=
                        null
                    ) {
                        AppResult.Success(Fetched(saved, fromCache = true))
                    } else {
                        result
                    }
                }
            }

        private companion object {
            const val KEY_STAGES = "stages"
            const val KEY_STAGE_SETS = "stage_sets_"
            const val KEY_SET = "set_"
        }
    }
