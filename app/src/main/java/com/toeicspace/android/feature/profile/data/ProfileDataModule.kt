package com.toeicspace.android.feature.profile.data

import com.toeicspace.android.BuildConfig
import com.toeicspace.android.feature.profile.data.avatar.AndroidAvatarImageEncoder
import com.toeicspace.android.feature.profile.data.avatar.AvatarImageEncoder
import com.toeicspace.android.feature.profile.data.avatar.AvatarStorageConfig
import com.toeicspace.android.feature.profile.data.remote.ProfileApiService
import com.toeicspace.android.feature.profile.data.repository.ProfileRepositoryImpl
import com.toeicspace.android.feature.profile.domain.ProfileRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class ProfileDataModule {
    @Binds
    @Singleton
    abstract fun bindProfileRepository(impl: ProfileRepositoryImpl): ProfileRepository

    @Binds
    abstract fun bindAvatarImageEncoder(impl: AndroidAvatarImageEncoder): AvatarImageEncoder

    companion object {
        @Provides
        fun provideAvatarStorageConfig(): AvatarStorageConfig =
            AvatarStorageConfig(BuildConfig.AVATAR_CDN_URL)

        @Provides
        @Singleton
        fun provideProfileApiService(retrofit: Retrofit): ProfileApiService =
            retrofit.create(ProfileApiService::class.java)
    }
}
