package com.toeicspace.android.feature.profile.presentation

import androidx.annotation.StringRes
import com.toeicspace.android.R
import com.toeicspace.android.core.common.AppError
import com.toeicspace.android.feature.auth.presentation.common.messageRes

/** One-off snackbar for the profile screens; the screen clears it once shown. */
sealed interface ProfileMessage {
    data object Saved : ProfileMessage

    data class SaveFailed(
        val error: AppError,
    ) : ProfileMessage

    data object AvatarSaved : ProfileMessage

    data class AvatarFailed(
        val error: AppError,
    ) : ProfileMessage
}

@StringRes
internal fun ProfileMessage.messageRes(): Int =
    when (this) {
        ProfileMessage.Saved -> R.string.account_info_message_saved
        is ProfileMessage.SaveFailed -> error.messageRes()
        ProfileMessage.AvatarSaved -> R.string.account_info_message_avatar_saved
        // Unknown here almost always means the picked file could not be read as an image.
        is ProfileMessage.AvatarFailed ->
            if (error ==
                AppError.Unknown
            ) {
                R.string.account_info_error_avatar_image
            } else {
                error.messageRes()
            }
    }
