package com.hrithikvish.curler.ui.screens.review

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.hrithikvish.curler.data.history.HistoryRepository
import com.hrithikvish.curler.data.model.HistoryEntry
import com.hrithikvish.curler.data.model.HttpMethod
import com.hrithikvish.curler.data.model.HttpRequestModel
import com.hrithikvish.curler.data.model.HttpResponseModel
import com.hrithikvish.curler.data.network.RequestExecutor
import com.hrithikvish.curler.ui.navigation.CurlerRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryReviewUiState(
    val isLoading: Boolean = true,
    val isSending: Boolean = false,
    val sendError: String? = null,
)

@HiltViewModel
class HistoryReviewViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val historyRepository: HistoryRepository,
    private val requestExecutor: RequestExecutor,
) : ViewModel(), ReviewCapable {

    private val historyId: Long = savedStateHandle.toRoute<CurlerRoute.HistoryFlowGraph>().historyId

    private val _uiState = MutableStateFlow(HistoryReviewUiState())
    val uiState: StateFlow<HistoryReviewUiState> = _uiState.asStateFlow()

    private val _request = MutableStateFlow(HttpRequestModel(HttpMethod.GET, ""))
    override val request: StateFlow<HttpRequestModel> = _request.asStateFlow()

    private val _response = MutableStateFlow<HttpResponseModel?>(null)
    val response: StateFlow<HttpResponseModel?> = _response.asStateFlow()

    private var rawCurlText: String? = null

    /** Guards the one-time auto-navigation to the saved response when this flow is first opened. */
    var didAutoOpenSavedResponse: Boolean = false

    init {
        viewModelScope.launch {
            var loadedError: String? = null
            historyRepository.getById(historyId)?.let { entry ->
                _request.value = entry.request
                rawCurlText = entry.rawCurlText
                _response.value = entry.response
                loadedError = entry.errorMessage
            }
            _uiState.update { it.copy(isLoading = false, sendError = loadedError) }
        }
    }

    override suspend fun send(): Result<HttpResponseModel> {
        _uiState.update { it.copy(isSending = true, sendError = null) }
        val result = requestExecutor.execute(_request.value)
        _uiState.update { it.copy(isSending = false) }

        result.onSuccess { response ->
            _response.value = response
        }.onFailure { error ->
            _uiState.update { it.copy(sendError = error.message ?: "Network error") }
        }

        viewModelScope.launch {
            historyRepository.insert(
                HistoryEntry(
                    timestamp = System.currentTimeMillis(),
                    rawCurlText = rawCurlText,
                    request = _request.value,
                    response = result.getOrNull(),
                    errorMessage = result.exceptionOrNull()?.message,
                )
            )
        }

        return result
    }
}
