package com.toeicspace.android.feature.enrollment.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toeicspace.android.core.common.AppError
import com.toeicspace.android.core.common.AppResult
import com.toeicspace.android.core.common.Clock
import com.toeicspace.android.feature.enrollment.domain.EnrollmentRepository
import com.toeicspace.android.feature.enrollment.domain.MyCourse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Wait this long after the last keystroke before searching. */
internal const val MY_COURSES_SEARCH_DEBOUNCE_MILLIS = 400L

/**
 * Covers are signed for five minutes. A cover that fails to load sooner than this after the list
 * arrived is a real failure, not an expired link, so it does not trigger a refetch.
 */
internal const val COVER_REFRESH_MIN_AGE_MILLIS = 60_000L

data class MyCoursesUiState(
    /** What is in the search box; it reaches [search] after the debounce. */
    val searchQuery: String = "",
    val search: String = "",
    val courses: List<MyCourse> = emptyList(),
    val totalCount: Int = 0,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    /** The first page failed: the list is replaced by an error with a retry button. */
    val error: AppError? = null,
    /** A later page failed: the loaded courses stay and the footer offers a retry. */
    val loadMoreError: AppError? = null,
    val endReached: Boolean = false,
) {
    val isEmpty: Boolean get() = !isLoading && error == null && courses.isEmpty()

    val isSearching: Boolean get() = search.isNotBlank()
}

/** "Khoá học của tôi": the signed-in learner's courses, newest enrollment first. */
@HiltViewModel
class MyCoursesViewModel
    @Inject
    constructor(
        private val enrollmentRepository: EnrollmentRepository,
        private val clock: Clock,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(MyCoursesUiState())
        val uiState: StateFlow<MyCoursesUiState> = _uiState.asStateFlow()

        private var nextPage = 1
        private var loadedAtMillis = 0L
        private var loadJob: Job? = null
        private var searchJob: Job? = null

        init {
            loadFirstPage()
        }

        fun onSearchQueryChange(query: String) {
            _uiState.update { it.copy(searchQuery = query) }
            searchJob?.cancel()
            searchJob =
                viewModelScope.launch {
                    delay(MY_COURSES_SEARCH_DEBOUNCE_MILLIS)
                    applySearch(query)
                }
        }

        fun onSearchSubmit() {
            searchJob?.cancel()
            applySearch(_uiState.value.searchQuery)
        }

        fun onClearSearch() {
            searchJob?.cancel()
            _uiState.update { it.copy(searchQuery = "") }
            applySearch("")
        }

        fun onRetry() = loadFirstPage()

        fun onRefresh() {
            if (_uiState.value.isLoading) return
            loadFirstPage(refreshing = true)
        }

        fun onLoadMore() {
            val state = _uiState.value
            if (state.isLoading || state.isRefreshing || state.isLoadingMore) return
            if (state.endReached || state.error != null || state.loadMoreError != null) return
            loadNextPage()
        }

        fun onRetryLoadMore() {
            if (_uiState.value.isLoadingMore) return
            _uiState.update { it.copy(loadMoreError = null) }
            loadNextPage()
        }

        /** A cover failed to load: its signed link has probably expired, so fetch fresh ones. */
        fun onCoverError() {
            val state = _uiState.value
            if (state.isLoading || state.isRefreshing || state.isLoadingMore) return
            if (clock.nowMillis() - loadedAtMillis < COVER_REFRESH_MIN_AGE_MILLIS) return
            // Marked now, so the other covers failing at the same moment do not each refetch.
            loadedAtMillis = clock.nowMillis()
            loadFirstPage(refreshing = true, quiet = true)
        }

        private fun applySearch(query: String) {
            val search = query.trim()
            if (search == _uiState.value.search) return
            _uiState.update { it.copy(search = search) }
            loadFirstPage()
        }

        /** [quiet]: a background refresh, without the pull-to-refresh indicator. */
        private fun loadFirstPage(
            refreshing: Boolean = false,
            quiet: Boolean = false,
        ) {
            loadJob?.cancel()
            _uiState.update {
                if (refreshing) {
                    it.copy(isRefreshing = !quiet, loadMoreError = null)
                } else {
                    it.copy(
                        isLoading = true,
                        courses = emptyList(),
                        totalCount = 0,
                        error = null,
                        loadMoreError = null,
                        isLoadingMore = false,
                        endReached = false,
                    )
                }
            }
            val search = _uiState.value.search
            loadJob =
                viewModelScope.launch {
                    when (val result = enrollmentRepository.getMyCourses(search, page = 1)) {
                        is AppResult.Success -> {
                            val page = result.data
                            nextPage = 2
                            loadedAtMillis = clock.nowMillis()
                            _uiState.update {
                                it.copy(
                                    courses = page.items,
                                    totalCount = page.totalCount,
                                    isLoading = false,
                                    isRefreshing = false,
                                    error = null,
                                    endReached = !page.hasNextPage,
                                )
                            }
                        }
                        is AppResult.Failure ->
                            _uiState.update {
                                when {
                                    // A failed background refresh changes nothing on screen.
                                    quiet -> it
                                    refreshing && it.courses.isNotEmpty() ->
                                        it.copy(isRefreshing = false, loadMoreError = result.error)
                                    else ->
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

        private fun loadNextPage() {
            _uiState.update { it.copy(isLoadingMore = true) }
            val search = _uiState.value.search
            val page = nextPage
            loadJob =
                viewModelScope.launch {
                    when (val result = enrollmentRepository.getMyCourses(search, page = page)) {
                        is AppResult.Success -> {
                            nextPage = page + 1
                            _uiState.update { state ->
                                // A course enrolled between two pages shifts the rest down by one.
                                val known = state.courses.mapTo(HashSet()) { it.courseId }
                                state.copy(
                                    courses =
                                        state.courses +
                                            result.data.items.filterNot { it.courseId in known },
                                    totalCount = result.data.totalCount,
                                    isLoadingMore = false,
                                    endReached = !result.data.hasNextPage,
                                )
                            }
                        }
                        is AppResult.Failure ->
                            _uiState.update {
                                it.copy(isLoadingMore = false, loadMoreError = result.error)
                            }
                    }
                }
        }
    }
