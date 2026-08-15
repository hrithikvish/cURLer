package com.hrithikvish.curler.data.model

data class HttpRequestModel(
    val method: HttpMethod,
    val url: String,
    val headers: List<Pair<String, String>> = emptyList(),
    val body: String? = null,
)
