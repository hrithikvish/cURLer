package com.hrithikvish.curler.data.curlparser

private val TOKEN_REGEX = Regex("\"([^\"]*)\"|'([^']*)'|(\\S+)")

/**
 * Splits a curl command string into shell-like tokens, honoring single-
 * and double-quoted segments (which may contain spaces). Kotlin regex
 * groups are null (not empty string) when non-participating, so each
 * alternative must be checked in order rather than relying on JS-style
 * `undefined` coalescing.
 */
fun tokenizeCurl(input: String): List<String> {
    return TOKEN_REGEX.findAll(input).map { m ->
        m.groups[1]?.value ?: m.groups[2]?.value ?: m.groups[3]?.value ?: ""
    }.toList()
}
