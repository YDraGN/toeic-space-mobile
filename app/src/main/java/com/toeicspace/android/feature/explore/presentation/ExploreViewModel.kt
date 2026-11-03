package com.toeicspace.android.feature.explore.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toeicspace.android.feature.explore.domain.ExploreRepository
import com.toeicspace.android.feature.explore.domain.LearningCenter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ExploreViewModel
    @Inject
    constructor(
        exploreRepository: ExploreRepository,
    ) : ViewModel() {
        val featuredCenters: StateFlow<List<LearningCenter>> =
            exploreRepository.featuredCenters.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList(),
            )
    }
