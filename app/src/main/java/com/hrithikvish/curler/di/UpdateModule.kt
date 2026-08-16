package com.hrithikvish.curler.di

import android.content.Context
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.hrithikvish.curler.data.update.PlayInAppUpdateManager
import com.hrithikvish.curler.data.update.UpdateManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class UpdateModule {

    @Binds
    @Singleton
    abstract fun bindUpdateManager(impl: PlayInAppUpdateManager): UpdateManager

    companion object {
        @Provides
        @Singleton
        fun provideAppUpdateManager(@ApplicationContext context: Context): AppUpdateManager =
            AppUpdateManagerFactory.create(context)
    }
}
