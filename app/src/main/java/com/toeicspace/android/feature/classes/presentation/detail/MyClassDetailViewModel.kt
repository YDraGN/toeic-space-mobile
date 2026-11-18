package com.toeicspace.android.feature.classes.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toeicspace.android.core.common.AppError
import com.toeicspace.android.core.common.AppResult
import com.toeicspace.android.feature.classes.domain.StudentClass
import com.toeicspace.android.feature.classes.domain.StudentClassRepository
import com.toeicspace.android.feature.classes.navigation.MyClassDetailDestination
import com.toeicspace.android.feature.classes.presentation.components.isNotFound
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MyClassDetailUiState(
    val studentClass: StudentClass? = null,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: AppError? = null,
) {
    /** Not enrolled any more, or the class was removed: there is nothing to retry. */
    val isNotFound: Boolean get() = error?.isNotFound == true

    val isStale: Boolean get() = studentClass != null && error != null && !isNotFound
}

@HiltViewModel
class MyClassDetailViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val repository: StudentClassRepository,
    ) : ViewModel() {
        // Read by key, not toRoute(), so plain JVM tests can build the handle from a map.
        private val classId: String =
            checkNotNull(savedStateHandle[MyClassDetailDestination.ARG_CLASS_ID]) {
                "MyClassDetailDestination needs a classId"
            }

        private val _uiState = MutableStateFlow(MyClassDetailUiState())
        val uiState: StateFlow<MyClassDetailUiState> = _uiState.asStateFlow()

        init {
            viewModelScope.launch {
                // The row tapped in the list is already known, so the page opens with it filled in.
                repository.cachedClass(classId)?.let { cached ->
                    _uiState.update {
                        it.copy(studentClass = cached, isLoading = false, isRefreshing = true)
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
                if (state.studentClass == null) {
                    state.copy(isLoading = true, error = null)
                } else {
                    state.copy(isRefreshing = true)
                }
            }
            viewModelScope.launch { fetch() }
        }

        private fun isBusy(): Boolean = _uiState.value.let { it.isLoading || it.isRefreshing }

        private suspend fun fetch() {
            when (val result = repository.getMyClass(classId)) {
                is AppResult.Success ->
                    _uiState.update {
                        it.copy(
                            studentClass = result.data,
                            isLoading = false,
                            isRefreshing = false,
                            error = null,
                        )
                    }
                is AppResult.Failure -> {
                    val error = result.error
                    _uiState.update {
                        it.copy(
                            // A class the student left must not stay on screen from the cache.
                            studentClass = if (error.isNotFound) null else it.studentClass,
                            isLoading = false,
                            isRefreshing = false,
                            error = error,
                        )
                    }
                }
            }
        }
    }
