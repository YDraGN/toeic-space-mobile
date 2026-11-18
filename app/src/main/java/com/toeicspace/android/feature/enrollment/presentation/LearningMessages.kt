package com.toeicspace.android.feature.enrollment.presentation

import androidx.annotation.StringRes
import com.toeicspace.android.R
import com.toeicspace.android.core.common.AppError
import com.toeicspace.android.feature.auth.presentation.common.messageRes

/**
 * Error text for the learning pages (enrollment, my courses, lessons). The shared mapping reads 401
 * as wrong credentials, which only makes sense on the sign-in form; here it means the session ended.
 */
@StringRes
fun AppError.learningMessageRes(): Int =
    when {
        this == AppError.Unauthorized -> R.string.enrollment_error_session_expired
        this is AppError.Server && status == HTTP_UNAUTHORIZED ->
            R.string.enrollment_error_session_expired
        else -> messageRes()
    }

private const val HTTP_UNAUTHORIZED = 401
