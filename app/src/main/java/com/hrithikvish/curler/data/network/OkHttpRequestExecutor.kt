package com.hrithikvish.curler.data.network

import com.hrithikvish.curler.data.model.HttpMethod
import com.hrithikvish.curler.data.model.HttpRequestModel
import com.hrithikvish.curler.data.model.HttpResponseModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import javax.inject.Inject

private val DEFAULT_CONTENT_TYPE = "application/json; charset=utf-8".toMediaType()

class OkHttpRequestExecutor @Inject constructor(
    private val client: OkHttpClient,
) : RequestExecutor {

    override suspend fun execute(request: HttpRequestModel): Result<HttpResponseModel> {
        return withContext(Dispatchers.IO) {
            try {
                val okRequest = buildRequest(request)
                val startTime = System.currentTimeMillis()
                client.newCall(okRequest).execute().use { response ->
                    val durationMs = System.currentTimeMillis() - startTime
                    val bodyBytes = response.body?.bytes() ?: ByteArray(0)
                    val bodyString = bodyBytes.toString(Charsets.UTF_8)
                    val sizeBytes = response.body?.contentLength()?.takeIf { it >= 0 }
                        ?: bodyBytes.size.toLong()
                    val isBodyJson = runCatching { Json.parseToJsonElement(bodyString) }.isSuccess

                    Result.success(
                        HttpResponseModel(
                            statusCode = response.code,
                            statusMessage = response.message,
                            headers = response.headers.toList(),
                            body = bodyString,
                            isBodyJson = isBodyJson,
                            durationMs = durationMs,
                            sizeBytes = sizeBytes,
                        )
                    )
                }
            } catch (e: IOException) {
                Result.failure(e)
            }
        }
    }

    private fun buildRequest(request: HttpRequestModel): Request {
        val builder = Request.Builder().url(request.url)
        for ((key, value) in request.headers) {
            builder.addHeader(key, value)
        }

        val contentType = request.headers
            .firstOrNull { it.first.equals("Content-Type", ignoreCase = true) }
            ?.second
            ?.toMediaTypeOrNull()
            ?: DEFAULT_CONTENT_TYPE

        when (request.method) {
            HttpMethod.GET -> builder.get()
            HttpMethod.HEAD -> builder.head()
            HttpMethod.DELETE, HttpMethod.OPTIONS -> {
                val body = request.body?.toRequestBody(contentType)
                builder.method(request.method.name, body)
            }
            HttpMethod.POST, HttpMethod.PUT, HttpMethod.PATCH -> {
                val body = if (request.body.isNullOrEmpty()) {
                    "".toRequestBody(null)
                } else {
                    request.body.toRequestBody(contentType)
                }
                builder.method(request.method.name, body)
            }
        }

        return builder.build()
    }
}
