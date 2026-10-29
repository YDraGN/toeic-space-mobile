package com.toeicspace.android.testing

import com.toeicspace.android.core.network.NetworkMonitor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeNetworkMonitor(
    isOnline: Boolean = true,
) : NetworkMonitor {
    private val online = MutableStateFlow(isOnline)

    override val isOnline: Flow<Boolean> = online

    fun setOnline(isOnline: Boolean) {
        online.value = isOnline
    }
}
