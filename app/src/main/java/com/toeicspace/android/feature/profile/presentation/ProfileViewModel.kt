package com.toeicspace.android.feature.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toeicspace.android.core.common.AppResult
import com.toeicspace.android.core.crash.CrashReporter
import com.toeicspace.android.core.crash.NoOpCrashReporter
import com.toeicspace.android.core.notifications.NoOpPushTokenProvider
import com.toeicspace.android.core.notifications.PushTokenProvider
import com.toeicspace.android.feature.profile.domain.ProfileRepository
import com.toeicspace.android.feature.profile.domain.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Profile details for the Profile tab header (target score, level). Null for guests. */
@HiltViewModel
class ProfileViewModel
    @Inject
    constructor(
        private val profileRepository: ProfileRepository,
        private val crashReporter: CrashReporter = NoOpCrashReporter,
        private val pushTokens: PushTokenProvider = NoOpPushTokenProvider,
    ) : ViewModel() {
        val profile: StateFlow<UserProfile?> =
            profileRepository.profile.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = null,
            )

        val isUploadingAvatar: StateFlow<Boolean> =
            profileRepository.isUploadingAvatar.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = false,
            )

        private val _message = MutableStateFlow<ProfileMessage?>(null)

        /** Result of an avatar upload started from the tab; cleared with [onMessageShown]. */
        val message: StateFlow<ProfileMessage?> = _message.asStateFlow()

        private val _tokenToCopy = MutableStateFlow<String?>(null)

        /** Debug tools: the FCM token the screen should put on the clipboard, then clear. */
        val tokenToCopy: StateFlow<String?> = _tokenToCopy.asStateFlow()

        init {
            // Target score and level only come from the API; offline the header shows name and email.
            viewModelScope.launch { profileRepository.refresh() }
        }

        /** [imageUri] is the content URI the photo picker returned. */
        fun onAvatarPicked(imageUri: String) {
            if (isUploadingAvatar.value) return
            viewModelScope.launch {
                _message.value =
                    when (val result = profileRepository.updateAvatar(imageUri)) {
                        is AppResult.Success -> ProfileMessage.AvatarSaved
                        is AppResult.Failure -> ProfileMessage.AvatarFailed(result.error)
                    }
            }
        }

        fun onMessageShown() {
            _message.value = null
        }

        /**
         * Debug tools: turns crash reporting on for this install (it stays on), then crashes. The
         * report is sent the next time the app opens.
         */
        fun onTestCrash() {
            crashReporter.setCollectionEnabled(true)
            crashReporter.log("Test crash from the debug tools")
            throw TestCrashException()
        }

        /** Debug tools: a non-fatal error, listed under "Non-fatals" in Crashlytics. */
        fun onTestNonFatal() {
            crashReporter.setCollectionEnabled(true)
            crashReporter.recordException(TestCrashException())
            _message.value = ProfileMessage.TestErrorRecorded
        }

        fun onCopyPushToken() {
            viewModelScope.launch {
                val token = pushTokens.token()
                _tokenToCopy.value = token
                if (token == null) _message.value = ProfileMessage.PushTokenUnavailable
            }
        }

        /** The screen copied [tokenToCopy]. */
        fun onPushTokenCopied() {
            _tokenToCopy.value = null
            _message.value = ProfileMessage.PushTokenCopied
        }
    }

/** Thrown on purpose by the debug tools, so it is easy to tell apart in Crashlytics. */
class TestCrashException :
    RuntimeException("Crashlytics test from the ToeicSpace debug tools (PBL-110)")
