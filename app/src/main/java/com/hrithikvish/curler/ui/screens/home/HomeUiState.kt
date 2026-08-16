package com.hrithikvish.curler.ui.screens.home

import com.hrithikvish.curler.data.model.HistoryEntry
import com.hrithikvish.curler.data.update.UpdateState

data class HomeUiState(
    val entries: List<HistoryEntry> = emptyList(),
    val searchQuery: String = "",
    val updateState: UpdateState = UpdateState.Idle,
) {
    val filteredEntries: List<HistoryEntry>
        get() = if (searchQuery.isBlank()) {
            entries
        } else {
            entries.filter { it.request.url.contains(searchQuery, ignoreCase = true) }
        }
}
