@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package com.hrithikvish.curler.ui.screens.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hrithikvish.curler.R
import com.hrithikvish.curler.data.model.HistoryEntry
import com.hrithikvish.curler.data.model.HttpMethod
import com.hrithikvish.curler.data.model.HttpRequestModel
import com.hrithikvish.curler.data.model.HttpResponseModel
import com.hrithikvish.curler.data.update.UpdateState
import com.hrithikvish.curler.ui.components.CurlerFab
import com.hrithikvish.curler.ui.components.EmptyState
import com.hrithikvish.curler.ui.components.MethodChip
import com.hrithikvish.curler.ui.components.UpdateDisplay
import com.hrithikvish.curler.ui.components.UpdateStateIcon
import com.hrithikvish.curler.ui.components.tapToFocus
import com.hrithikvish.curler.ui.components.updateDisplayFor
import com.hrithikvish.curler.ui.theme.CurlerTheme
import com.hrithikvish.curler.ui.theme.PillShape
import com.hrithikvish.curler.ui.theme.SignalError
import com.hrithikvish.curler.ui.theme.SignalSuccess
import com.hrithikvish.curler.ui.theme.codeMono

@Composable
fun HomeScreen(
    onAddRequest: () -> Unit,
    onHistoryRowClick: (Long) -> Unit,
    onAboutClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeContent(
        uiState = uiState,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onDelete = viewModel::deleteEntry,
        onAddRequest = onAddRequest,
        onHistoryRowClick = onHistoryRowClick,
        onAboutClick = onAboutClick,
        onUpdateAction = viewModel::onUpdateAction,
        modifier = modifier,
    )
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    onSearchQueryChange: (String) -> Unit,
    onDelete: (HistoryEntry) -> Unit,
    onAddRequest: () -> Unit,
    onHistoryRowClick: (Long) -> Unit,
    onAboutClick: () -> Unit,
    onUpdateAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val updateDisplay = updateDisplayFor(uiState.updateState)
    Scaffold(
        modifier = modifier.imePadding(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.ic_terminal),
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.home_title), style = MaterialTheme.typography.titleLarge)
                    }
                },
                actions = {
                    IconButton(onClick = onAboutClick) {
                        BadgedBox(
                            badge = {
                                if (updateDisplay != null) {
                                    Badge(containerColor = MaterialTheme.colorScheme.onSurface)
                                }
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = stringResource(R.string.home_cd_about),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            CurlerFab(onClick = onAddRequest) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.home_cd_new_request),
                    modifier = Modifier.size(28.dp),
                )
            }
        },
        snackbarHost = {
            UpdateBar(
                display = updateDisplay,
                onAction = onUpdateAction,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 20.dp)
                .fillMaxSize(),
        ) {
            SearchField(query = uiState.searchQuery, onQueryChange = onSearchQueryChange)
            if (uiState.filteredEntries.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        title = stringResource(
                            if (uiState.entries.isEmpty()) R.string.home_empty_title_no_requests else R.string.home_empty_title_no_matches,
                        ),
                        description = stringResource(
                            if (uiState.entries.isEmpty()) {
                                R.string.home_empty_description_no_requests
                            } else {
                                R.string.home_empty_description_no_matches
                            },
                        ),
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    itemsIndexed(uiState.filteredEntries, key = { _, entry -> entry.id }) { index, entry ->
                        HistoryRow(
                            entry = entry,
                            onClick = { onHistoryRowClick(entry.id) },
                            onDelete = { onDelete(entry) },
                        )
                        if (index < uiState.filteredEntries.lastIndex) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UpdateBar(display: UpdateDisplay?, onAction: () -> Unit, modifier: Modifier = Modifier) {
    if (display == null) return
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.inverseSurface)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        UpdateStateIcon(
            icon = display.icon,
            tint = MaterialTheme.colorScheme.inverseOnSurface,
            trackTint = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.25f),
            modifier = Modifier.size(18.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = display.title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.inverseOnSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = display.subtitle,
                style = codeMono.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.75f),
            )
        }
        if (display.actionLabel != null) {
            TextButton(onClick = onAction) {
                Text(
                    text = display.actionLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.inverseOnSurface,
                )
            }
        }
    }
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .clip(PillShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(8.dp))
        val focusRequester = remember { FocusRequester() }
        Box(modifier = Modifier.weight(1f).tapToFocus(focusRequester)) {
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                modifier = Modifier.focusRequester(focusRequester),
                decorationBox = { innerTextField ->
                    if (query.isEmpty()) {
                        Text(
                            text = stringResource(R.string.home_search_placeholder),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    innerTextField()
                },
            )
        }
    }
}

@Composable
private fun HistoryRow(
    entry: HistoryEntry,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val isSuccess = entry.response?.let { it.statusCode in 200..299 } == true
    val hasResult = entry.response != null

    Row(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = { menuExpanded = true }
            )
            .padding(vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .background(
                    color = when {
                        !hasResult -> MaterialTheme.colorScheme.outline
                        isSuccess -> SignalSuccess
                        else -> SignalError
                    },
                    shape = CircleShape,
                ),
        )
        MethodChip(method = entry.request.method)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = shortPath(entry.request.url),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface,
            )
            val statusText = entry.response?.statusCode?.toString() ?: stringResource(R.string.home_status_error)
            val relativeTimeText = relativeTime(entry.timestamp)
            Text(
                text = "$relativeTimeText · $statusText",
                style = codeMono.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Box {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = stringResource(R.string.home_cd_more_options),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.home_action_delete)) },
                    onClick = { menuExpanded = false; onDelete() },
                )
            }
        }
    }
}

private fun shortPath(url: String): String {
    val withoutScheme = url.substringAfter("://", url)
    val path = withoutScheme.substringAfter('/', "")
    return if (path.isEmpty()) withoutScheme else "/$path".substringBefore('?')
}

@Composable
private fun relativeTime(timestamp: Long, now: Long = System.currentTimeMillis()): String {
    val minutes = (now - timestamp).coerceAtLeast(0) / 60_000
    val hours = minutes / 60
    val days = hours / 24
    return when {
        minutes < 1 -> stringResource(R.string.home_relative_just_now)
        minutes < 60 -> stringResource(R.string.home_relative_minutes_ago, minutes)
        hours < 24 -> stringResource(R.string.home_relative_hours_ago, hours)
        days == 1L -> stringResource(R.string.home_relative_yesterday)
        else -> stringResource(R.string.home_relative_days_ago, days)
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    CurlerTheme {
        HomeContent(
            uiState = HomeUiState(
                entries = listOf(
                    HistoryEntry(
                        id = 1,
                        timestamp = System.currentTimeMillis() - 120_000,
                        rawCurlText = null,
                        request = HttpRequestModel(HttpMethod.POST, "https://api.example.com/orders"),
                        response = HttpResponseModel(200, "OK", emptyList(), "{}", true, 342, 1200),
                        errorMessage = null,
                    ),
                    HistoryEntry(
                        id = 2,
                        timestamp = System.currentTimeMillis() - 3_600_000,
                        rawCurlText = null,
                        request = HttpRequestModel(HttpMethod.GET, "https://api.example.com/users/42"),
                        response = HttpResponseModel(200, "OK", emptyList(), "{}", true, 120, 400),
                        errorMessage = null,
                    ),
                    HistoryEntry(
                        id = 3,
                        timestamp = System.currentTimeMillis() - 10_800_000,
                        rawCurlText = null,
                        request = HttpRequestModel(HttpMethod.DELETE, "https://api.example.com/cart/9"),
                        response = HttpResponseModel(403, "Forbidden", emptyList(), "{}", true, 88, 60),
                        errorMessage = null,
                    ),
                ),
            ),
            onSearchQueryChange = {},
            onDelete = {},
            onAddRequest = {},
            onHistoryRowClick = {},
            onAboutClick = {},
            onUpdateAction = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenEmptyPreview() {
    CurlerTheme {
        HomeContent(
            uiState = HomeUiState(),
            onSearchQueryChange = {},
            onDelete = {},
            onAddRequest = {},
            onHistoryRowClick = {},
            onAboutClick = {},
            onUpdateAction = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenUpdateAvailablePreview() {
    CurlerTheme {
        HomeContent(
            uiState = HomeUiState(updateState = UpdateState.Available(2)),
            onSearchQueryChange = {},
            onDelete = {},
            onAddRequest = {},
            onHistoryRowClick = {},
            onAboutClick = {},
            onUpdateAction = {},
        )
    }
}

