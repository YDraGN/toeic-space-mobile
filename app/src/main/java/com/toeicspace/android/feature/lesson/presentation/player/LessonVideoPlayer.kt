package com.toeicspace.android.feature.lesson.presentation.player

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.activity.compose.LocalActivity
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import com.toeicspace.android.core.ui.TestTags
import com.toeicspace.android.core.ui.theme.VideoBackdrop

private const val VIDEO_ASPECT_RATIO = 16f / 9f

/**
 * Media3's own controls: play/pause, the seek bar, playback speed (in the settings menu) and the
 * fullscreen button, which calls [onFullscreenChange]. [fullscreen] fills the given space instead
 * of keeping 16:9.
 */
@OptIn(UnstableApi::class)
@Composable
internal fun LessonVideoPlayer(
    player: Player,
    fullscreen: Boolean,
    onFullscreenChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape? = null,
) {
    val sized =
        if (fullscreen) {
            modifier
        } else {
            modifier.fillMaxWidth().aspectRatio(VIDEO_ASPECT_RATIO)
        }
    AndroidView(
        factory = { context ->
            PlayerView(context).apply {
                setShowSubtitleButton(false)
                setShowNextButton(false)
                setShowPreviousButton(false)
                setShutterBackgroundColor(VideoBackdrop.toArgb())
                setBackgroundColor(VideoBackdrop.toArgb())
            }
        },
        update = { view ->
            view.player = player
            view.setFullscreenButtonState(fullscreen)
            view.setFullscreenButtonClickListener { onFullscreenChange(it) }
        },
        onRelease = { view -> view.player = null },
        modifier =
            sized
                .then(if (shape != null) Modifier.clip(shape) else Modifier)
                .background(VideoBackdrop)
                .testTag(TestTags.LESSON_PLAYER),
    )
}

/**
 * While [fullscreen]: system bars hidden (a swipe shows them for a moment) and landscape. Rotation
 * recreates the activity, so nothing is restored while it is only changing configuration.
 */
@Composable
internal fun FullscreenEffect(fullscreen: Boolean) {
    val activity = LocalActivity.current ?: return
    DisposableEffect(activity, fullscreen) {
        if (fullscreen) activity.enterFullscreen()
        onDispose {
            if (fullscreen && !activity.isChangingConfigurations) activity.exitFullscreen()
        }
    }
}

private fun Activity.enterFullscreen() {
    WindowCompat.getInsetsController(window, window.decorView).apply {
        systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        hide(WindowInsetsCompat.Type.systemBars())
    }
    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
}

private fun Activity.exitFullscreen() {
    WindowCompat
        .getInsetsController(
            window,
            window.decorView,
        ).show(WindowInsetsCompat.Type.systemBars())
    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
}
