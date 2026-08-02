package com.nerojust.vela.di

import com.nerojust.vela.core.network.FakeVelaApiService
import com.nerojust.vela.core.network.NetworkConditions
import com.nerojust.vela.core.network.VelaApiService
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkModule {
    @Binds
    @Singleton
    abstract fun bindVelaApiService(impl: FakeVelaApiService): VelaApiService

    companion object {
        @Provides
        @Singleton
        fun provideNetworkConditions(): NetworkConditions =
            NetworkConditions(
                simulatedLatencyMillis = 600,
                failureRate = 0.35,
            )
    }
}
