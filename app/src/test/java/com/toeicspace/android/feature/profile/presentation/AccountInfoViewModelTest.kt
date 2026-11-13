package com.toeicspace.android.feature.profile.presentation

import com.toeicspace.android.core.common.AppError
import com.toeicspace.android.core.common.AppResult
import com.toeicspace.android.feature.auth.domain.FieldError
import com.toeicspace.android.feature.profile.domain.EnglishLevel
import com.toeicspace.android.feature.profile.domain.UserProfile
import com.toeicspace.android.testing.FakeProfileRepository
import com.toeicspace.android.testing.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
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

    private fun viewModel(repository: FakeProfileRepository = FakeProfileRepository(profile)) =
        AccountInfoViewModel(repository)

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
            assertFalse(state.isSaving)
            assertNull(state.message)
        }

    @Test
    fun `opening the screen fetches the full profile`() =
        runTest {
            val repository = FakeProfileRepository(profile)

            viewModel(repository)

            assertEquals(1, repository.refreshCount)
        }

    @Test
    fun `a valid form is saved, then the form closes and says so`() =
        runTest {
            val repository = FakeProfileRepository(profile)
            val viewModel = viewModel(repository)
            viewModel.onEditClick()
            viewModel.onFullNameChange("  Minh Anh Tran  ")
            viewModel.onPhoneChange("")
            viewModel.onTargetScoreChange(800)

            viewModel.onSaveClick()

            val sent = repository.updatedProfile
            assertEquals("Minh Anh Tran", sent?.fullName)
            assertNull(sent?.phone)
            assertEquals(800, sent?.targetScore)
            assertEquals(profile.email, sent?.email)
            val state = viewModel.uiState.value
            assertNull(state.form)
            assertFalse(state.isSaving)
            assertEquals(ProfileMessage.Saved, state.message)
            assertEquals(800, state.profile?.targetScore)

            viewModel.onMessageShown()
            assertNull(viewModel.uiState.value.message)
        }

    @Test
    fun `a failed save keeps the form open with what was typed`() =
        runTest {
            val repository =
                FakeProfileRepository(profile).apply {
                    updateResult = AppResult.Failure(AppError.Network)
                }
            val viewModel = viewModel(repository)
            viewModel.onEditClick()
            viewModel.onTargetScoreChange(900)

            viewModel.onSaveClick()

            val state = viewModel.uiState.value
            assertEquals(900, state.form?.targetScore)
            assertFalse(state.isSaving)
            assertEquals(ProfileMessage.SaveFailed(AppError.Network), state.message)
            assertEquals(650, state.profile?.targetScore)
        }

    @Test
    fun `a picked photo is uploaded and the new avatar shown`() =
        runTest {
            val repository = FakeProfileRepository(profile)
            val viewModel = viewModel(repository)

            viewModel.onAvatarPicked("content://picked/1")

            assertEquals("content://picked/1", repository.uploadedImageUri)
            val state = viewModel.uiState.value
            assertEquals(ProfileMessage.AvatarSaved, state.message)
            assertEquals("https://cdn.test/avatars/user-1?v=1", state.profile?.avatarUrl)
        }

    @Test
    fun `a failed avatar upload says why and keeps the old photo`() =
        runTest {
            val repository =
                FakeProfileRepository(profile).apply {
                    avatarResult = AppResult.Failure(AppError.Network)
                }
            val viewModel = viewModel(repository)

            viewModel.onAvatarPicked("content://picked/1")

            val state = viewModel.uiState.value
            assertEquals(ProfileMessage.AvatarFailed(AppError.Network), state.message)
            assertNull(state.profile?.avatarUrl)
        }

    @Test
    fun `no second upload starts while one is running`() =
        runTest {
            val repository = FakeProfileRepository(profile).apply { uploadingAvatar.value = true }
            val viewModel = viewModel(repository)

            viewModel.onAvatarPicked("content://picked/1")

            assertNull(repository.uploadedImageUri)
        }

    @Test
    fun `guests have no profile to show`() =
        runTest {
            val state = viewModel(FakeProfileRepository(null)).uiState.value

            assertFalse(state.isLoading)
            assertNull(state.profile)
        }
}
