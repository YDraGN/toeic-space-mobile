package com.toeicspace.android.feature.profile.presentation

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.toeicspace.android.core.notifications.AppNotifications

/** Whether notifications can be shown right now, and what flipping the switch does. */
internal class NotificationToggle(
    val enabled: Boolean,
    val onToggle: (Boolean) -> Unit,
)

/**
 * The Profile "Thông báo" switch. Turning it on asks for the Android 13+ permission; once Android
 * stops asking (denied twice), or to turn notifications off, it opens the app's notification
 * settings, since an app cannot change that itself. Re-read on every resume.
 */
@Composable
internal fun rememberNotificationToggle(): NotificationToggle {
    val context = LocalContext.current
    var enabled by remember { mutableStateOf(AppNotifications.canPost(context)) }
    LifecycleResumeEffect(Unit) {
        enabled = AppNotifications.canPost(context)
        onPauseOrDispose {}
    }
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            enabled = AppNotifications.canPost(context)
            if (!granted && !context.canAskAgain()) context.openNotificationSettings()
        }
    return NotificationToggle(enabled = enabled) { turnOn ->
        // Asking only helps while the permission itself is missing; a granted permission with
        // notifications switched off in system settings can only be fixed there.
        if (turnOn && !context.hasNotificationPermission()) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            context.openNotificationSettings()
        }
    }
}

private fun Context.hasNotificationPermission(): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
        PackageManager.PERMISSION_GRANTED

private fun Context.canAskAgain(): Boolean {
    val activity = findActivity() ?: return false
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ActivityCompat.shouldShowRequestPermissionRationale(
            activity,
            Manifest.permission.POST_NOTIFICATIONS,
        )
}

private fun Context.openNotificationSettings() {
    val intent =
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(intent)
}

private tailrec fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
