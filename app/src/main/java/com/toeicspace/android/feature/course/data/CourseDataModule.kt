package com.toeicspace.android.feature.course.data

import com.toeicspace.android.feature.course.data.remote.CourseApiService
import com.toeicspace.android.feature.course.data.repository.CourseRepositoryImpl
import com.toeicspace.android.feature.course.domain.CourseRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class CourseDataModule {
    @Binds
    @Singleton
    abstract fun bindCourseRepository(impl: CourseRepositoryImpl): CourseRepository

    companion object {
        @Provides
        @Singleton
        fun provideCourseApiService(retrofit: Retrofit): CourseApiService =
            retrofit.create(CourseApiService::class.java)
    }
}
