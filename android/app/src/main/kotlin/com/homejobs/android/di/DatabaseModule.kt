package com.homejobs.android.di

import android.content.Context
import androidx.room.Room
import com.homejobs.android.data.local.db.AppDatabase
import com.homejobs.android.data.local.db.JobDao
import com.homejobs.android.data.local.db.JobNoteDao
import com.homejobs.android.data.local.db.PhotoDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "homejobs.db")
            // This is a single-device, on-disk cache with no server to reconcile against, so a
            // destructive reset on an unhandled schema change is an acceptable tradeoff for a
            // personal app still under active development, rather than hand-writing Migrations.
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideJobDao(database: AppDatabase): JobDao = database.jobDao()

    @Provides
    fun provideJobNoteDao(database: AppDatabase): JobNoteDao = database.jobNoteDao()

    @Provides
    fun providePhotoDao(database: AppDatabase): PhotoDao = database.photoDao()
}
