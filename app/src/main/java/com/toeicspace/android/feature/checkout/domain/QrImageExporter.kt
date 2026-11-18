package com.toeicspace.android.feature.checkout.domain

/** Hands the backend's VietQR image to the learner's other apps. The image itself is never altered. */
interface QrImageExporter {
    /** Saves the image to `Pictures/ToeicSpace` in the gallery. False when it could not be saved. */
    suspend fun saveToGallery(
        qrUrl: String,
        fileName: String,
    ): Boolean

    /** A `content://` uri of a private copy for the share sheet, or null when it could not be made. */
    suspend fun prepareShare(
        qrUrl: String,
        fileName: String,
    ): String?

    /** Below Android 10 the gallery is only writable with the storage permission. */
    val needsStoragePermission: Boolean
}
