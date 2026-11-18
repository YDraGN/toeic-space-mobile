package com.toeicspace.android.feature.classes.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toeicspace.android.core.common.AppError
import com.toeicspace.android.core.common.AppResult
import com.toeicspace.android.feature.classes.domain.StudentClass
import com.toeicspace.android.feature.classes.domain.StudentClassRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MyClassesUiState(
    /** Null until something is loaded, from the network or from what was loaded last. */
    val classes: List<StudentClass>? = null,
    /** First load with nothing to show yet. */
    val isLoading: Boolean = true,
    /** Pull to refresh, or a retry while a list is already on screen. */
    val isRefreshing: Boolean = false,
    /** The last load failed. With [classes] still set, the list shown is the one loaded before. */
    val error: AppError? = null,
) {
    val isEmpty: Boolean get() = classes?.isEmpty() == true

    /** Showing the list loaded earlier because the latest load failed (offline, server down). */
    val isStale: Boolean get() = classes != null && error != null
}

@HiltViewModel
class MyClassesViewModel
    @Inject
    constructor(
        private val repository: StudentClassRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(MyClassesUiState())
        val uiState: StateFlow<MyClassesUiState> = _uiState.asStateFlow()

        init {
            viewModelScope.launch {
                // What was loaded last shows at once; the fresh list replaces it when it arrives.
                repository.cachedClasses()?.let { cached ->
                    _uiState.update {
                        it.copy(
                            classes = cached,
                            isLoading = false,
                            isRefreshing = true,
                        )
                    }
                }
                fetch()
            }
        }

        fun onRefresh() {
            if (isBusy()) return
            _uiState.update { it.copy(isRefreshing = true) }
            viewModelScope.launch { fetch() }
        }

        fun onRetry() {
            if (isBusy()) return
            _uiState.update { state ->
                if (state.classes == null) {
                    state.copy(isLoading = true, error = null)
                } else {
                    state.copy(isRefreshing = true)
                }
            }
            viewModelScope.launch { fetch() }
        }

        private fun isBusy(): Boolean = _uiState.value.let { it.isLoading || it.isRefreshing }

        private suspend fun fetch() {
            when (val result = repository.getMyClasses()) {
                is AppResult.Success ->
                    _uiState.update {
                        it.copy(
                            classes = result.data,
                            isLoading = false,
                            isRefreshing = false,
                            error = null,
                        )
                    }
                is AppResult.Failure ->
                    _uiState.update {
                        it.copy(isLoading = false, isRefreshing = false, error = result.error)
                    }
            }
        }
    }
