package com.hrithikvish.curler.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hrithikvish.curler.data.history.HistoryRepository
import com.hrithikvish.curler.data.model.HistoryEntry
import com.hrithikvish.curler.data.update.UpdateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val historyRepository: HistoryRepository,
    private val updateManager: UpdateManager,
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")

    val uiState: StateFlow<HomeUiState> = combine(
        historyRepository.observeHistory(),
        searchQuery,
        updateManager.updateState,
    ) { entries, query, updateState ->
        HomeUiState(
            entries = entries,
            searchQuery = query,
            updateState = updateState
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState()
    )

    fun onSearchQueryChange(query: String) {
        searchQuery.value = query
    }

    fun deleteEntry(entry: HistoryEntry) {
        viewModelScope.launch { historyRepository.delete(entry) }
    }

    fun onUpdateAction() = updateManager.performAction()
}
