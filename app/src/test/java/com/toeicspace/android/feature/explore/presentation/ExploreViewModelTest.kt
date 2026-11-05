package com.toeicspace.android.feature.explore.presentation

import com.toeicspace.android.feature.explore.domain.ExploreRepository
import com.toeicspace.android.feature.explore.domain.LearningCenter
import com.toeicspace.android.feature.explore.domain.StudyMode
import com.toeicspace.android.testing.MainDispatcherRule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ExploreViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val online = center(id = "online", modes = setOf(StudyMode.ONLINE))
    private val offline = center(id = "offline", modes = setOf(StudyMode.OFFLINE))
    private val both = center(id = "both", modes = setOf(StudyMode.ONLINE, StudyMode.OFFLINE))

    private val repository =
        object : ExploreRepository {
            override val featuredCenters: Flow<List<LearningCenter>> =
                flowOf(listOf(online, offline, both))
        }

    @Test
    fun `every center shows when no filter is picked`() =
        runTest {
            val viewModel = ExploreViewModel(repository)
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.featuredCenters.collect {}
            }

            assertEquals(null, viewModel.studyModeFilter.value)
            assertEquals(listOf(online, offline, both), viewModel.featuredCenters.value)
        }

    @Test
    fun `a study mode filter keeps the centers that offer it`() =
        runTest {
            val viewModel = ExploreViewModel(repository)
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.featuredCenters.collect {}
            }

            viewModel.onStudyModeFilterChange(StudyMode.OFFLINE)

            assertEquals(listOf(offline, both), viewModel.featuredCenters.value)
        }

    @Test
    fun `clearing the filter brings every center back`() =
        runTest {
            val viewModel = ExploreViewModel(repository)
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.featuredCenters.collect {}
            }
            viewModel.onStudyModeFilterChange(StudyMode.ONLINE)

            viewModel.onStudyModeFilterChange(null)

            assertEquals(3, viewModel.featuredCenters.value.size)
        }

    private fun center(
        id: String,
        modes: Set<StudyMode>,
    ) = LearningCenter(id = id, name = id, location = "", modes = modes, description = "")
}
