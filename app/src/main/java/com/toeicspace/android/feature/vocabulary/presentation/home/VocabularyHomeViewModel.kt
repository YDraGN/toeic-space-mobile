package com.toeicspace.android.feature.vocabulary.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toeicspace.android.core.common.AppError
import com.toeicspace.android.core.common.AppResult
import com.toeicspace.android.core.data.repository.SessionRepository
import com.toeicspace.android.core.model.SessionState
import com.toeicspace.android.feature.vocabulary.domain.SetProgress
import com.toeicspace.android.feature.vocabulary.domain.StageProgress
import com.toeicspace.android.feature.vocabulary.domain.VocabularyRepository
import com.toeicspace.android.feature.vocabulary.domain.VocabularySet
import com.toeicspace.android.feature.vocabulary.domain.VocabularyStage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** The sets of one stage, loaded the first time the stage is opened. */
data class StageSetsState(
    val isLoading: Boolean = true,
    val sets: List<VocabularySet> = emptyList(),
    val error: AppError? = null,
    val fromCache: Boolean = false,
)

data class VocabularyHomeUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val stages: List<VocabularyStage> = emptyList(),
    val error: AppError? = null,
    /** The stages came from the device because the network failed. */
    val fromCache: Boolean = false,
    val selectedStageId: String? = null,
    val stageSets: Map<String, StageSetsState> = emptyMap(),
    /** Signed-in learners only: progress per set id, for every stage opened so far. */
    val progress: Map<String, SetProgress> = emptyMap(),
    /** Signed-in learners only: progress per stage id, for the bar on each stage card. */
    val stageProgress: Map<String, StageProgress> = emptyMap(),
    val isSignedIn: Boolean = false,
) {
    val selectedStage: VocabularyStage? get() = stages.firstOrNull { it.id == selectedStageId }

    val selectedSets: StageSetsState? get() = selectedStageId?.let(stageSets::get)

    /** Something on screen is the offline copy, so the page says it may be out of date. */
    val isOffline: Boolean get() = fromCache || selectedSets?.fromCache == true
}

@HiltViewModel
class VocabularyHomeViewModel
    @Inject
    constructor(
        private val repository: VocabularyRepository,
        sessionRepository: SessionRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(VocabularyHomeUiState())
        val uiState: StateFlow<VocabularyHomeUiState> = _uiState.asStateFlow()

        private var setsJob: Job? = null

        init {
            viewModelScope.launch {
                sessionRepository.session
                    .filterIsInstance<SessionState.Ready>()
                    .map { it.isSignedIn }
                    .distinctUntilChanged()
                    .collect { signedIn ->
                        _uiState.update {
                            it.copy(
                                isSignedIn = signedIn,
                                progress = emptyMap(),
                                stageProgress = emptyMap(),
                            )
                        }
                        if (signedIn) {
                            _uiState.value.selectedStageId?.let(::loadProgress)
                            loadStagesProgress()
                        }
                    }
            }
            loadStages(refreshing = false)
        }

        fun onRetry() {
            if (_uiState.value.isLoading) return
            loadStages(refreshing = false)
        }

        fun onRefresh() {
            if (_uiState.value.isLoading || _uiState.value.isRefreshing) return
            loadStages(refreshing = true)
        }

        fun onStageSelect(stageId: String) {
            if (stageId == _uiState.value.selectedStageId) return
            _uiState.update { it.copy(selectedStageId = stageId) }
            val loaded = _uiState.value.stageSets[stageId]
            if (loaded == null || loaded.error != null) loadSets(stageId)
            if (_uiState.value.isSignedIn) loadProgress(stageId)
        }

        fun onRetrySets() {
            _uiState.value.selectedStageId?.let(::loadSets)
        }

        private fun loadStages(refreshing: Boolean) {
            _uiState.update {
                it.copy(isLoading = !refreshing, isRefreshing = refreshing, error = null)
            }
            viewModelScope.launch {
                when (val result = repository.getStages()) {
                    is AppResult.Success -> {
                        val stages = result.data.data
                        _uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                isRefreshing = false,
                                stages = stages,
                                fromCache = result.data.fromCache,
                                selectedStageId =
                                    state.selectedStageId?.takeIf { id ->
                                        stages.any { it.id == id }
                                    }
                                        ?: stages.firstOrNull()?.id,
                                // A refresh reloads the sets too, so word counts stay in step.
                                stageSets = if (refreshing) emptyMap() else state.stageSets,
                            )
                        }
                        _uiState.value.selectedStageId?.let { id ->
                            loadSets(id)
                            if (_uiState.value.isSignedIn) loadProgress(id)
                        }
                        if (refreshing && _uiState.value.isSignedIn) loadStagesProgress()
                    }
                    is AppResult.Failure ->
                        _uiState.update {
                            // A failed refresh keeps what is already on screen.
                            if (refreshing && it.stages.isNotEmpty()) {
                                it.copy(isRefreshing = false)
                            } else {
                                it.copy(
                                    isLoading = false,
                                    isRefreshing = false,
                                    error = result.error,
                                )
                            }
                        }
                }
            }
        }

        private fun loadSets(stageId: String) {
            setsJob?.cancel()
            _uiState.update { state ->
                val previous = state.stageSets[stageId]
                state.copy(
                    stageSets =
                        state.stageSets +
                            (
                                stageId to
                                    (previous ?: StageSetsState()).copy(
                                        isLoading = true,
                                        error = null,
                                    )
                            ),
                )
            }
            setsJob =
                viewModelScope.launch {
                    val next =
                        when (val result = repository.getStageSets(stageId)) {
                            is AppResult.Success ->
                                StageSetsState(
                                    isLoading = false,
                                    sets = result.data.data,
                                    fromCache = result.data.fromCache,
                                )
                            is AppResult.Failure ->
                                StageSetsState(
                                    isLoading = false,
                                    error = result.error,
                                )
                        }
                    _uiState.update { it.copy(stageSets = it.stageSets + (stageId to next)) }
                }
        }

        /** Like set progress, extra: when it fails the stage cards simply show no bar. */
        private fun loadStagesProgress() {
            viewModelScope.launch {
                val result = repository.getStagesProgress()
                if (result is AppResult.Success && _uiState.value.isSignedIn) {
                    _uiState.update { it.copy(stageProgress = result.data) }
                }
            }
        }

        /** Progress is extra: when it fails the sets simply show no progress. */
        private fun loadProgress(stageId: String) {
            viewModelScope.launch {
                val result = repository.getStageProgress(stageId)
                if (result is AppResult.Success && _uiState.value.isSignedIn) {
                    _uiState.update { it.copy(progress = it.progress + result.data) }
                }
            }
        }
    }
