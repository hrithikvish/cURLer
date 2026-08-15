package com.hrithikvish.curler.ui.screens.newrequest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hrithikvish.curler.data.curlparser.CurlParser
import com.hrithikvish.curler.data.history.HistoryRepository
import com.hrithikvish.curler.data.model.HistoryEntry
import com.hrithikvish.curler.data.model.HttpMethod
import com.hrithikvish.curler.data.model.HttpRequestModel
import com.hrithikvish.curler.data.model.HttpResponseModel
import com.hrithikvish.curler.data.model.ParsedCurlRequest
import com.hrithikvish.curler.data.network.RequestExecutor
import com.hrithikvish.curler.ui.screens.review.ReviewCapable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RequestFlowViewModel @Inject constructor(
    private val historyRepository: HistoryRepository,
    private val requestExecutor: RequestExecutor,
) : ViewModel(), ReviewCapable {

    private val _uiState = MutableStateFlow(RequestFlowUiState())
    val uiState: StateFlow<RequestFlowUiState> = _uiState.asStateFlow()

    private val _request = MutableStateFlow(HttpRequestModel(HttpMethod.GET, ""))
    override val request: StateFlow<HttpRequestModel> = _request.asStateFlow()

    private val _response = MutableStateFlow<HttpResponseModel?>(null)
    val response: StateFlow<HttpResponseModel?> = _response.asStateFlow()

    private var rawCurlTextForHistory: String? = null

    val recentChips: StateFlow<List<RecentCurlChip>> = historyRepository.observeHistory()
        .map { entries ->
            entries.mapNotNull { it.rawCurlText }
                .distinct()
                .take(5)
                .map { raw ->
                    val parsed = CurlParser.parse(raw)
                    val method = (parsed as? ParsedCurlRequest.Success)?.method ?: HttpMethod.GET
                    val url = (parsed as? ParsedCurlRequest.Success)?.url ?: raw
                    RecentCurlChip(method = method, label = shortLabel(url), rawCurlText = raw)
                }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun selectTab(tab: NewRequestTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun updatePasteText(text: String) {
        _uiState.update { it.copy(pasteText = text, pasteError = null) }
    }

    fun applyRecentChip(rawCurlText: String) {
        _uiState.update { it.copy(selectedTab = NewRequestTab.Paste, pasteText = rawCurlText, pasteError = null) }
    }

    fun updateBuildMethod(method: HttpMethod) {
        _uiState.update { it.copy(buildMethod = method) }
    }

    fun updateBuildUrl(url: String) {
        _uiState.update { it.copy(buildUrl = url, buildError = null) }
    }

    fun updateHeaderKey(index: Int, key: String) {
        _uiState.update { state ->
            state.copy(buildHeaders = state.buildHeaders.mapIndexed { i, h -> if (i == index) h.copy(key = key) else h })
        }
    }

    fun updateHeaderValue(index: Int, value: String) {
        _uiState.update { state ->
            state.copy(buildHeaders = state.buildHeaders.mapIndexed { i, h -> if (i == index) h.copy(value = value) else h })
        }
    }

    fun addHeaderRow() {
        _uiState.update { it.copy(buildHeaders = it.buildHeaders + HeaderFieldState()) }
    }

    fun removeHeaderRow(index: Int) {
        _uiState.update { state ->
            val updated = state.buildHeaders.toMutableList().apply { removeAt(index) }
            state.copy(buildHeaders = updated.ifEmpty { listOf(HeaderFieldState()) })
        }
    }

    fun updateBuildBodyMode(mode: BodyMode) {
        _uiState.update { it.copy(buildBodyMode = mode) }
    }

    fun updateBuildBodyJson(json: String) {
        _uiState.update { it.copy(buildBodyJson = json) }
    }

    /** Called by both Paste's "Parse & validate" and Build's "Continue to review". */
    fun validateAndBuildRequest(): Boolean {
        val state = _uiState.value
        return when (state.selectedTab) {
            NewRequestTab.Paste -> validateFromPaste(state)
            NewRequestTab.Build -> validateFromBuild(state)
        }
    }

    private fun validateFromPaste(state: RequestFlowUiState): Boolean {
        when (val parsed = CurlParser.parse(state.pasteText)) {
            is ParsedCurlRequest.Success -> {
                _request.value = HttpRequestModel(
                    method = parsed.method,
                    url = parsed.url,
                    headers = parsed.headers,
                    body = parsed.body,
                )
                rawCurlTextForHistory = state.pasteText
                _uiState.update { it.copy(pasteError = null) }
                return true
            }
            is ParsedCurlRequest.Error -> {
                _uiState.update { it.copy(pasteError = parsed.message) }
                return false
            }
        }
    }

    private fun validateFromBuild(state: RequestFlowUiState): Boolean {
        val url = state.buildUrl.trim()
        if (url.isEmpty()) {
            _uiState.update { it.copy(buildError = BuildUrlError.REQUIRED) }
            return false
        }
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            _uiState.update { it.copy(buildError = BuildUrlError.INVALID_SCHEME) }
            return false
        }

        val headers = state.buildHeaders
            .filter { it.key.isNotBlank() }
            .map { it.key.trim() to it.value.trim() }
        val body = if (state.buildMethod == HttpMethod.GET) {
            null
        } else {
            when (state.buildBodyMode) {
                BodyMode.JSON -> state.buildBodyJson.ifBlank { null }
                BodyMode.FORM, BodyMode.NONE -> null
            }
        }

        _request.value = HttpRequestModel(
            method = state.buildMethod,
            url = url,
            headers = headers,
            body = body,
        )
        rawCurlTextForHistory = null
        _uiState.update { it.copy(buildError = null) }
        return true
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

        persistHistory(result)
        return result
    }

    private fun persistHistory(result: Result<HttpResponseModel>) {
        viewModelScope.launch {
            historyRepository.insert(
                HistoryEntry(
                    timestamp = System.currentTimeMillis(),
                    rawCurlText = rawCurlTextForHistory,
                    request = _request.value,
                    response = result.getOrNull(),
                    errorMessage = result.exceptionOrNull()?.message,
                )
            )
        }
    }

    private fun shortLabel(url: String): String {
        val withoutScheme = url.substringAfter("://", url)
        val path = withoutScheme.substringAfter('/', "")
        return if (path.isEmpty()) "/" else "/$path".substringBefore('?')
    }
}
