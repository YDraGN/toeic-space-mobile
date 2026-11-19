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

    const val COURSE_SEARCH = "course_search"
    const val COURSE_FILTER = "course_filter"
    const val COURSE_ENROLL = "course_enroll"
    const val COURSE_ENROLL_CONFIRM = "course_enroll_confirm"
    const val COURSE_ENROLL_CONFIRM_BUTTON = "course_enroll_confirm_button"
    const val COURSE_SOLD_OUT_DIALOG = "course_sold_out_dialog"

    const val MY_COURSES_ENTRY = "my_courses_entry"
    const val MY_COURSES_ITEM = "my_courses_item"

    const val LESSON_PLAYER = "lesson_player"
    const val LESSON_PREVIOUS = "lesson_previous"
    const val LESSON_NEXT = "lesson_next"

    // Shared UI states (core/ui/components/*State.kt)
    const val STATE_LOADING = "state_loading"
    const val STATE_EMPTY = "state_empty"
    const val STATE_ERROR = "state_error"
    const val STATE_COMING_SOON = "state_coming_soon"
    const val STATE_SIGN_IN_REQUIRED = "state_sign_in_required"
    const val STATE_OFFLINE_BANNER = "state_offline_banner"
    const val STATE_ACTION = "state_action"
    const val STATE_ACTION_RETRY = "state_action_retry"
    const val STATE_ACTION_SIGN_UP = "state_action_sign_up"
    const val STATE_ACTION_SIGN_IN = "state_action_sign_in"

    // Vocabulary (PBL-124, PBL-125)
    const val VOCAB_STUDY_FLASHCARDS = "vocab_study_flashcards"
    const val VOCAB_FLASHCARD = "vocab_flashcard"
    const val VOCAB_FLASHCARD_NEXT = "vocab_flashcard_next"
    const val VOCAB_REVIEW_START = "vocab_review_start"
    const val VOCAB_REVIEW_OPTION = "vocab_review_option"
}
