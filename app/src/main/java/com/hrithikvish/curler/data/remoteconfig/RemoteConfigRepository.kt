package com.hrithikvish.curler.data.remoteconfig

import com.google.android.gms.tasks.Task
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

// Thin, feature-agnostic wrapper around FirebaseRemoteConfig: fetch/activate
// plumbing and raw value access live here once. Feature repositories (e.g.
// AboutConfigRepository) depend on this and own only their key + DTO
// mapping, so adding a new Remote Config-backed feature never re-implements
// the fetch/await/error-swallow dance.
@Singleton
class RemoteConfigRepository @Inject constructor(
    private val remoteConfig: FirebaseRemoteConfig,
) {

    // Fetches (respecting the configured minimum fetch interval) and, on
    // success, activates the new values so subsequent getString() calls see
    // them. Failures (offline, throttled) are swallowed — the previously
    // activated values or in-app defaults remain in effect, which is the
    // correct fallback for a non-critical config screen.
    suspend fun refresh() {
        runCatching { remoteConfig.fetchAndActivate().await() }
    }

    fun getString(key: String): String = remoteConfig.getString(key)
}

private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { continuation ->
    addOnCompleteListener { task ->
        if (task.isSuccessful) {
            continuation.resume(task.result)
        } else {
            continuation.resumeWithException(task.exception ?: IllegalStateException("Task failed with no exception"))
        }
    }
}
