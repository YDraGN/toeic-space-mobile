package com.toeicspace.android.core.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.toeicspace.android.MainActivity
import com.toeicspace.android.R

/** Notification channels and posting, shared by the FCM service and future local reminders. */
object AppNotifications {
    /** Announcements and learning updates; the id the manifest gives FCM as its default channel. */
    const val CHANNEL_GENERAL = "general"

    /** Safe to call on every start: creating an existing channel only updates its name. */
    fun createChannels(context: Context) {
        val channel =
            NotificationChannel(
                CHANNEL_GENERAL,
                context.getString(R.string.notification_channel_general),
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = context.getString(R.string.notification_channel_general_body)
            }
        context
            .getSystemService(
                NotificationManager::class.java,
            )?.createNotificationChannel(channel)
    }

    /** Android 13+ asks; earlier versions allow it unless the user turned notifications off. */
    fun canPost(context: Context): Boolean {
        val granted =
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) ==
                PackageManager.PERMISSION_GRANTED
        return granted && NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    /** Shows [title]/[body]; tapping it opens the app. Does nothing without permission. */
    fun show(
        context: Context,
        id: Int,
        title: String,
        body: String?,
    ) {
        if (!canPost(context)) return
        val openApp =
            PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
        val notification =
            NotificationCompat
                .Builder(context, CHANNEL_GENERAL)
                .setSmallIcon(R.drawable.ic_bell)
                .setColor(ContextCompat.getColor(context, R.color.notification_accent))
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setAutoCancel(true)
                .setContentIntent(openApp)
                .build()
        try {
            NotificationManagerCompat.from(context).notify(id, notification)
        } catch (_: SecurityException) {
            // The permission was revoked between the check and the post.
        }
    }
}
