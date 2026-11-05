package com.toeicspace.android.feature.profile.data

import com.toeicspace.android.core.data.repository.SessionRepository
import com.toeicspace.android.core.model.SessionState
import com.toeicspace.android.feature.profile.domain.EnglishLevel
import com.toeicspace.android.feature.profile.domain.ProfileRepository
import com.toeicspace.android.feature.profile.domain.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

/**
 * Sample profile until the app calls `GET /users/me`: name and email come from the real session,
 * the other fields are made up and the screen labels them "Dữ liệu mẫu". Gender is left empty on
 * purpose so the "not filled in yet" state shows too. Swap the binding in [ProfileDataModule] for
 * the API-backed repository.
 */
class SampleProfileRepository
    @Inject
    constructor(
        sessionRepository: SessionRepository,
    ) : ProfileRepository {
        override val profile: Flow<UserProfile?> =
            // Wait for the stored session: an early "still loading" would read as a guest and close
            // the account screen right after it opens.
            sessionRepository.session.filterIsInstance<SessionState.Ready>().map { session ->
                val user = session.user ?: return@map null
                UserProfile(
                    fullName = user.fullName,
                    email = user.email,
                    phone = "0905123456",
                    dateOfBirth = LocalDate.of(2004, 5, 12),
                    biography = "Muốn đạt 650+ trước kỳ thực tập mùa hè.",
                    targetScore = 650,
                    currentLevel = EnglishLevel.INTERMEDIATE,
                )
            }
    }
