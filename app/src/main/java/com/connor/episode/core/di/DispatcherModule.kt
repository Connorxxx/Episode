package com.connor.episode.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier
import com.connor.episode.core.di.Dispatcher.EpisodeDispatchers.Default
import com.connor.episode.core.di.Dispatcher.EpisodeDispatchers.IO


@Module
@InstallIn(SingletonComponent::class)
internal object DispatcherModule {
    @Provides
    @Dispatcher(IO)
    fun providesIODispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Dispatcher(Default)
    fun providesDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default
}

@Qualifier
annotation class Dispatcher(val dispatcher: EpisodeDispatchers) {
    enum class EpisodeDispatchers {
        Default,
        IO,
    }
}