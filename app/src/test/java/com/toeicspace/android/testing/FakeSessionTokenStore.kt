package com.toeicspace.android.testing

import com.toeicspace.android.core.datastore.AccessToken
import com.toeicspace.android.core.datastore.SessionTokenStore
import com.toeicspace.android.core.datastore.SessionTokens
import com.toeicspace.android.core.model.AuthUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeSessionTokenStore(
    private val clock: () -> Long = { 0L },
) : SessionTokenStore {
    private val currentUser = MutableStateFlow<AuthUser?>(null)
    var tokens: SessionTokens? = null
        private set

    override val user: Flow<AuthUser?> = currentUser

    override fun accessToken(): AccessToken? = tokens?.accessToken

    override suspend fun refreshToken(): String? =
        tokens?.takeIf { it.refreshTokenExpiresAtMillis > clock() }?.refreshToken

    override suspend fun save(
        tokens: SessionTokens,
        user: AuthUser,
    ) {
        this.tokens = tokens
        currentUser.value = user
    }

    override suspend fun clear() {
        tokens = null
        currentUser.value = null
    }
}
