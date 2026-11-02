package com.toeicspace.android.core.di

import com.toeicspace.android.core.common.Clock
import com.toeicspace.android.core.common.SystemClock
import com.toeicspace.android.core.data.repository.SessionRepository
import com.toeicspace.android.core.data.repository.SessionRepositoryImpl
import com.toeicspace.android.core.datastore.EncryptedSessionTokenStore
import com.toeicspace.android.core.datastore.KeystoreTokenCipher
import com.toeicspace.android.core.datastore.SessionTokenStore
import com.toeicspace.android.core.datastore.TokenCipher
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

    @Binds
    abstract fun bindSessionTokenStore(impl: EncryptedSessionTokenStore): SessionTokenStore

    @Binds
    abstract fun bindTokenCipher(impl: KeystoreTokenCipher): TokenCipher

    @Binds
    abstract fun bindClock(impl: SystemClock): Clock
}
