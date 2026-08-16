package com.hrithikvish.curler.ui.screens.about

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hrithikvish.curler.data.image.ImageLoader
import com.hrithikvish.curler.data.remoteconfig.AboutConfigRepository
import com.hrithikvish.curler.data.update.UpdateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AboutViewModel @Inject constructor(
    private val aboutConfigRepository: AboutConfigRepository,
    private val imageLoader: ImageLoader,
    private val updateManager: UpdateManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AboutUiState())
    val uiState: StateFlow<AboutUiState> = _uiState.asStateFlow()
    private var lastLoadedImageUrl: String? = null

    init {
        // Paint immediately with whatever's already activated (defaults, or
        // a previous session's fetch) — no loading state needed. Then kick a
        // background refresh; if it lands a newer value, re-apply.
        applyConfig()
        viewModelScope.launch {
            aboutConfigRepository.refresh()
            applyConfig()
        }
        viewModelScope.launch {
            updateManager.updateState.collect { state -> _uiState.update { it.copy(updateState = state) } }
        }
    }

    fun onUpdateAction() = updateManager.performAction()

    private fun applyConfig() {
        val config = aboutConfigRepository.getAboutConfig() ?: return
        _uiState.update {
            it.copy(
                devImageUrl = config.devImageUrl,
                changelogEntries = config.changelogEntries,
            )
        }
        loadDevImage(config.devImageUrl)
    }

    private fun loadDevImage(url: String) {
        if (url.isBlank() || url == lastLoadedImageUrl) return
        lastLoadedImageUrl = url
        viewModelScope.launch {
            val bitmap = imageLoader.load(url)
            _uiState.update { it.copy(devImageBitmap = bitmap) }
        }
    }
}
