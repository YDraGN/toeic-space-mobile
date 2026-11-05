package com.toeicspace.android.feature.main.presentation

import com.toeicspace.android.core.common.Clock
import com.toeicspace.android.core.ui.components.BottomNavTab
import com.toeicspace.android.feature.main.navigation.startTab
import com.toeicspace.android.testing.FakeSessionRepository
import com.toeicspace.android.testing.MainDispatcherRule
import com.toeicspace.android.testing.TestUser
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MainViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val clock = Clock { 0L }

    @Test
    fun `signed-in user is a member with name and email`() =
        runTest {
            val viewModel = MainViewModel(FakeSessionRepository(user = TestUser), clock)

            val state = viewModel.uiState.first { it is MainUiState.Ready } as MainUiState.Ready

            assertEquals(
                Viewer.Member(fullName = TestUser.fullName, email = TestUser.email),
                state.viewer,
            )
        }

    @Test
    fun `guest without an account is a guest viewer`() =
        runTest {
            val viewModel = MainViewModel(FakeSessionRepository(isGuest = true), clock)

            val state = viewModel.uiState.first { it is MainUiState.Ready } as MainUiState.Ready

            assertEquals(Viewer.Guest, state.viewer)
        }

    @Test
    fun `signing out ends the session`() =
        runTest {
            val repository = FakeSessionRepository(user = TestUser)
            val viewModel = MainViewModel(repository, clock)
            backgroundScope.launch(
                UnconfinedTestDispatcher(testScheduler),
            ) { viewModel.uiState.collect {} }

            viewModel.onSignOutClick()

            assertEquals(MainUiState.SessionEnded, viewModel.uiState.value)
            assertEquals(1, repository.signOutCount)
        }

    @Test
    fun `session expiring on the server ends the session`() =
        runTest {
            val repository = FakeSessionRepository(user = TestUser)
            val viewModel = MainViewModel(repository, clock)
            backgroundScope.launch(
                UnconfinedTestDispatcher(testScheduler),
            ) { viewModel.uiState.collect {} }
            assertTrue(viewModel.uiState.value is MainUiState.Ready)

            repository.expireSession()

            assertEquals(MainUiState.SessionEnded, viewModel.uiState.value)
        }

    @Test
    fun `guests start on explore and members on home`() {
        assertEquals(BottomNavTab.EXPLORE, Viewer.Guest.startTab)
        assertEquals(BottomNavTab.HOME, Viewer.Member(fullName = "A", email = "a@b.c").startTab)
    }

    @Test
    fun `greeting follows the hour of the day`() {
        assertEquals(TimeOfDay.EVENING, TimeOfDay.fromHour(4))
        assertEquals(TimeOfDay.MORNING, TimeOfDay.fromHour(5))
        assertEquals(TimeOfDay.MORNING, TimeOfDay.fromHour(11))
        assertEquals(TimeOfDay.AFTERNOON, TimeOfDay.fromHour(12))
        assertEquals(TimeOfDay.AFTERNOON, TimeOfDay.fromHour(17))
        assertEquals(TimeOfDay.EVENING, TimeOfDay.fromHour(18))
        assertEquals(TimeOfDay.EVENING, TimeOfDay.fromHour(0))
    }
}
