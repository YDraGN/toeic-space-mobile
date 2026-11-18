package com.toeicspace.android.feature.checkout.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.toeicspace.android.feature.checkout.data.local.CheckoutDataStore
import com.toeicspace.android.feature.checkout.data.local.CheckoutSessionStore
import com.toeicspace.android.feature.checkout.data.local.DataStoreCheckoutSessionStore
import com.toeicspace.android.feature.checkout.data.qr.AndroidQrImageExporter
import com.toeicspace.android.feature.checkout.data.remote.CheckoutApiService
import com.toeicspace.android.feature.checkout.data.repository.CheckoutRepositoryImpl
import com.toeicspace.android.feature.checkout.domain.CheckoutRepository
import com.toeicspace.android.feature.checkout.domain.QrImageExporter
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

/** Checkout references and order ids. Under datastore/, so backups leave it out like the others. */
private const val CHECKOUT_FILE = "toeic_space_checkout"

@Module
@InstallIn(SingletonComponent::class)
internal abstract class CheckoutDataModule {
    @Binds
    @Singleton
    abstract fun bindCheckoutRepository(impl: CheckoutRepositoryImpl): CheckoutRepository

    @Binds
    @Singleton
    abstract fun bindCheckoutSessionStore(impl: DataStoreCheckoutSessionStore): CheckoutSessionStore

    @Binds
    abstract fun bindQrImageExporter(impl: AndroidQrImageExporter): QrImageExporter

    companion object {
        @Provides
        @Singleton
        fun provideCheckoutApiService(retrofit: Retrofit): CheckoutApiService =
            retrofit.create(CheckoutApiService::class.java)

        @Provides
        @Singleton
        @CheckoutDataStore
        fun provideCheckoutDataStore(
            @ApplicationContext context: Context,
        ): DataStore<Preferences> =
            PreferenceDataStoreFactory.create(
                produceFile = { context.preferencesDataStoreFile(CHECKOUT_FILE) },
            )
    }
}
