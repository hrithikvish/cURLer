package com.hrithikvish.curler.data.update

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeUpdateManager(initialState: UpdateState = UpdateState.Idle) : UpdateManager {
    private val _updateState = MutableStateFlow(initialState)
    override val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    var checkForUpdateCallCount = 0
        private set
    var startUpdateCallCount = 0
        private set
    var completeUpdateCallCount = 0
        private set

    fun emit(state: UpdateState) {
        _updateState.value = state
    }

    override suspend fun checkForUpdate() {
        checkForUpdateCallCount++
    }

    override fun startUpdate() {
        startUpdateCallCount++
    }

    override fun completeUpdate() {
        completeUpdateCallCount++
    }

    override fun attachLauncher(launcher: ActivityResultLauncher<IntentSenderRequest>) = Unit

    override fun detachLauncher() = Unit
}
