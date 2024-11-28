package com.connor.episode.core.di

import com.connor.episode.BuildConfig
import com.connor.episode.data.repository.FakeSerialPortRepository
import com.connor.episode.data.repository.MessageRepositoryImpl
import com.connor.episode.data.repository.PreferencesRepositoryImpl
import com.connor.episode.data.repository.SerialPortRepositoryImpl
import com.connor.episode.domain.repository.MessageRepository
import com.connor.episode.domain.repository.PreferencesRepository
import com.connor.episode.domain.repository.SerialPortRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

enum class SerialPortType {
    REAL, FAKE
}

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class SerialPort(val type: SerialPortType)

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    @SerialPort(SerialPortType.REAL)
    abstract fun bindSerialPortRepository(impl: SerialPortRepositoryImpl): SerialPortRepository

    @Binds
    @Singleton
    @SerialPort(SerialPortType.FAKE)
    abstract fun bindFakeSerialPortRepository(impl: FakeSerialPortRepository): SerialPortRepository

    @Binds
    @Singleton
    abstract fun bindMessageRepository(impl: MessageRepositoryImpl): MessageRepository

    @Binds
    @Singleton
    abstract fun bindPreferencesRepository(impl: PreferencesRepositoryImpl): PreferencesRepository

}

@Module
@InstallIn(SingletonComponent::class)
object RepositoryProvideModule {
    @Provides
    @Singleton
    fun provide(
        @SerialPort(SerialPortType.REAL) real: SerialPortRepository,
        @SerialPort(SerialPortType.FAKE) fake: SerialPortRepository
    ) = if (BuildConfig.FLAVOR == "demo") fake else real
}