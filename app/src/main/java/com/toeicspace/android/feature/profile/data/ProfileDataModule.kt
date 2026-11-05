package com.toeicspace.android.feature.profile.data

import com.toeicspace.android.feature.profile.domain.ProfileRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class ProfileDataModule {
    @Binds
    @Singleton
    abstract fun bindProfileRepository(impl: SampleProfileRepository): ProfileRepository
}
