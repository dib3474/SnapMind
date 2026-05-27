package com.example.snapmind.di

import com.example.snapmind.core.coroutine.DefaultDispatcherProvider
import com.example.snapmind.core.coroutine.DispatcherProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DispatcherModule {
    @Binds
    @Singleton
    abstract fun bindDispatcherProvider(
        provider: DefaultDispatcherProvider,
    ): DispatcherProvider
}
