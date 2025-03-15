package com.connor.episode.core.di

import com.connor.episode.core.di.NetType.TCP
import com.connor.episode.core.di.NetType.UDP
import com.connor.episode.core.di.NetType.WebSocket
import com.connor.episode.data.remote.network.NetworkClient
import com.connor.episode.data.remote.network.NetworkServer
import com.connor.episode.data.remote.network.tcp.TCPClientImpl
import com.connor.episode.data.remote.network.tcp.TCPServerImpl
import com.connor.episode.data.remote.network.udp.UDPClientImpl
import com.connor.episode.data.remote.network.udp.UDPServerImpl
import com.connor.episode.data.remote.network.wss.WebSocketsClientImpl
import com.connor.episode.data.remote.network.wss.WebSocketsServerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {

    @Binds
    @Singleton
    @Server(TCP)
    abstract fun bindTCPServerDataSource(impl: TCPServerImpl): NetworkServer

    @Binds
    @Singleton
    @Server(UDP)
    abstract fun bindUDPServerDataSource(impl: UDPServerImpl): NetworkServer

    @Binds
    @Singleton
    @Server(WebSocket)
    abstract fun bindWSSServerDataSource(impl: WebSocketsServerImpl): NetworkServer

    @Binds
    @Singleton
    @Client(TCP)
    abstract fun bindTCPClientDataSource(impl: TCPClientImpl): NetworkClient

    @Binds
    @Singleton
    @Client(UDP)
    abstract fun bindUDPClientDataSource(impl: UDPClientImpl): NetworkClient

    @Binds
    @Singleton
    @Client(WebSocket)
    abstract fun bindWSClientDataSource(impl: WebSocketsClientImpl): NetworkClient

}