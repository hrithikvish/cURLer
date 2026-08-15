package com.hrithikvish.curler.data.history

import androidx.room.TypeConverter
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.add
import kotlinx.serialization.json.addJsonArray
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

class Converters {

    @TypeConverter
    fun fromHeaders(headers: List<Pair<String, String>>): String {
        val array: JsonArray = buildJsonArray {
            for ((key, value) in headers) {
                addJsonArray {
                    add(JsonPrimitive(key))
                    add(JsonPrimitive(value))
                }
            }
        }
        return array.toString()
    }

    @TypeConverter
    fun toHeaders(value: String): List<Pair<String, String>> {
        if (value.isBlank()) return emptyList()
        return Json.parseToJsonElement(value).jsonArray.map { entry ->
            val pair = entry.jsonArray
            pair[0].jsonPrimitive.content to pair[1].jsonPrimitive.content
        }
    }
}
