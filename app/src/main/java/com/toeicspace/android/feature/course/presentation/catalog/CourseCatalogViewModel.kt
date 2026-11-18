package com.toeicspace.android.feature.course.presentation.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toeicspace.android.core.common.AppError
import com.toeicspace.android.core.common.AppResult
import com.toeicspace.android.core.common.Clock
import com.toeicspace.android.core.data.repository.SessionRepository
import com.toeicspace.android.core.model.SessionState
import com.toeicspace.android.core.model.UserRole
import com.toeicspace.android.feature.course.domain.CourseExamType
import com.toeicspace.android.feature.course.domain.CourseFilter
import com.toeicspace.android.feature.course.domain.CourseLevel
import com.toeicspace.android.feature.course.domain.CourseRepository
import com.toeicspace.android.feature.course.domain.CourseSummary
import com.toeicspace.android.feature.enrollment.domain.EnrollmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Wait this long after the last keystroke before searching, so typing a word sends one request. */
internal const val SEARCH_DEBOUNCE_MILLIS = 400L

/**
 * Coming back to the catalog after this long reloads it quietly, so courses published, repriced or
 * filled up meanwhile show up without a pull to refresh.
 */
internal const val CATALOG_STALE_AFTER_MILLIS = 30_000L

/** Target score choices offered in the filter; the backend takes any number. */
internal val TargetScoreOptions = listOf(450, 650, 800)

/**
 * When owned courses leave fewer than this many on screen, the next page loads by itself: a page
 * made mostly of owned courses would otherwise look like the end of the list.
 */
internal const val MIN_VISIBLE_COURSES = 6

data class CourseCatalogUiState(
    /** What is in the search box; it reaches [filter] after [SEARCH_DEBOUNCE_MILLIS]. */
    val searchQuery: String = "",
    val filter: CourseFilter = CourseFilter(),
    val courses: List<CourseSummary> = emptyList(),
    val totalCount: Int = 0,
    /** The first page is loading and there is nothing to show yet. */
    val isLoading: Boolean = true,
    /** Pull to refresh: the current list stays on screen until the new one arrives. */
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    /** The first page failed; the whole list is replaced by an error with a retry button. */
    val error: AppError? = null,
    /** A later page failed; the loaded courses stay and the footer offers a retry. */
    val loadMoreError: AppError? = null,
    val endReached: Boolean = false,
    /** Courses the signed-in student is enrolled in; the list hides them. Always empty for guests. */
    val ownedIds: Set<String> = emptySet(),
    /** The owned ids (or the session) are still loading, so the list can't tell what to hide yet. */
    val isOwnedLoading: Boolean = false,
    /** The owned ids failed: an error with Retry replaces the list, as on the web. */
    val ownedError: AppError? = null,
) {
    /** What the list shows: the loaded courses minus the owned ones, like the web catalog. */
    val visibleCourses: List<CourseSummary>
        get() = if (ownedIds.isEmpty()) courses else courses.filterNot { it.id in ownedIds }

    /**
     * The count above the list. Owned courses on pages not loaded yet can't be known, so until the
     * last page arrives this is the server total minus the owned courses found so far.
     */
    val visibleCount: Int
        get() =
            (totalCount - (courses.size - visibleCourses.size)).coerceAtLeast(
                visibleCourses.size,
            )

    /** The first page or the owned ids are on their way: the catalog shows its loading state. */
    val isWaiting: Boolean get() = isLoading || isOwnedLoading

    /** Filters chosen in the sheet (level, score), for the badge on the filter button. */
    val sheetFilterCount: Int get() = listOfNotNull(filter.level, filter.targetScore).size

    val hasActiveFilter: Boolean get() = filter != CourseFilter()

    val isEmpty: Boolean
        get() =
            !isWaiting &&
                error == null &&
                ownedError == null &&
                !isLoadingMore &&
                visibleCourses.isEmpty() &&
                (endReached || courses.isEmpty())
}

@HiltViewModel
class CourseCatalogViewModel
    @Inject
    constructor(
        private val courseRepository: CourseRepository,
        private val enrollmentRepository: EnrollmentRepository,
        sessionRepository: SessionRepository,
        private val clock: Clock,
    ) : ViewModel() {
        // Owned ids wait for the session: a student must not glimpse their own courses first.
        private val _uiState = MutableStateFlow(CourseCatalogUiState(isOwnedLoading = true))
        val uiState: StateFlow<CourseCatalogUiState> = _uiState.asStateFlow()

        private var nextPage = 1

        /** When the first page last arrived; 0 until then. */
        private var loadedAtMillis = 0L
        private var loadJob: Job? = null
        private var searchJob: Job? = null
        private var ownedJob: Job? = null

        /** Only students (role User) have owned courses hidden, as on the web. */
        private var isStudent = false

        init {
            loadFirstPage()
            viewModelScope.launch {
                sessionRepository.session
                    .filterIsInstance<SessionState.Ready>()
                    .map { session -> session.user?.takeIf { it.role == UserRole.User }?.id }
                    .distinctUntilChanged()
                    .collect { studentId ->
                        isStudent = studentId != null
                        if (studentId == null) {
                            ownedJob?.cancel()
                            _uiState.update {
                                it.copy(
                                    ownedIds = emptySet(),
                                    isOwnedLoading = false,
                                    ownedError = null,
                                )
                            }
                        } else {
                            loadOwned()
                        }
                    }
            }
        }

        fun onSearchQueryChange(query: String) {
            _uiState.update { it.copy(searchQuery = query) }
            searchJob?.cancel()
            searchJob =
                viewModelScope.launch {
                    delay(SEARCH_DEBOUNCE_MILLIS)
                    applyFilter { copy(search = query.trim()) }
                }
        }

        /** Search now, without waiting for the debounce (the keyboard's search key). */
        fun onSearchSubmit() {
            searchJob?.cancel()
            val query = _uiState.value.searchQuery
            applyFilter { copy(search = query.trim()) }
        }

        fun onExamTypeSelect(examType: CourseExamType?) = applyFilter { copy(examType = examType) }

        fun onLevelSelect(level: CourseLevel?) = applyFilter { copy(level = level) }

        fun onTargetScoreSelect(score: Int?) = applyFilter { copy(targetScore = score) }

        fun onClearFilters() {
            searchJob?.cancel()
            _uiState.update { it.copy(searchQuery = "") }
            applyFilter { CourseFilter() }
        }

        /** Retries what failed: the owned ids, the first page, or both. */
        fun onRetry() {
            val state = _uiState.value
            if (state.ownedError != null) loadOwned()
            if (state.ownedError == null || state.error != null) loadFirstPage()
        }

        fun onRefresh() {
            if (_uiState.value.isLoading) return
            loadFirstPage(refreshing = true)
            if (isStudent) loadOwned(quiet = true)
        }

        /**
         * The tab is shown again (switching tabs, back from a course, or the app returning to the
         * foreground). The tab keeps its state, so without this the list would stay as it was at
         * launch until the learner pulls to refresh.
         */
        fun onResume() {
            val state = _uiState.value
            if (state.isLoading || state.isRefreshing || state.isLoadingMore) return
            if (loadedAtMillis == 0L) return
            if (clock.nowMillis() - loadedAtMillis < CATALOG_STALE_AFTER_MILLIS) return
            loadFirstPage(refreshing = true, quiet = true)
            // A course enrolled in or bought meanwhile leaves the list with this refresh.
            if (isStudent) loadOwned(quiet = true)
        }

        /** Called when the list nears its end. */
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

        /** [quiet]: keeps the ids it has when the refresh fails, instead of showing an error. */
        private fun loadOwned(quiet: Boolean = false) {
            ownedJob?.cancel()
            if (!quiet) _uiState.update { it.copy(isOwnedLoading = true, ownedError = null) }
            ownedJob =
                viewModelScope.launch {
                    when (val result = enrollmentRepository.getOwnedCourseIds()) {
                        is AppResult.Success -> {
                            _uiState.update {
                                it.copy(
                                    ownedIds = result.data,
                                    isOwnedLoading = false,
                                    ownedError = null,
                                )
                            }
                            fillShortList()
                        }
                        is AppResult.Failure ->
                            _uiState.update {
                                if (quiet) {
                                    it.copy(isOwnedLoading = false)
                                } else {
                                    it.copy(
                                        ownedIds = emptySet(),
                                        isOwnedLoading = false,
                                        ownedError = result.error,
                                    )
                                }
                            }
                    }
                }
        }

        /** Owned courses hid most of what is loaded: fetch the next page instead of looking finished. */
        private fun fillShortList() {
            val state = _uiState.value
            val visible = state.visibleCourses.size
            if (visible < state.courses.size && visible < MIN_VISIBLE_COURSES) onLoadMore()
        }

        private fun applyFilter(transform: CourseFilter.() -> CourseFilter) {
            val current = _uiState.value.filter
            val updated = current.transform()
            if (updated == current) return
            _uiState.update { it.copy(filter = updated) }
            loadFirstPage()
        }

        /** [quiet]: a background refresh, without the pull-to-refresh indicator or an error. */
        private fun loadFirstPage(
            refreshing: Boolean = false,
            quiet: Boolean = false,
        ) {
            // A newer filter wins: the answer to an older request must not land on top of it.
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
            val filter = _uiState.value.filter
            loadJob =
                viewModelScope.launch {
                    when (val result = courseRepository.getCourses(filter, page = 1)) {
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
                                    isLoadingMore = false,
                                    error = null,
                                    endReached = !page.hasNextPage,
                                )
                            }
                            fillShortList()
                        }
                        // A failed refresh keeps the list it already has; only an empty screen shows the error.
                        is AppResult.Failure ->
                            _uiState.update {
                                if (refreshing && it.courses.isNotEmpty()) {
                                    it.copy(
                                        isRefreshing = false,
                                        loadMoreError = if (quiet) null else result.error,
                                    )
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

        private fun loadNextPage() {
            _uiState.update { it.copy(isLoadingMore = true) }
            val filter = _uiState.value.filter
            val page = nextPage
            loadJob =
                viewModelScope.launch {
                    when (val result = courseRepository.getCourses(filter, page = page)) {
                        is AppResult.Success -> {
                            nextPage = page + 1
                            _uiState.update { state ->
                                // A course published between two pages shifts the rest down by one, so the
                                // next page can repeat the last course of this one.
                                val known = state.courses.mapTo(HashSet()) { it.id }
                                state.copy(
                                    courses =
                                        state.courses +
                                            result.data.items.filterNot { it.id in known },
                                    totalCount = result.data.totalCount,
                                    isLoadingMore = false,
                                    endReached = !result.data.hasNextPage,
                                )
                            }
                            fillShortList()
                        }
                        is AppResult.Failure ->
                            _uiState.update {
                                it.copy(
                                    isLoadingMore = false,
                                    loadMoreError = result.error,
                                )
                            }
                    }
                }
        }
    }
