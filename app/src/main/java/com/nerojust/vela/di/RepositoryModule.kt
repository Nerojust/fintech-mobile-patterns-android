package com.nerojust.vela.di

import com.nerojust.vela.core.common.DefaultDispatcherProvider
import com.nerojust.vela.core.common.DispatcherProvider
import com.nerojust.vela.data.CardRepositoryImpl
import com.nerojust.vela.data.FxRateRepositoryImpl
import com.nerojust.vela.data.TransactionRepositoryImpl
import com.nerojust.vela.domain.RetryPolicy
import com.nerojust.vela.domain.repository.CardRepository
import com.nerojust.vela.domain.repository.FxRateRepository
import com.nerojust.vela.domain.repository.TransactionRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindCardRepository(impl: CardRepositoryImpl): CardRepository

    @Binds
    @Singleton
    abstract fun bindFxRateRepository(impl: FxRateRepositoryImpl): FxRateRepository

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(impl: TransactionRepositoryImpl): TransactionRepository

    companion object {
        @Provides
        @Singleton
        fun provideDispatcherProvider(): DispatcherProvider = DefaultDispatcherProvider()

        @Provides
        @Singleton
        fun provideRetryPolicy(): RetryPolicy = RetryPolicy()
    }
}
