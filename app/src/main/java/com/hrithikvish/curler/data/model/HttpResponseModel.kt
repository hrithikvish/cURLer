package com.hrithikvish.curler.data.model

data class HttpResponseModel(
    val statusCode: Int,
    val statusMessage: String,
    val headers: List<Pair<String, String>>,
    val body: String,
    val isBodyJson: Boolean,
    val durationMs: Long,
    val sizeBytes: Long,
)
