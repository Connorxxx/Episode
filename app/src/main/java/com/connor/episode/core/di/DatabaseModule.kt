package com.connor.episode.core.di

import android.content.Context
import androidx.room.Room
import com.connor.episode.data.local.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext ctx: Context) =
        Room.databaseBuilder(ctx, AppDatabase::class.java, "episode_db").build()

    @Singleton
    @Provides
    fun provideMessageDao(db: AppDatabase) = db.messageDao()

}