package com.hrithikvish.curler.data.common

import com.google.android.gms.tasks.Task
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

// Shared bridge from Google Play Services' Task<T> to coroutines — used by
// every repository/manager wrapping a Play Services SDK (Firebase Remote
// Config, Play Core), so the await/error-propagate dance is written once.
internal suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { continuation ->
    addOnCompleteListener { task ->
        if (task.isSuccessful) {
            continuation.resume(task.result)
        } else {
            continuation.resumeWithException(task.exception ?: IllegalStateException("Task failed with no exception"))
        }
    }
}
