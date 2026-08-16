package com.hrithikvish.curler.data.update

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import kotlinx.coroutines.flow.StateFlow

// Single source of truth for in-app update status, shared by every screen
// that shows update UI. MainActivity is the only Activity-aware caller
// (attach/detachLauncher); ViewModels only ever read [updateState] and call
// [performAction].
interface UpdateManager {
    val updateState: StateFlow<UpdateState>

    suspend fun checkForUpdate()

    fun startUpdate()

    fun completeUpdate()

    // Single entry point for the one action button both UpdateBar and
    // UpdateRow expose — keeps the "which state does what" dispatch defined
    // exactly once, instead of duplicated per ViewModel.
    fun performAction() {
        when (updateState.value) {
            is UpdateState.Available -> startUpdate()
            UpdateState.Downloaded -> completeUpdate()
            else -> Unit
        }
    }

    fun attachLauncher(launcher: ActivityResultLauncher<IntentSenderRequest>)

    fun detachLauncher()
}
