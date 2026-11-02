package com.toeicspace.android.core.common

import javax.inject.Inject

/** Wall-clock time, injectable so token expiry can be tested. */
fun interface Clock {
    fun nowMillis(): Long
}

class SystemClock
    @Inject
    constructor() : Clock {
        override fun nowMillis(): Long = System.currentTimeMillis()
    }
