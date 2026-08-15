package com.hrithikvish.curler.data.history

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val rawCurlText: String?,
    val method: String,
    val url: String,
    val requestHeaders: List<Pair<String, String>>,
    val requestBody: String?,
    val responseStatusCode: Int?,
    val responseStatusMessage: String?,
    val responseHeaders: List<Pair<String, String>>,
    val responseBody: String?,
    val responseIsBodyJson: Boolean?,
    val responseDurationMs: Long?,
    val responseSizeBytes: Long?,
    val errorMessage: String?,
)
