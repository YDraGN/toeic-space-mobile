package com.toeicspace.android.feature.auth.presentation.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Backend default; the OTP endpoints reject requests sent sooner than this. */
const val DEFAULT_RESEND_COOLDOWN_SECONDS = 60

/**
 * Counts down the seconds until another OTP may be requested, so the resend button stays disabled
 * instead of sending requests the backend would reject.
 */
internal class ResendCountdown(
    private val scope: CoroutineScope,
    private val onTick: (secondsLeft: Int) -> Unit,
) {
    private var job: Job? = null

    fun start(seconds: Int) {
        job?.cancel()
        job =
            scope.launch {
                for (secondsLeft in seconds downTo 0) {
                    onTick(secondsLeft)
                    if (secondsLeft > 0) delay(1_000)
                }
            }
    }
}
