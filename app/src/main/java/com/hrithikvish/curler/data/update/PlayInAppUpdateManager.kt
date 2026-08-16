package com.hrithikvish.curler.data.update

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.hrithikvish.curler.data.common.await
import com.hrithikvish.curler.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

// Statuses Play Core reports while a flexible update is actively in flight
// but hasn't yet handed us real byte counts via the InstallStateUpdatedListener.
private val ACTIVE_DOWNLOAD_STATUSES = setOf(InstallStatus.DOWNLOADING, InstallStatus.PENDING, InstallStatus.INSTALLING)

@Singleton
class PlayInAppUpdateManager @Inject constructor(
    private val appUpdateManager: AppUpdateManager,
    @ApplicationScope private val scope: CoroutineScope,
) : UpdateManager {

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    override val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    // Each AppUpdateInfo can only be used to start one update flow, per Play
    // Core's contract — cached from the last successful checkForUpdate().
    private var latestAppUpdateInfo: AppUpdateInfo? = null
    private var launcher: ActivityResultLauncher<IntentSenderRequest>? = null

    init {
        // Registered once, for the process lifetime — mirrors AppUpdateManager's
        // own singleton lifetime, so it's never unregistered.
        appUpdateManager.registerListener { state ->
            when (state.installStatus()) {
                InstallStatus.DOWNLOADING ->
                    _updateState.value = UpdateState.Downloading(state.bytesDownloaded(), state.totalBytesToDownload())
                InstallStatus.DOWNLOADED, InstallStatus.INSTALLING ->
                    _updateState.value = UpdateState.Downloaded
                InstallStatus.PENDING ->
                    _updateState.value = UpdateState.Downloading(0, 0)
                InstallStatus.FAILED, InstallStatus.CANCELED ->
                    // Self-heal by re-deriving true state from a fresh live query
                    // instead of getting stuck on a dead-end failure.
                    scope.launch { checkForUpdate() }
                else ->
                    _updateState.value = UpdateState.Idle
            }
        }
    }

    override suspend fun checkForUpdate() {
        val info = runCatching { appUpdateManager.appUpdateInfo.await() }.getOrNull()
        if (info == null) {
            latestAppUpdateInfo = null
            _updateState.value = UpdateState.Idle
            return
        }
        latestAppUpdateInfo = info
        _updateState.value = when {
            info.installStatus() == InstallStatus.DOWNLOADED -> UpdateState.Downloaded
            info.installStatus() in ACTIVE_DOWNLOAD_STATUSES -> UpdateState.Downloading(0, 0)
            info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE) -> UpdateState.Available(info.availableVersionCode())
            info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> UpdateState.Downloading(0, 0)
            else -> UpdateState.Idle
        }
    }

    override fun startUpdate() {
        val info = latestAppUpdateInfo ?: return
        val activeLauncher = launcher ?: return
        appUpdateManager.startUpdateFlowForResult(
            info,
            activeLauncher,
            AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build(),
        )
    }

    override fun completeUpdate() {
        appUpdateManager.completeUpdate()
    }

    override fun attachLauncher(launcher: ActivityResultLauncher<IntentSenderRequest>) {
        this.launcher = launcher
    }

    override fun detachLauncher() {
        launcher = null
    }
}
