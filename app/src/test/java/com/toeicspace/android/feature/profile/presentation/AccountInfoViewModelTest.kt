package com.toeicspace.android.feature.profile.presentation

import com.toeicspace.android.feature.auth.domain.FieldError
import com.toeicspace.android.feature.profile.domain.EnglishLevel
import com.toeicspace.android.feature.profile.domain.ProfileRepository
import com.toeicspace.android.feature.profile.domain.UserProfile
import com.toeicspace.android.testing.MainDispatcherRule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AccountInfoViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val profile =
        UserProfile(
            fullName = "Minh Anh",
            email = "minhanh@example.com",
            phone = "0905123456",
            targetScore = 650,
            currentLevel = EnglishLevel.INTERMEDIATE,
        )

    private fun viewModel(profile: UserProfile? = this.profile) =
        AccountInfoViewModel(
            object : ProfileRepository {
                override val profile: Flow<UserProfile?> = flowOf(profile)
            },
        )

    @Test
    fun `opens in view mode with the profile`() =
        runTest {
            val state = viewModel().uiState.value

            assertFalse(state.isLoading)
            assertEquals(profile, state.profile)
            assertNull(state.form)
        }

    @Test
    fun `edit fills the form from the profile and cancel closes it`() =
        runTest {
            val viewModel = viewModel()

            viewModel.onEditClick()
            val form = viewModel.uiState.value.form
            assertEquals("Minh Anh", form?.fullName)
            assertEquals(650, form?.targetScore)

            viewModel.onCancelEdit()
            assertNull(viewModel.uiState.value.form)
        }

    @Test
    fun `invalid fields block saving and show their errors`() =
        runTest {
            val viewModel = viewModel()
            viewModel.onEditClick()
            viewModel.onFullNameChange("")
            viewModel.onPhoneChange("123")

            viewModel.onSaveClick()

            val state = viewModel.uiState.value
            assertEquals(FieldError.Required, state.form?.fullNameError)
            assertEquals(FieldError.InvalidPhone, state.form?.phoneError)
            assertFalse(state.isSaveRequested)
        }

    @Test
    fun `a valid form is handed over for saving and stays open`() =
        runTest {
            val viewModel = viewModel()
            viewModel.onEditClick()
            viewModel.onTargetScoreChange(800)

            viewModel.onSaveClick()
            assertTrue(viewModel.uiState.value.isSaveRequested)

            viewModel.onSaveHandled()
            assertFalse(viewModel.uiState.value.isSaveRequested)
            val form = viewModel.uiState.value.form
            assertEquals(800, form?.targetScore)
        }

    @Test
    fun `guests have no profile to show`() =
        runTest {
            val state = viewModel(profile = null).uiState.value

            assertFalse(state.isLoading)
            assertNull(state.profile)
        }
}
