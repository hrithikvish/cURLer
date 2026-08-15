package com.hrithikvish.curler.data.history

import com.hrithikvish.curler.data.model.HistoryEntry
import com.hrithikvish.curler.data.model.HttpMethod
import com.hrithikvish.curler.data.model.HttpRequestModel
import com.hrithikvish.curler.data.model.HttpResponseModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class HistoryRepository @Inject constructor(
    private val dao: HistoryDao,
) {
    fun observeHistory(): Flow<List<HistoryEntry>> =
        dao.observeAll().map { entities -> entities.map { it.toDomain() } }

    suspend fun getById(id: Long): HistoryEntry? = dao.getById(id)?.toDomain()

    suspend fun insert(entry: HistoryEntry): Long = dao.insert(entry.toEntity())

    suspend fun delete(entry: HistoryEntry) = dao.delete(entry.toEntity())
}

private fun HistoryEntity.toDomain(): HistoryEntry = HistoryEntry(
    id = id,
    timestamp = timestamp,
    rawCurlText = rawCurlText,
    request = HttpRequestModel(
        method = HttpMethod.valueOf(method),
        url = url,
        headers = requestHeaders,
        body = requestBody,
    ),
    response = responseStatusCode?.let { statusCode ->
        HttpResponseModel(
            statusCode = statusCode,
            statusMessage = responseStatusMessage.orEmpty(),
            headers = responseHeaders,
            body = responseBody.orEmpty(),
            isBodyJson = responseIsBodyJson ?: false,
            durationMs = responseDurationMs ?: 0,
            sizeBytes = responseSizeBytes ?: 0,
        )
    },
    errorMessage = errorMessage,
)

private fun HistoryEntry.toEntity(): HistoryEntity = HistoryEntity(
    id = id,
    timestamp = timestamp,
    rawCurlText = rawCurlText,
    method = request.method.name,
    url = request.url,
    requestHeaders = request.headers,
    requestBody = request.body,
    responseStatusCode = response?.statusCode,
    responseStatusMessage = response?.statusMessage,
    responseHeaders = response?.headers ?: emptyList(),
    responseBody = response?.body,
    responseIsBodyJson = response?.isBodyJson,
    responseDurationMs = response?.durationMs,
    responseSizeBytes = response?.sizeBytes,
    errorMessage = errorMessage,
)
