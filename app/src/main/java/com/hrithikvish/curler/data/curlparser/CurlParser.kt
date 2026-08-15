package com.hrithikvish.curler.data.curlparser

import com.hrithikvish.curler.data.model.HttpMethod
import com.hrithikvish.curler.data.model.ParsedCurlRequest
import kotlinx.serialization.json.Json

private val LINE_CONTINUATION_REGEX = Regex("\\\\\\s*\\n")
private val URL_SCHEME_REGEX = Regex("^https?://")

/**
 * Direct Kotlin port of the wireframe's reference JS `parseCurl`/`tokenize`
 * algorithm.
 */
object CurlParser {

    fun parse(raw: String): ParsedCurlRequest {
        val cleaned = raw.replace(LINE_CONTINUATION_REGEX, " ").trim()
        val tokens = tokenizeCurl(cleaned)

        if (tokens.isEmpty() || tokens[0] != "curl") {
            return ParsedCurlRequest.Error("Command should start with \"curl\"")
        }

        var rawMethod: String? = null
        var url: String? = null
        val headers = mutableListOf<Pair<String, String>>()
        var body: String? = null

        var i = 1
        while (i < tokens.size) {
            when (val t = tokens[i]) {
                "-X", "--request" -> {
                    i++
                    rawMethod = tokens.getOrNull(i)
                }
                "-H", "--header" -> {
                    i++
                    val h = tokens.getOrNull(i) ?: ""
                    val idx = h.indexOf(':')
                    if (idx > -1) {
                        headers.add(h.substring(0, idx).trim() to h.substring(idx + 1).trim())
                    } else {
                        return ParsedCurlRequest.Error("Header \"$h\" is missing a colon")
                    }
                }
                "-d", "--data", "--data-raw", "--data-binary" -> {
                    i++
                    body = tokens.getOrNull(i)
                }
                "-u", "--user" -> {
                    i++
                    val userPass = tokens.getOrNull(i) ?: ""
                    val encoded = java.util.Base64.getEncoder()
                        .encodeToString(userPass.toByteArray(Charsets.UTF_8))
                    headers.add("Authorization" to "Basic $encoded")
                }
                "--url" -> {
                    i++
                    url = tokens.getOrNull(i)
                }
                else -> {
                    if (!t.startsWith("-") && url == null) {
                        val urlParts = mutableListOf(t)
                        while (i + 1 < tokens.size && !tokens[i + 1].startsWith("-")) {
                            i++
                            urlParts.add(tokens[i])
                        }
                        url = urlParts.joinToString(" ")
                    }
                }
            }
            i++
        }

        if (url == null) {
            return ParsedCurlRequest.Error("No URL found in the command")
        }
        if (!URL_SCHEME_REGEX.containsMatchIn(url)) {
            return ParsedCurlRequest.Error("URL should start with http:// or https:// — got \"$url\"")
        }

        val method = parseMethod(rawMethod, hasBody = body != null)

        val bodyIsValidJson = if (body != null) {
            runCatching { Json.parseToJsonElement(body) }.isSuccess
        } else {
            false
        }

        return ParsedCurlRequest.Success(
            method = method,
            rawMethod = rawMethod ?: method.name,
            url = url,
            headers = headers,
            body = body,
            bodyIsValidJson = bodyIsValidJson,
        )
    }

    private fun parseMethod(rawMethod: String?, hasBody: Boolean): HttpMethod {
        if (rawMethod != null) {
            HttpMethod.entries.firstOrNull { it.name.equals(rawMethod, ignoreCase = true) }
                ?.let { return it }
        }
        return if (hasBody) HttpMethod.POST else HttpMethod.GET
    }
}
