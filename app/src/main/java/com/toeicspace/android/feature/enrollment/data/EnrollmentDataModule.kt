package com.toeicspace.android.feature.enrollment.data

import com.toeicspace.android.feature.enrollment.data.remote.EnrollmentApiService
import com.toeicspace.android.feature.enrollment.data.repository.EnrollmentRepositoryImpl
import com.toeicspace.android.feature.enrollment.domain.EnrollmentRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class EnrollmentDataModule {
    @Binds
    @Singleton
    abstract fun bindEnrollmentRepository(impl: EnrollmentRepositoryImpl): EnrollmentRepository

    companion object {
        @Provides
        @Singleton
        fun provideEnrollmentApiService(retrofit: Retrofit): EnrollmentApiService =
            retrofit.create(EnrollmentApiService::class.java)
    }
}
