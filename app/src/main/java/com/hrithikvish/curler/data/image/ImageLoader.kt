package com.hrithikvish.curler.data.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

// Minimal OkHttp-backed bitmap fetcher so a single remote image (the dev
// avatar) doesn't pull in an image-loading library. Not a general-purpose
// replacement for one — no disk cache, request de-duplication, resizing, or
// placeholder/transition support; if the app needs more than a couple of
// remote images, reach for Coil instead of growing this.
@Singleton
class ImageLoader @Inject constructor(
    private val client: OkHttpClient,
) {
    private val cache = ConcurrentHashMap<String, Bitmap>()

    suspend fun load(url: String): Bitmap? = withContext(Dispatchers.IO) {
        cache[url]?.let { return@withContext it }

        val bitmap = runCatching {
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@use null
                val bytes = response.body?.bytes() ?: return@use null
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            }
        }.getOrNull()

        bitmap?.also { cache[url] = it }
    }
}
