package com.toeicspace.android.feature.vocabulary.data

import android.content.Context
import com.toeicspace.android.feature.vocabulary.data.local.FileVocabularyLocalStore
import com.toeicspace.android.feature.vocabulary.data.local.VocabularyLocalStore
import com.toeicspace.android.feature.vocabulary.data.local.VocabularyStoreDir
import com.toeicspace.android.feature.vocabulary.data.remote.VocabularyApiService
import com.toeicspace.android.feature.vocabulary.data.repository.FlashcardMarkRepositoryImpl
import com.toeicspace.android.feature.vocabulary.data.repository.VocabularyRepositoryImpl
import com.toeicspace.android.feature.vocabulary.domain.FlashcardMarkRepository
import com.toeicspace.android.feature.vocabulary.domain.VocabularyRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class VocabularyDataModule {
    @Binds
    @Singleton
    abstract fun bindVocabularyRepository(impl: VocabularyRepositoryImpl): VocabularyRepository

    /** One instance, so the flashcard review queue is sent once. */
    @Binds
    @Singleton
    abstract fun bindFlashcardMarkRepository(
        impl: FlashcardMarkRepositoryImpl,
    ): FlashcardMarkRepository

    @Binds
    @Singleton
    abstract fun bindVocabularyLocalStore(impl: FileVocabularyLocalStore): VocabularyLocalStore

    companion object {
        @Provides
        @Singleton
        fun provideVocabularyApiService(retrofit: Retrofit): VocabularyApiService =
            retrofit.create(VocabularyApiService::class.java)

        /** App-private files, not the cache folder: unsent review answers must not be cleared. */
        @Provides
        @VocabularyStoreDir
        fun provideVocabularyStoreDir(
            @ApplicationContext context: Context,
        ): File = File(context.filesDir, "vocabulary")
    }
}
