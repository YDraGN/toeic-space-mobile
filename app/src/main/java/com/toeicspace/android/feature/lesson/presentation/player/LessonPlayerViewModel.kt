package com.toeicspace.android.feature.lesson.presentation.player

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.HttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

private const val HTTP_UNAUTHORIZED = 401
private const val HTTP_FORBIDDEN = 403

/**
 * Owns the ExoPlayer, so it lives as long as the lesson page and not as long as one composition:
 * rotating or going fullscreen recreates the views, but the video carries on where it was.
 */
@HiltViewModel
class LessonPlayerViewModel
    @Inject
    constructor(
        @ApplicationContext context: Context,
    ) : ViewModel() {
        val player: ExoPlayer = ExoPlayer.Builder(context).build()

        private var videoId: String? = null
        private var url: String? = null

        private val _expiredCount = MutableStateFlow(0)

        /** Goes up each time the server refuses the link (401/403): it expired, fetch a new one. */
        val expiredCount: StateFlow<Int> = _expiredCount.asStateFlow()

        private val listener =
            object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    val code =
                        (error.cause as? HttpDataSource.InvalidResponseCodeException)
                            ?.responseCode
                    if (code == HTTP_UNAUTHORIZED || code == HTTP_FORBIDDEN) {
                        _expiredCount.update { it + 1 }
                    }
                }
            }

        init {
            player.addListener(listener)
        }

        /**
         * Plays [newVideoId] from [newUrl]. The same video with a fresh link resumes where it was,
         * so an expired link costs the learner nothing; another video starts from the beginning.
         * Null stops playback (a lesson without video).
         */
        fun setVideo(
            newVideoId: String?,
            newUrl: String?,
        ) {
            if (newVideoId == videoId && newUrl == url) return
            if (newVideoId == null || newUrl == null) {
                player.stop()
                player.clearMediaItems()
                videoId = null
                url = null
                return
            }
            val sameVideo = newVideoId == videoId
            val position = if (sameVideo) player.currentPosition else 0L
            val playWhenReady = sameVideo && player.playWhenReady
            videoId = newVideoId
            url = newUrl
            player.setMediaItem(MediaItem.fromUri(newUrl), position)
            player.playWhenReady = playWhenReady
            player.prepare()
        }

        fun pause() = player.pause()

        override fun onCleared() {
            player.removeListener(listener)
            player.release()
        }
    }
