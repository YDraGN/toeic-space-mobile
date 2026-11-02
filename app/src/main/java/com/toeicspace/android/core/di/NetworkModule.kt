package com.toeicspace.android.core.di

import com.toeicspace.android.BuildConfig
import com.toeicspace.android.core.network.AUTHORIZATION_HEADER
import com.toeicspace.android.core.network.AuthInterceptor
import com.toeicspace.android.core.network.ClientHeadersInterceptor
import com.toeicspace.android.core.network.ConnectivityInterceptor
import com.toeicspace.android.core.network.SessionApiService
import com.toeicspace.android.core.network.TokenAuthenticator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

/** Client without the auth interceptor and authenticator, used to refresh the session itself. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SessionHttpClient

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideJson(): Json =
        Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }

    @Provides
    @Singleton
    @SessionHttpClient
    fun provideSessionOkHttpClient(
        connectivityInterceptor: ConnectivityInterceptor,
        clientHeadersInterceptor: ClientHeadersInterceptor,
    ): OkHttpClient =
        OkHttpClient
            .Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .addInterceptor(connectivityInterceptor)
            .addInterceptor(clientHeadersInterceptor)
            .apply {
                if (BuildConfig.DEBUG) {
                    // Headers only: bodies carry passwords and tokens.
                    addInterceptor(
                        HttpLoggingInterceptor()
                            .apply { level = HttpLoggingInterceptor.Level.BASIC }
                            .also { it.redactHeader(AUTHORIZATION_HEADER) },
                    )
                }
            }.build()

    @Provides
    @Singleton
    fun provideOkHttpClient(
        @SessionHttpClient sessionClient: OkHttpClient,
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator,
    ): OkHttpClient =
        sessionClient
            .newBuilder()
            .apply { interceptors().add(0, authInterceptor) }
            .authenticator(tokenAuthenticator)
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        json: Json,
    ): Retrofit = buildRetrofit(okHttpClient, json)

    @Provides
    @Singleton
    fun provideSessionApiService(
        @SessionHttpClient sessionClient: OkHttpClient,
        json: Json,
    ): SessionApiService = buildRetrofit(sessionClient, json).create(SessionApiService::class.java)

    private fun buildRetrofit(
        client: OkHttpClient,
        json: Json,
    ): Retrofit =
        Retrofit
            .Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
}
