package com.toeicspace.android.feature.explore.data

import com.toeicspace.android.feature.explore.domain.ExploreRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class ExploreDataModule {
    @Binds
    @Singleton
    abstract fun bindExploreRepository(impl: SampleExploreRepository): ExploreRepository
}
