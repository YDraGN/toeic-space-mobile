package com.toeicspace.android.core.ui

/**
 * Test tags shared by the app and the Maestro flows in `maestro/`. Change a value here and in the
 * flows together: Maestro finds these as `id:` because MainActivity turns on testTagsAsResourceId.
 */
object TestTags {
    const val SIGN_IN_EMAIL = "sign_in_email"
    const val SIGN_IN_PASSWORD = "sign_in_password"

    const val SIGN_UP_FULL_NAME = "sign_up_full_name"
    const val SIGN_UP_EMAIL = "sign_up_email"
    const val SIGN_UP_PHONE = "sign_up_phone"
    const val SIGN_UP_PASSWORD = "sign_up_password"
    const val SIGN_UP_CONFIRM_PASSWORD = "sign_up_confirm_password"

    const val PROFILE_FULL_NAME = "profile_full_name"
    const val PROFILE_PHONE = "profile_phone"
    const val PROFILE_BIOGRAPHY = "profile_biography"
}
