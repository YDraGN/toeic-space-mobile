package com.toeicspace.android.feature.vocabulary.data

import com.toeicspace.android.feature.vocabulary.data.remote.ReviewApiService
import com.toeicspace.android.feature.vocabulary.data.repository.ReviewRepositoryImpl
import com.toeicspace.android.feature.vocabulary.domain.ReviewRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

/** Review sessions; the local store comes from [VocabularyDataModule]. */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class ReviewDataModule {
    /** One instance, so a single answer queue is sent once whichever screen asks. */
    @Binds
    @Singleton
    abstract fun bindReviewRepository(impl: ReviewRepositoryImpl): ReviewRepository

    companion object {
        @Provides
        @Singleton
        fun provideReviewApiService(retrofit: Retrofit): ReviewApiService =
            retrofit.create(ReviewApiService::class.java)
    }
}
