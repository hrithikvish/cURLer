package com.hrithikvish.curler.data.model

data class HistoryEntry(
    val id: Long = 0,
    val timestamp: Long,
    val rawCurlText: String?,
    val request: HttpRequestModel,
    val response: HttpResponseModel?,
    val errorMessage: String?,
)
