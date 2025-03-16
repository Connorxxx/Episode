package com.connor.episode.core.di

import com.connor.episode.BuildConfig
import com.connor.episode.core.di.NetType.TCP
import com.connor.episode.core.di.NetType.UDP
import com.connor.episode.core.di.NetType.WebSocket
import com.connor.episode.core.di.SerialPortType.FAKE
import com.connor.episode.core.di.SerialPortType.REAL
import com.connor.episode.data.remote.network.NetworkClient
import com.connor.episode.data.remote.network.NetworkServer
import com.connor.episode.data.repository.BleClientRepositoryImpl
import com.connor.episode.data.repository.BleServerRepositoryImpl
import com.connor.episode.data.repository.ClientRepositoryImpl
import com.connor.episode.data.repository.FakeSerialPortRepository
import com.connor.episode.data.repository.MessageRepositoryImpl
import com.connor.episode.data.repository.PreferencesRepositoryImpl
import com.connor.episode.data.repository.SerialPortRepositoryImpl
import com.connor.episode.data.repository.ServerRepositoryImpl
import com.connor.episode.domain.repository.BleClientRepository
import com.connor.episode.domain.repository.BleServerRepository
import com.connor.episode.domain.repository.MessageRepository
import com.connor.episode.domain.repository.NetClientRepository
import com.connor.episode.domain.repository.NetServerRepository
import com.connor.episode.domain.repository.PreferencesRepository
import com.connor.episode.domain.repository.SerialPortRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
annotation class SerialPort(val type: SerialPortType)
enum class SerialPortType { REAL, FAKE }

@Qualifier
annotation class Server(val type: NetType)

@Qualifier
annotation class Client(val type: NetType)

enum class NetType { TCP, UDP, WebSocket }

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideMessageRepository(repository: MessageRepositoryImpl): MessageRepository =
        repository

    @Provides
    @Singleton
    fun providePreferencesRepository(repository: PreferencesRepositoryImpl): PreferencesRepository =
        repository

    @Provides
    @Singleton
    @SerialPort(REAL)
    fun provideSerialPortRepository(repository: SerialPortRepositoryImpl): SerialPortRepository =
        repository

    @Provides
    @Singleton
    @SerialPort(FAKE)
    fun provideFakeSerialPortRepository(repository: FakeSerialPortRepository): SerialPortRepository =
        repository

    @Provides
    @Singleton
    @Server(TCP)
    fun provideTCPServerRepository(@Server(TCP) dataSource: NetworkServer): NetServerRepository =
        ServerRepositoryImpl(dataSource)

    @Provides
    @Singleton
    @Server(UDP)
    fun provideUDPServerRepository(@Server(UDP) dataSource: NetworkServer): NetServerRepository =
        ServerRepositoryImpl(dataSource)

    @Provides
    @Singleton
    @Server(WebSocket)
    fun provideWebSocketServerRepository(@Server(WebSocket) dataSource: NetworkServer): NetServerRepository =
        ServerRepositoryImpl(dataSource)

    @Provides
    @Singleton
    @Client(TCP)
    fun provideTCPClientRepository(@Client(TCP) dataSource: NetworkClient): NetClientRepository =
        ClientRepositoryImpl(dataSource)

    @Provides
    @Singleton
    @Client(UDP)
    fun provideUDPClientRepository(@Client(UDP) dataSource: NetworkClient): NetClientRepository =
        ClientRepositoryImpl(dataSource)

    @Provides
    @Singleton
    @Client(WebSocket)
    fun provideWebSocketClientRepository(@Client(WebSocket) dataSource: NetworkClient): NetClientRepository =
        ClientRepositoryImpl(dataSource)

    @Provides
    @Singleton
    fun provideBleServerRepository(repository: BleServerRepositoryImpl): BleServerRepository = repository

    @Provides
    @Singleton
    fun provideBleClientRepository(repository: BleClientRepositoryImpl): BleClientRepository = repository
}

@Module
@InstallIn(SingletonComponent::class)
object RepositoryProvideModule {
    @Provides
    @Singleton
    fun provide(
        @SerialPort(REAL) real: SerialPortRepository,
        @SerialPort(FAKE) fake: SerialPortRepository
    ) = if (BuildConfig.FLAVOR == "demo") fake else real
}