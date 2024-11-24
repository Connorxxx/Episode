package com.connor.episode.core.di

import com.connor.episode.data.repository.SerialPortRepositoryImpl
import com.connor.episode.domain.repository.SerialPortRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSerialPortRepository(impl: SerialPortRepositoryImpl): SerialPortRepository
}