package com.toeicspace.android.core.notifications

import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/** This device's FCM token, for the debug tools now and for the backend once it stores tokens. */
interface PushTokenProvider {
    /** Null when Google Play services cannot give one (no network on first run, no Play services). */
    suspend fun token(): String?
}

object NoOpPushTokenProvider : PushTokenProvider {
    override suspend fun token(): String? = null
}

@Singleton
internal class FirebasePushTokenProvider
    @Inject
    constructor(
        private val messaging: FirebaseMessaging,
    ) : PushTokenProvider {
        override suspend fun token(): String? =
            suspendCancellableCoroutine { continuation ->
                messaging.token.addOnCompleteListener { task ->
                    continuation.resume(if (task.isSuccessful) task.result else null)
                }
            }
    }
