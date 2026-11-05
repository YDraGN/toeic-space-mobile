package com.toeicspace.android.feature.explore.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toeicspace.android.feature.explore.domain.ExploreRepository
import com.toeicspace.android.feature.explore.domain.LearningCenter
import com.toeicspace.android.feature.explore.domain.StudyMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ExploreViewModel
    @Inject
    constructor(
        exploreRepository: ExploreRepository,
    ) : ViewModel() {
        private val _studyModeFilter = MutableStateFlow<StudyMode?>(null)

        /** The study mode the centers are filtered by; null shows every center. */
        val studyModeFilter: StateFlow<StudyMode?> = _studyModeFilter.asStateFlow()

        val featuredCenters: StateFlow<List<LearningCenter>> =
            combine(exploreRepository.featuredCenters, _studyModeFilter) { centers, mode ->
                if (mode == null) centers else centers.filter { mode in it.modes }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList(),
            )

        fun onStudyModeFilterChange(mode: StudyMode?) {
            _studyModeFilter.value = mode
        }
    }
