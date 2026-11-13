package com.toeicspace.android.testing

import com.toeicspace.android.core.common.AppResult
import com.toeicspace.android.feature.profile.domain.ProfileRepository
import com.toeicspace.android.feature.profile.domain.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeProfileRepository(
    initial: UserProfile?,
) : ProfileRepository {
    private val state = MutableStateFlow(initial)
    override val profile: Flow<UserProfile?> = state

    var refreshCount = 0
        private set
    var updatedProfile: UserProfile? = null
        private set

    /** Null means succeed and echo the profile back. */
    var updateResult: AppResult<UserProfile>? = null

    override suspend fun refresh(): AppResult<Unit> {
        refreshCount++
        return AppResult.Success(Unit)
    }

    override suspend fun updateProfile(profile: UserProfile): AppResult<UserProfile> {
        updatedProfile = profile
        val result = updateResult ?: AppResult.Success(profile)
        if (result is AppResult.Success) state.value = result.data
        return result
    }
}
