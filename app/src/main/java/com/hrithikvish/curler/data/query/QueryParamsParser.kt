package com.hrithikvish.curler.data.query

import java.net.URLDecoder

/**
 * Manual string parsing rather than android.net.Uri, so this is JVM-unit-testable.
 */
object QueryParamsParser {

    fun parse(url: String): List<Pair<String, String>> {
        val queryStart = url.indexOf('?')
        if (queryStart == -1 || queryStart == url.length - 1) return emptyList()

        var query = url.substring(queryStart + 1)
        val fragmentStart = query.indexOf('#')
        if (fragmentStart >= 0) query = query.substring(0, fragmentStart)
        if (query.isBlank()) return emptyList()

        return query.split("&")
            .filter { it.isNotEmpty() }
            .map { pair ->
                val eqIdx = pair.indexOf('=')
                if (eqIdx == -1) {
                    decode(pair) to ""
                } else {
                    decode(pair.substring(0, eqIdx)) to decode(pair.substring(eqIdx + 1))
                }
            }
    }

    private fun decode(s: String): String =
        runCatching { URLDecoder.decode(s, "UTF-8") }.getOrDefault(s)
}
