package com.toeicspace.android.testing

import com.toeicspace.android.feature.vocabulary.data.local.VocabularyLocalStore
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

/** Keeps values as JSON in a map, so a test still goes through serialization like the file store. */
class InMemoryVocabularyLocalStore(
    private val json: Json = Json { ignoreUnknownKeys = true },
) : VocabularyLocalStore {
    val values = mutableMapOf<String, String>()

    override suspend fun <T> read(
        key: String,
        serializer: KSerializer<T>,
    ): T? = values[key]?.let { json.decodeFromString(serializer, it) }

    override suspend fun <T> write(
        key: String,
        serializer: KSerializer<T>,
        value: T,
    ) {
        values[key] = json.encodeToString(serializer, value)
    }

    override suspend fun delete(key: String) {
        values.remove(key)
    }
}
