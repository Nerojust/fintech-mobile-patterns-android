package com.nerojust.vela.di

import android.content.Context
import androidx.room.Room
import com.nerojust.vela.core.database.OutboxDao
import com.nerojust.vela.core.database.VelaDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideVelaDatabase(
        @ApplicationContext context: Context,
    ): VelaDatabase = Room.databaseBuilder(context, VelaDatabase::class.java, "vela.db").build()

    @Provides
    @Singleton
    fun provideOutboxDao(database: VelaDatabase): OutboxDao = database.outboxDao()
}
