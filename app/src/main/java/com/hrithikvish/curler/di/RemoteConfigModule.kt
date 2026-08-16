package com.hrithikvish.curler.di

import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.hrithikvish.curler.BuildConfig
import com.hrithikvish.curler.data.remoteconfig.RemoteConfigDefaults
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RemoteConfigModule {

    // 0 in debug so every fetch() call actually hits the network instead of
    // being throttled by the previous fetch, since we're actively iterating
    // on config values; 1 hour in prod, Firebase's own recommended default,
    // so screens don't each pay for a network round trip.
    private const val DEBUG_MIN_FETCH_INTERVAL_SECONDS = 0L
    private const val PROD_MIN_FETCH_INTERVAL_SECONDS = 3600L

    @Provides
    @Singleton
    fun provideFirebaseRemoteConfig(): FirebaseRemoteConfig {
        val remoteConfig = Firebase.remoteConfig
        remoteConfig.setConfigSettingsAsync(
            remoteConfigSettings {
                minimumFetchIntervalInSeconds = when(BuildConfig.DEBUG) {
                    true -> DEBUG_MIN_FETCH_INTERVAL_SECONDS
                    false -> PROD_MIN_FETCH_INTERVAL_SECONDS
                }
            },
        )
        remoteConfig.setDefaultsAsync(RemoteConfigDefaults.values)
        return remoteConfig
    }
}
