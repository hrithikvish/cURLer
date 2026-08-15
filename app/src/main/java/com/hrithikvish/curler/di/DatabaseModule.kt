package com.hrithikvish.curler.di

import android.content.Context
import androidx.room.Room
import com.hrithikvish.curler.data.history.CurlerDatabase
import com.hrithikvish.curler.data.history.HistoryDao
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
    fun provideCurlerDatabase(@ApplicationContext context: Context): CurlerDatabase =
        Room.databaseBuilder(context, CurlerDatabase::class.java, "curler.db").build()

    @Provides
    fun provideHistoryDao(database: CurlerDatabase): HistoryDao = database.historyDao()
}
