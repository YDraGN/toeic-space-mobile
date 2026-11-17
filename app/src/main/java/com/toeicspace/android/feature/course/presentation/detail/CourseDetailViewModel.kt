package com.toeicspace.android.feature.course.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toeicspace.android.core.common.AppError
import com.toeicspace.android.core.common.AppResult
import com.toeicspace.android.feature.course.domain.CourseDetail
import com.toeicspace.android.feature.course.domain.CourseRepository
import com.toeicspace.android.feature.course.navigation.CourseDetailDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CourseDetailUiState(
    val isLoading: Boolean = true,
    val course: CourseDetail? = null,
    val error: AppError? = null,
    /** Chapters showing their lessons. The first one opens when the course loads. */
    val expandedModuleIds: Set<String> = emptySet(),
) {
    /** Content answers 404 for a course that does not exist or is not published. */
    val isNotFound: Boolean get() = (error as? AppError.Server)?.status == HTTP_NOT_FOUND
}

private const val HTTP_NOT_FOUND = 404

@HiltViewModel
class CourseDetailViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val courseRepository: CourseRepository,
    ) : ViewModel() {
        // Read by key, not toRoute(), so plain JVM tests can build the handle from a map.
        private val courseId: String =
            checkNotNull(savedStateHandle[CourseDetailDestination.ARG_COURSE_ID]) {
                "CourseDetailDestination needs a courseId"
            }

        private val _uiState = MutableStateFlow(CourseDetailUiState())
        val uiState: StateFlow<CourseDetailUiState> = _uiState.asStateFlow()

        init {
            load()
        }

        fun onRetry() {
            if (_uiState.value.isLoading) return
            load()
        }

        fun onModuleToggle(moduleId: String) {
            _uiState.update { state ->
                val expanded = state.expandedModuleIds
                state.copy(
                    expandedModuleIds =
                        if (moduleId in expanded) expanded - moduleId else expanded + moduleId,
                )
            }
        }

        private fun load() {
            _uiState.update { it.copy(isLoading = true, error = null) }
            viewModelScope.launch {
                when (val result = courseRepository.getCourse(courseId)) {
                    is AppResult.Success ->
                        _uiState.update { state ->
                            val course = result.data
                            state.copy(
                                isLoading = false,
                                course = course,
                                expandedModuleIds =
                                    state.expandedModuleIds.ifEmpty {
                                        setOfNotNull(course.modules.firstOrNull()?.id)
                                    },
                            )
                        }
                    is AppResult.Failure ->
                        _uiState.update { it.copy(isLoading = false, error = result.error) }
                }
            }
        }
    }
