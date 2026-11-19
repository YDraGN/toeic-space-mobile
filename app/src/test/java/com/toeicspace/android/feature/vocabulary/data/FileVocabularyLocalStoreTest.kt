package com.toeicspace.android.feature.vocabulary.data

import com.toeicspace.android.feature.vocabulary.data.local.FileVocabularyLocalStore
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class FileVocabularyLocalStoreTest {
    @get:Rule
    val folder = TemporaryFolder()

    private val serializer = ListSerializer(String.serializer())

    @Test
    fun `a written value reads back after a new store is created`() =
        runTest {
            val dir = File(folder.root, "vocabulary")
            val dispatcher = StandardTestDispatcher(testScheduler)
            FileVocabularyLocalStore(
                dir,
                Json,
                dispatcher,
            ).write("stage_sets_a/b", serializer, listOf("x", "y"))

            val read =
                FileVocabularyLocalStore(
                    dir,
                    Json,
                    dispatcher,
                ).read("stage_sets_a/b", serializer)

            assertEquals(listOf("x", "y"), read)
        }

    @Test
    fun `a missing, deleted or unreadable value reads as null`() =
        runTest {
            val dir = folder.newFolder("vocabulary")
            val store = FileVocabularyLocalStore(dir, Json, StandardTestDispatcher(testScheduler))

            assertNull(store.read("nothing", serializer))

            store.write("key", serializer, listOf("x"))
            store.delete("key")
            assertNull(store.read("key", serializer))

            File(dir, "broken.json").writeText("{not json")
            assertNull(store.read("broken", serializer))
        }
}
