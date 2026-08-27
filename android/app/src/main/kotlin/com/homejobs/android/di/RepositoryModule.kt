package com.homejobs.android.di

import com.homejobs.android.data.local.datastore.SettingsDataStore
import com.homejobs.android.data.repository.JobRepositoryImpl
import com.homejobs.android.data.repository.SettingsRepositoryImpl
import com.homejobs.android.domain.repository.JobRepository
import com.homejobs.android.domain.repository.SettingsRepository
import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindJobRepository(impl: JobRepositoryImpl): JobRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    companion object {
        @Provides
        @Singleton
        fun provideSettingsDataStore(@ApplicationContext context: Context): SettingsDataStore =
            SettingsDataStore(context)
    }
}
