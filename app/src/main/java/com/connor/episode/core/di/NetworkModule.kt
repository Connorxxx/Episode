package com.connor.episode.core.di

import com.connor.episode.core.di.Dispatcher.EpisodeDispatchers.IO
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.pingInterval
import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.TcpSocketBuilder
import io.ktor.network.sockets.UDPSocketBuilder
import io.ktor.network.sockets.aSocket
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideSelectorManager(
        @Dispatcher(IO) dispatcher: CoroutineDispatcher
    ): SelectorManager {
        return SelectorManager(dispatcher)
    }

    @Provides
    @Singleton
    fun provideTCPSocketBuilder(
        selectorManager: SelectorManager
    ): TcpSocketBuilder = aSocket(selectorManager).tcp()

    @Provides
    @Singleton
    fun provideUDPSocketBuilder(
        selectorManager: SelectorManager
    ): UDPSocketBuilder = aSocket(selectorManager).udp()

    @Provides
    @Singleton
    fun webSocketClient() = HttpClient(CIO) {
        install(WebSockets) {
            pingInterval = 5.seconds
        }
    }

}