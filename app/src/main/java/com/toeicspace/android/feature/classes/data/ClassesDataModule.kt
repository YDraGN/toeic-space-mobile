package com.toeicspace.android.feature.classes.data

import com.toeicspace.android.feature.classes.data.remote.ClassroomApiService
import com.toeicspace.android.feature.classes.data.repository.StudentClassRepositoryImpl
import com.toeicspace.android.feature.classes.domain.StudentClassRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class ClassesDataModule {
    @Binds
    @Singleton
    abstract fun bindStudentClassRepository(
        impl: StudentClassRepositoryImpl,
    ): StudentClassRepository

    companion object {
        @Provides
        @Singleton
        fun provideClassroomApiService(retrofit: Retrofit): ClassroomApiService =
            retrofit.create(ClassroomApiService::class.java)
    }
}
