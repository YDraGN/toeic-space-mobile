package com.toeicspace.android.feature.lesson.data

import com.toeicspace.android.feature.lesson.data.remote.LessonApiService
import com.toeicspace.android.feature.lesson.domain.LessonRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class LessonDataModule {
    @Binds
    @Singleton
    abstract fun bindLessonRepository(impl: LessonRepositoryImpl): LessonRepository

    companion object {
        @Provides
        @Singleton
        fun provideLessonApiService(retrofit: Retrofit): LessonApiService =
            retrofit.create(LessonApiService::class.java)
    }
}
