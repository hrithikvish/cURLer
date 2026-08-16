package com.hrithikvish.curler.data.update

// Pure domain model for in-app update status — no Play Core types leak past
// PlayInAppUpdateManager. Every UI surface (Home's bar, About's row, and any
// future one) renders off this single vocabulary.
sealed interface UpdateState {
    data object Idle : UpdateState
    data object Checking : UpdateState
    data class Available(val availableVersionCode: Int) : UpdateState
    data class Downloading(val bytesDownloaded: Long, val totalBytes: Long) : UpdateState {
        // null while Play Core hasn't reported real byte counts yet (e.g. the
        // very first tick after resuming a download) — callers show an
        // indeterminate state rather than a misleading "0%".
        val percent: Int?
            get() = if (totalBytes > 0) ((bytesDownloaded * 100) / totalBytes).toInt() else null
    }
    data object Downloaded : UpdateState
    data class Failed(val message: String) : UpdateState
}
