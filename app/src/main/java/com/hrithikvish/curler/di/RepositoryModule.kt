package com.hrithikvish.curler.di

import com.hrithikvish.curler.data.network.OkHttpRequestExecutor
import com.hrithikvish.curler.data.network.RequestExecutor
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindRequestExecutor(impl: OkHttpRequestExecutor): RequestExecutor
}
