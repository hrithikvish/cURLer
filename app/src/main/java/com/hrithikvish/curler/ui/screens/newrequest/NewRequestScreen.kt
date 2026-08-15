@file:OptIn(ExperimentalMaterial3Api::class)

package com.hrithikvish.curler.ui.screens.newrequest

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hrithikvish.curler.R
import com.hrithikvish.curler.ui.components.SegmentedControl

@Composable
fun NewRequestScreen(
    viewModel: RequestFlowViewModel,
    onBack: () -> Unit,
    onNavigateToReview: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val recentChips by viewModel.recentChips.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
                title = { Text(stringResource(R.string.new_request_title), style = MaterialTheme.typography.titleLarge) },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 20.dp)
                .fillMaxSize()
                .imePadding(),
        ) {
            SegmentedControl(
                options = listOf(stringResource(R.string.tab_paste_curl), stringResource(R.string.tab_build_manually)),
                selectedIndex = uiState.selectedTab.ordinal,
                onSelect = { viewModel.selectTab(NewRequestTab.entries[it]) },
                modifier = Modifier.padding(vertical = 16.dp),
            )
            AnimatedContent(
                targetState = uiState.selectedTab,
                modifier = Modifier.weight(1f),
                transitionSpec = {
                    val direction = if (targetState.ordinal > initialState.ordinal) 1 else -1
                    (slideInHorizontally(initialOffsetX = { it * direction }) + fadeIn()) togetherWith
                        (slideOutHorizontally(targetOffsetX = { -it * direction }) + fadeOut())
                },
                label = "newRequestTabContent",
            ) { tab ->
                when (tab) {
                    NewRequestTab.Paste -> PasteTab(
                        text = uiState.pasteText,
                        error = uiState.pasteError,
                        recentChips = recentChips,
                        onTextChange = viewModel::updatePasteText,
                        onParse = { if (viewModel.validateAndBuildRequest()) onNavigateToReview() },
                        onChipClick = viewModel::applyRecentChip,
                        modifier = Modifier.fillMaxSize(),
                    )
                    NewRequestTab.Build -> BuildTab(
                        uiState = uiState,
                        onMethodChange = viewModel::updateBuildMethod,
                        onUrlChange = viewModel::updateBuildUrl,
                        onHeaderKeyChange = viewModel::updateHeaderKey,
                        onHeaderValueChange = viewModel::updateHeaderValue,
                        onAddHeader = viewModel::addHeaderRow,
                        onRemoveHeader = viewModel::removeHeaderRow,
                        onBodyModeChange = viewModel::updateBuildBodyMode,
                        onBodyJsonChange = viewModel::updateBuildBodyJson,
                        onContinue = { if (viewModel.validateAndBuildRequest()) onNavigateToReview() },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}
