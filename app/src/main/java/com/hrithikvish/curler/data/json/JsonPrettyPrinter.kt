package com.hrithikvish.curler.data.json

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

object JsonPrettyPrinter {
    private val prettyJson = Json {
        prettyPrint = true
        prettyPrintIndent = "  "
    }

    /** Returns the pretty-printed form of [raw], or null if it is not valid JSON. */
    fun prettyPrintOrNull(raw: String): String? = runCatching {
        val element = Json.parseToJsonElement(raw)
        prettyJson.encodeToString(JsonElement.serializer(), element)
    }.getOrNull()

    /** Returns the pretty-printed form of [raw], falling back to [raw] unchanged if invalid. */
    fun prettyPrint(raw: String): String = prettyPrintOrNull(raw) ?: raw
}
