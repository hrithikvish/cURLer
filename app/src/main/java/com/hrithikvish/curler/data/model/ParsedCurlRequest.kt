package com.hrithikvish.curler.data.model

sealed class ParsedCurlRequest {
    data class Success(
        val method: HttpMethod,
        val rawMethod: String,
        val url: String,
        val headers: List<Pair<String, String>>,
        val body: String?,
        val bodyIsValidJson: Boolean,
    ) : ParsedCurlRequest()

    data class Error(val message: String) : ParsedCurlRequest()
}
