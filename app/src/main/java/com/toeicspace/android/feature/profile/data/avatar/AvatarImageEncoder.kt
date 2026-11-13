package com.toeicspace.android.feature.profile.data.avatar

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import com.toeicspace.android.core.common.Dispatcher
import com.toeicspace.android.core.common.ToeicDispatchers
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

/** Content type of the encoded avatar; the upload URL is signed for exactly this type. */
const val AVATAR_CONTENT_TYPE = "image/webp"

/** Turns a picked image into the bytes uploaded as the avatar. */
fun interface AvatarImageEncoder {
    /** The encoded image, or null when the file cannot be read as an image. */
    suspend fun encode(imageUri: String): ByteArray?
}

/**
 * Center-crops the picked photo to a square of at most [AVATAR_SIZE_PX] and compresses it to WebP,
 * like the web client, so a phone photo of several MB becomes a few dozen KB before upload.
 */
class AndroidAvatarImageEncoder
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
        @param:Dispatcher(ToeicDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
    ) : AvatarImageEncoder {
        override suspend fun encode(imageUri: String): ByteArray? =
            withContext(ioDispatcher) {
                try {
                    val source =
                        ImageDecoder.createSource(
                            context.contentResolver,
                            Uri.parse(imageUri),
                        )
                    val decoded =
                        ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                            // Decode already shrunk, so a 12 MP photo never sits in memory at full size.
                            val shortSide = min(info.size.width, info.size.height)
                            if (shortSide > AVATAR_SIZE_PX) {
                                val scale = AVATAR_SIZE_PX.toFloat() / shortSide
                                decoder.setTargetSize(
                                    max(1, (info.size.width * scale).toInt()),
                                    max(1, (info.size.height * scale).toInt()),
                                )
                            }
                            // Hardware bitmaps cannot be cropped or compressed.
                            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                        }
                    decoded.centerSquare().toWebp()
                } catch (_: IOException) {
                    null
                } catch (_: IllegalArgumentException) {
                    null
                } catch (_: SecurityException) {
                    null
                }
            }

        private fun Bitmap.centerSquare(): Bitmap {
            val side = min(width, height)
            return if (width == height) {
                this
            } else {
                Bitmap.createBitmap(this, (width - side) / 2, (height - side) / 2, side, side)
            }
        }

        private fun Bitmap.toWebp(): ByteArray {
            val format =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Bitmap.CompressFormat.WEBP_LOSSY
                } else {
                    @Suppress("DEPRECATION")
                    Bitmap.CompressFormat.WEBP
                }
            return ByteArrayOutputStream().use { stream ->
                compress(format, WEBP_QUALITY, stream)
                stream.toByteArray()
            }
        }

        private companion object {
            const val AVATAR_SIZE_PX = 512
            const val WEBP_QUALITY = 85
        }
    }
