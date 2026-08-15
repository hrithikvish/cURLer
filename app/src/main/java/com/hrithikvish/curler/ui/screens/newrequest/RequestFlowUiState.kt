package com.hrithikvish.curler.ui.screens.newrequest

import com.hrithikvish.curler.data.model.HttpMethod

enum class NewRequestTab { Paste, Build }

enum class BodyMode { JSON, FORM, NONE }

data class HeaderFieldState(
    val key: String = "",
    val value: String = "",
)

data class RecentCurlChip(
    val method: HttpMethod,
    val label: String,
    val rawCurlText: String,
)

enum class BuildUrlError { REQUIRED, INVALID_SCHEME }

data class RequestFlowUiState(
    val selectedTab: NewRequestTab = NewRequestTab.Paste,

    // Paste tab
    val pasteText: String = "",
    val pasteError: String? = null,
    val recentChips: List<RecentCurlChip> = emptyList(),

    // Build tab
    val buildMethod: HttpMethod = HttpMethod.GET,
    val buildUrl: String = "",
    val buildHeaders: List<HeaderFieldState> = listOf(HeaderFieldState()),
    val buildBodyMode: BodyMode = BodyMode.JSON,
    val buildBodyJson: String = "",
    val buildError: BuildUrlError? = null,

    // Send flow (Review -> Response)
    val isSending: Boolean = false,
    val sendError: String? = null,
)
