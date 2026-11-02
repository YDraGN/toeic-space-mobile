package com.toeicspace.android.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.toeicspace.android.core.common.Clock
import com.toeicspace.android.core.common.Dispatcher
import com.toeicspace.android.core.common.ToeicDispatchers
import com.toeicspace.android.core.model.AuthUser
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton

/** Short-lived JWT; kept in memory only. */
data class AccessToken(
    val value: String,
    val expiresAtMillis: Long,
)

data class SessionTokens(
    val accessToken: AccessToken,
    val refreshToken: String,
    val refreshTokenExpiresAtMillis: Long,
)

/**
 * Owns the signed-in session on this device: the access token in memory, and the refresh token plus
 * the cached user encrypted on disk. Reading the user never touches the network.
 */
interface SessionTokenStore {
    val user: Flow<AuthUser?>

    fun accessToken(): AccessToken?

    /** The stored refresh token, or null when there is none or it has already expired. */
    suspend fun refreshToken(): String?

    suspend fun save(
        tokens: SessionTokens,
        user: AuthUser,
    )

    suspend fun clear()
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SessionDataStore

private val KEY_SESSION = stringPreferencesKey("session")

@Serializable
private data class StoredSession(
    val refreshToken: String,
    val refreshTokenExpiresAtMillis: Long,
    val user: AuthUser,
)

@Singleton
class EncryptedSessionTokenStore
    @Inject
    constructor(
        @param:SessionDataStore private val dataStore: DataStore<Preferences>,
        private val cipher: TokenCipher,
        private val json: Json,
        private val clock: Clock,
        @param:Dispatcher(ToeicDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
    ) : SessionTokenStore {
        @Volatile
        private var accessToken: AccessToken? = null

        private val storedSession: Flow<StoredSession?> =
            dataStore.data
                .map { prefs -> prefs[KEY_SESSION]?.let(::decode) }
                .flowOn(ioDispatcher)

        override val user: Flow<AuthUser?> =
            storedSession.map { it?.user }.distinctUntilChanged()

        override fun accessToken(): AccessToken? = accessToken

        override suspend fun refreshToken(): String? {
            val session = storedSession.first() ?: return null
            return session.refreshToken.takeIf {
                session.refreshTokenExpiresAtMillis >
                    clock.nowMillis()
            }
        }

        override suspend fun save(
            tokens: SessionTokens,
            user: AuthUser,
        ) {
            accessToken = tokens.accessToken
            val stored =
                StoredSession(
                    refreshToken = tokens.refreshToken,
                    refreshTokenExpiresAtMillis = tokens.refreshTokenExpiresAtMillis,
                    user = user,
                )
            val encrypted = cipher.encrypt(json.encodeToString(StoredSession.serializer(), stored))
            dataStore.edit { prefs -> prefs[KEY_SESSION] = encrypted }
        }

        override suspend fun clear() {
            accessToken = null
            dataStore.edit { prefs -> prefs.remove(KEY_SESSION) }
        }

        // A session that cannot be read (key lost after a restore, old format) counts as signed out.
        private fun decode(encrypted: String): StoredSession? {
            val plain = cipher.decrypt(encrypted) ?: return null
            return try {
                json.decodeFromString(StoredSession.serializer(), plain)
            } catch (_: SerializationException) {
                null
            } catch (_: IllegalArgumentException) {
                null
            }
        }
    }
