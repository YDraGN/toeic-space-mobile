package com.toeicspace.android.core.di

import com.toeicspace.android.core.data.repository.SessionRepository
import com.toeicspace.android.core.data.repository.SessionRepositoryImpl
import com.toeicspace.android.core.network.ConnectivityManagerNetworkMonitor
import com.toeicspace.android.core.network.NetworkMonitor
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DataModule {
    @Binds
    @Singleton
    abstract fun bindSessionRepository(impl: SessionRepositoryImpl): SessionRepository

    @Binds
    abstract fun bindNetworkMonitor(impl: ConnectivityManagerNetworkMonitor): NetworkMonitor
}
