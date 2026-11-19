package com.toeicspace.android.feature.vocabulary.presentation.components

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import com.toeicspace.android.BuildConfig
import java.io.IOException
import java.net.URLEncoder
import java.util.Locale

/** Reads a word aloud. Implementations are cheap to call repeatedly; a new call stops the last one. */
fun interface WordSpeaker {
    fun speak(text: String)
}

/**
 * The device's text-to-speech in US English. When no engine or no English voice is available it
 * plays the Content service's neural speech instead (`GET content/api/v1/speech?text=`, an
 * `audio/mpeg` clip that the server caches).
 */
internal class AndroidWordSpeaker(
    context: Context,
) : WordSpeaker {
    private val speechUrlBase = BuildConfig.API_BASE_URL + SPEECH_PATH

    /** null until the engine reports back; then whether it can speak English. */
    @Volatile
    private var ttsReady: Boolean? = null
    private var player: MediaPlayer? = null
    private val tts: TextToSpeech =
        TextToSpeech(context.applicationContext) { status ->
            ttsReady = status == TextToSpeech.SUCCESS && configureEnglish()
        }

    private fun configureEnglish(): Boolean {
        val result = tts.setLanguage(Locale.US)
        return result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
    }

    override fun speak(text: String) {
        if (text.isBlank()) return
        if (ttsReady == true) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, text)
        } else {
            playFromServer(text)
        }
    }

    private fun playFromServer(text: String) {
        release()
        val url = speechUrlBase + URLEncoder.encode(text, Charsets.UTF_8.name())
        player =
            MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes
                        .Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build(),
                )
                setOnPreparedListener { it.start() }
                setOnErrorListener { _, _, _ ->
                    release()
                    true
                }
                setOnCompletionListener { release() }
                try {
                    setDataSource(url)
                    prepareAsync()
                } catch (_: IOException) {
                    release()
                } catch (_: IllegalStateException) {
                    release()
                }
            }
    }

    private fun release() {
        player?.release()
        player = null
    }

    fun shutdown() {
        release()
        tts.stop()
        tts.shutdown()
    }

    private companion object {
        const val SPEECH_PATH = "content/api/v1/speech?text="
    }
}

/** One speaker per screen, shut down when the screen leaves; a silent one in previews. */
@Composable
internal fun rememberWordSpeaker(): WordSpeaker {
    if (LocalInspectionMode.current) return remember { WordSpeaker { } }
    val context = LocalContext.current
    val speaker = remember(context) { AndroidWordSpeaker(context) }
    DisposableEffect(speaker) { onDispose { speaker.shutdown() } }
    return speaker
}
