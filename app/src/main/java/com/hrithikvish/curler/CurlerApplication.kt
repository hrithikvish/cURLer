package com.hrithikvish.curler

import android.app.Application
import com.hrithikvish.curler.data.remoteconfig.RemoteConfigRepository
import com.hrithikvish.curler.data.update.UpdateManager
import com.hrithikvish.curler.di.ApplicationScope
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class CurlerApplication : Application() {

    @Inject
    lateinit var remoteConfigRepository: RemoteConfigRepository

    @Inject
    lateinit var updateManager: UpdateManager

    @Inject
    @ApplicationScope
    lateinit var applicationScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()
        // Warm Remote Config up front so it's already fetched/activated by
        // the time any screen (About, and whatever else uses it later) reads
        // from it — not just configured lazily on first injection.
        applicationScope.launch {
            remoteConfigRepository.refresh()
        }
        // Earliest possible update-availability signal — Home/About render
        // the correct state on first composition instead of starting Idle.
        applicationScope.launch {
            updateManager.checkForUpdate()
        }
    }
}
