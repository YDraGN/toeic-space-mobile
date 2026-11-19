package com.toeicspace.android.feature.vocabulary.presentation.set

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toeicspace.android.core.common.AppError
import com.toeicspace.android.core.common.AppResult
import com.toeicspace.android.feature.vocabulary.domain.VocabularyRepository
import com.toeicspace.android.feature.vocabulary.domain.VocabularySetDetail
import com.toeicspace.android.feature.vocabulary.navigation.VocabularySetDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VocabularySetUiState(
    val isLoading: Boolean = true,
    val set: VocabularySetDetail? = null,
    val error: AppError? = null,
    val fromCache: Boolean = false,
) {
    /** Content answers 404 for a set that is hidden or does not exist. */
    val isNotFound: Boolean get() = (error as? AppError.Server)?.status == HTTP_NOT_FOUND
}

internal const val HTTP_NOT_FOUND = 404

@HiltViewModel
class VocabularySetViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val repository: VocabularyRepository,
    ) : ViewModel() {
        // Read by key, not toRoute(), so plain JVM tests can build the handle from a map.
        private val setId: String =
            checkNotNull(savedStateHandle[VocabularySetDestination.ARG_SET_ID]) {
                "VocabularySetDestination needs a setId"
            }

        private val _uiState = MutableStateFlow(VocabularySetUiState())
        val uiState: StateFlow<VocabularySetUiState> = _uiState.asStateFlow()

        init {
            load()
        }

        fun onRetry() {
            if (_uiState.value.isLoading) return
            load()
        }

        private fun load() {
            _uiState.update { it.copy(isLoading = true, error = null) }
            viewModelScope.launch {
                when (val result = repository.getSet(setId)) {
                    is AppResult.Success ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                set = result.data.data,
                                fromCache = result.data.fromCache,
                            )
                        }
                    is AppResult.Failure ->
                        _uiState.update { it.copy(isLoading = false, error = result.error) }
                }
            }
        }
    }
