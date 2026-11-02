package com.toeicspace.android.core.network

import kotlinx.coroutines.flow.Flow

/** Reports whether the device currently has a network connection with internet capability. */
interface NetworkMonitor {
    val isOnline: Flow<Boolean>

    /** Current state without waiting for a callback, for code that cannot suspend. */
    fun isCurrentlyOnline(): Boolean
}
