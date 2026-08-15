package com.hrithikvish.curler.ui.screens.home

import com.hrithikvish.curler.data.model.HistoryEntry

data class HomeUiState(
    val entries: List<HistoryEntry> = emptyList(),
    val searchQuery: String = "",
) {
    val filteredEntries: List<HistoryEntry>
        get() = if (searchQuery.isBlank()) {
            entries
        } else {
            entries.filter { it.request.url.contains(searchQuery, ignoreCase = true) }
        }
}
