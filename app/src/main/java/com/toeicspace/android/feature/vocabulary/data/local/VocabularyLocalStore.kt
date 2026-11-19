package com.toeicspace.android.feature.vocabulary.data.local

import com.toeicspace.android.core.common.Dispatcher
import com.toeicspace.android.core.common.ToeicDispatchers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Qualifier

/**
 * Small key-value store for vocabulary data that must survive offline use and process death: the
 * last loaded roadmap, the review answers not sent yet and the open review session. Values are
 * JSON; a value that cannot be read any more (an app update changed its shape) reads as missing.
 */
interface VocabularyLocalStore {
    suspend fun <T> read(
        key: String,
        serializer: KSerializer<T>,
    ): T?

    suspend fun <T> write(
        key: String,
        serializer: KSerializer<T>,
        value: T,
    )

    suspend fun delete(key: String)
}

/** The folder [FileVocabularyLocalStore] writes into. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class VocabularyStoreDir

/** One file per key, written to a temporary file first so a crash never leaves half a value. */
internal class FileVocabularyLocalStore
    @Inject
    constructor(
        @VocabularyStoreDir private val directory: File,
        private val json: Json,
        @Dispatcher(ToeicDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
    ) : VocabularyLocalStore {
        private val mutex = Mutex()

        override suspend fun <T> read(
            key: String,
            serializer: KSerializer<T>,
        ): T? =
            withContext(ioDispatcher) {
                mutex.withLock {
                    val file = fileOf(key)
                    if (!file.exists()) return@withLock null
                    try {
                        json.decodeFromString(serializer, file.readText())
                    } catch (_: IOException) {
                        null
                    } catch (_: SerializationException) {
                        null
                    } catch (_: IllegalArgumentException) {
                        null
                    }
                }
            }

        override suspend fun <T> write(
            key: String,
            serializer: KSerializer<T>,
            value: T,
        ) = withContext(ioDispatcher) {
            mutex.withLock {
                try {
                    directory.mkdirs()
                    val target = fileOf(key)
                    val temp = File(directory, "${target.name}.tmp")
                    temp.writeText(json.encodeToString(serializer, value))
                    if (!temp.renameTo(target)) {
                        target.delete()
                        temp.renameTo(target)
                    }
                } catch (_: IOException) {
                    // A full disk only costs the offline copy; the screen already has the data.
                }
            }
        }

        override suspend fun delete(key: String) =
            withContext(ioDispatcher) {
                mutex.withLock {
                    fileOf(key).delete()
                    Unit
                }
            }

        private fun fileOf(key: String) =
            File(directory, key.replace(UnsafeKeyChars, "_") + ".json")

        private companion object {
            val UnsafeKeyChars = Regex("[^A-Za-z0-9_-]")
        }
    }
