package com.toeicspace.android.core.notifications

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.toeicspace.android.BuildConfig
import com.toeicspace.android.R

/**
 * Receives FCM messages. While the app is in the background the system shows notification
 * messages itself (manifest defaults: channel, icon, color); this service shows them while the app
 * is open, and shows data-only messages that carry a "title" and "body".
 *
 * The backend does not store device tokens yet, so for now messages come from the Firebase console.
 */
class ToeicMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        // Debug only: the token lets a developer send a test message from the console.
        if (BuildConfig.DEBUG) Log.d(TAG, "FCM token: $token")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val title =
            message.notification?.title
                ?: message.data[KEY_TITLE]
                ?: getString(R.string.app_name)
        val body = message.notification?.body ?: message.data[KEY_BODY]
        if (body == null && message.notification == null && message.data[KEY_TITLE] == null) return
        AppNotifications.show(
            context = this,
            id = message.messageId?.hashCode() ?: System.currentTimeMillis().toInt(),
            title = title,
            body = body,
        )
    }

    companion object {
        /** Logcat tag; filter on "FCM" to find the token. */
        const val TAG = "FCM"
        private const val KEY_TITLE = "title"
        private const val KEY_BODY = "body"
    }
}
