package com.toeicspace.android.feature.profile.presentation

import androidx.annotation.StringRes
import com.toeicspace.android.R
import com.toeicspace.android.feature.profile.domain.EnglishLevel
import com.toeicspace.android.feature.profile.domain.Gender
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/** The one place profile values turn into Vietnamese labels, shared by the tab and Account info. */
@get:StringRes
internal val Gender.labelRes: Int
    get() =
        when (this) {
            Gender.MALE -> R.string.account_info_gender_male
            Gender.FEMALE -> R.string.account_info_gender_female
            Gender.OTHER -> R.string.account_info_gender_other
        }

@get:StringRes
internal val EnglishLevel.labelRes: Int
    get() =
        when (this) {
            EnglishLevel.BEGINNER -> R.string.account_info_level_beginner
            EnglishLevel.INTERMEDIATE -> R.string.account_info_level_intermediate
            EnglishLevel.ADVANCED -> R.string.account_info_level_advanced
        }

private val DateOfBirthFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy")

internal fun LocalDate.toDisplayDate(): String = DateOfBirthFormat.format(this)
