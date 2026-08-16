package com.hrithikvish.curler.data.curlparser

/**
 * Splits a curl command string into shell-like tokens using the same
 * stateful word-accumulation POSIX shells (and Python's `shlex`) use:
 * quoted and unquoted fragments glue together into a single token, and a
 * quote may open mid-word rather than only at the start of one. Handles
 * single quotes (fully literal), double quotes (backslash escapes for
 * backslash, double-quote, dollar, backtick), and Bash's ANSI-C `$'...'`
 * quoting with its own backslash escapes (\n, \t, \r) — curl commands
 * copied from mobile/network logs sometimes wrap `--data` in `$'...'`
 * because the payload contains literal spaces.
 */
fun tokenizeCurl(input: String): List<String> {
    val tokens = mutableListOf<String>()
    var word: StringBuilder? = null
    var i = 0
    val n = input.length

    fun currentWord(): StringBuilder = word ?: StringBuilder().also { word = it }

    while (i < n) {
        val c = input[i]
        when {
            c.isWhitespace() -> {
                word?.let { tokens.add(it.toString()) }
                word = null
                i++
            }
            c == '\'' -> {
                val sb = currentWord()
                i++
                while (i < n && input[i] != '\'') {
                    sb.append(input[i])
                    i++
                }
                i++
            }
            c == '"' -> {
                val sb = currentWord()
                i++
                while (i < n && input[i] != '"') {
                    if (input[i] == '\\' && i + 1 < n && input[i + 1] in DOUBLE_QUOTE_ESCAPABLE) {
                        sb.append(input[i + 1])
                        i += 2
                    } else {
                        sb.append(input[i])
                        i++
                    }
                }
                i++
            }
            c == '$' && i + 1 < n && input[i + 1] == '\'' -> {
                val sb = currentWord()
                i += 2
                while (i < n && input[i] != '\'') {
                    if (input[i] == '\\' && i + 1 < n) {
                        sb.append(decodeAnsiCEscape(input[i + 1]))
                        i += 2
                    } else {
                        sb.append(input[i])
                        i++
                    }
                }
                i++
            }
            c == '\\' && i + 1 < n -> {
                currentWord().append(input[i + 1])
                i += 2
            }
            else -> {
                currentWord().append(c)
                i++
            }
        }
    }
    word?.let { tokens.add(it.toString()) }
    return tokens
}

private const val DOUBLE_QUOTE_ESCAPABLE = "\\\"$`"

private fun decodeAnsiCEscape(escaped: Char): String = when (escaped) {
    'n' -> "\n"
    't' -> "\t"
    'r' -> "\r"
    '\\' -> "\\"
    '\'' -> "'"
    '"' -> "\""
    else -> "\\$escaped"
}
