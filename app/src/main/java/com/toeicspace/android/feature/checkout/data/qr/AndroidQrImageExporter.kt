package com.toeicspace.android.feature.checkout.data.qr

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import coil3.SingletonImageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap
import com.toeicspace.android.core.common.Dispatcher
import com.toeicspace.android.core.common.ToeicDispatchers
import com.toeicspace.android.feature.checkout.domain.QrImageExporter
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.io.OutputStream
import javax.inject.Inject

/** Folder under Pictures, and the cache folder the FileProvider in the manifest exposes. */
private const val GALLERY_FOLDER = "ToeicSpace"
private const val SHARE_CACHE_FOLDER = "shared_qr"
private const val PNG_QUALITY = 100
private const val PNG_MIME = "image/png"

/** Must match the `<provider>` authority in AndroidManifest.xml. */
internal fun checkoutFileProviderAuthority(context: Context) =
    "${context.packageName}.checkout.fileprovider"

/**
 * Loads the QR through Coil (usually already in its cache, since the screen shows it) and writes it
 * as PNG. Android 10+ goes through MediaStore with no permission; Android 9 writes the file into the
 * public Pictures folder, which needs WRITE_EXTERNAL_STORAGE, then asks the media scanner to index it.
 */
internal class AndroidQrImageExporter
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
        @param:Dispatcher(ToeicDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
    ) : QrImageExporter {
        override val needsStoragePermission: Boolean
            get() = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q

        override suspend fun saveToGallery(
            qrUrl: String,
            fileName: String,
        ): Boolean {
            val bitmap = loadBitmap(qrUrl) ?: return false
            return withContext(ioDispatcher) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        saveWithMediaStore(bitmap, fileName)
                    } else {
                        saveToPublicPictures(bitmap, fileName)
                    }
                } catch (_: IOException) {
                    false
                } catch (_: SecurityException) {
                    false
                }
            }
        }

        override suspend fun prepareShare(
            qrUrl: String,
            fileName: String,
        ): String? {
            val bitmap = loadBitmap(qrUrl) ?: return null
            return withContext(ioDispatcher) {
                try {
                    val folder = File(context.cacheDir, SHARE_CACHE_FOLDER)
                    // Keep only the QR being shared now; older ones hold another order's memo.
                    folder.deleteRecursively()
                    folder.mkdirs()
                    val file = File(folder, fileName)
                    file.outputStream().use { bitmap.writePng(it) }
                    FileProvider
                        .getUriForFile(context, checkoutFileProviderAuthority(context), file)
                        .toString()
                } catch (_: IOException) {
                    null
                } catch (_: IllegalArgumentException) {
                    null
                }
            }
        }

        private suspend fun loadBitmap(url: String): Bitmap? {
            val request =
                ImageRequest
                    .Builder(context)
                    .data(url)
                    .allowHardware(false)
                    .build()
            val result = SingletonImageLoader.get(context).execute(request)
            return (result as? SuccessResult)?.image?.toBitmap()
        }

        private fun saveWithMediaStore(
            bitmap: Bitmap,
            fileName: String,
        ): Boolean {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false
            val resolver = context.contentResolver
            val values =
                ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, PNG_MIME)
                    put(
                        MediaStore.Images.Media.RELATIVE_PATH,
                        "${Environment.DIRECTORY_PICTURES}/$GALLERY_FOLDER",
                    )
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            val collection =
                MediaStore.Images.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL_PRIMARY,
                )
            val uri = resolver.insert(collection, values) ?: return false
            val written =
                resolver.openOutputStream(uri)?.use { bitmap.writePng(it) } ?: false
            if (!written) {
                resolver.delete(uri, null, null)
                return false
            }
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
            return true
        }

        @Suppress("DEPRECATION") // The only way to reach the public Pictures folder on Android 9.
        private fun saveToPublicPictures(
            bitmap: Bitmap,
            fileName: String,
        ): Boolean {
            val folder =
                File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    GALLERY_FOLDER,
                )
            if (!folder.exists() && !folder.mkdirs()) return false
            val file = File(folder, fileName)
            val written = file.outputStream().use { bitmap.writePng(it) }
            if (written) {
                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(file.absolutePath),
                    arrayOf(PNG_MIME),
                    null,
                )
            }
            return written
        }

        private fun Bitmap.writePng(stream: OutputStream): Boolean =
            compress(Bitmap.CompressFormat.PNG, PNG_QUALITY, stream)
    }
