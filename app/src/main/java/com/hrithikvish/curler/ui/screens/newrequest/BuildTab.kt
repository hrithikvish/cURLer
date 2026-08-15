package com.hrithikvish.curler.ui.screens.newrequest

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hrithikvish.curler.R
import com.hrithikvish.curler.data.model.HttpMethod
import com.hrithikvish.curler.ui.components.HeaderEditRow
import com.hrithikvish.curler.ui.components.MethodChip
import com.hrithikvish.curler.ui.components.SegmentedControl
import com.hrithikvish.curler.ui.components.tapToFocus
import com.hrithikvish.curler.ui.theme.CurlerTheme
import com.hrithikvish.curler.ui.theme.PillShape
import com.hrithikvish.curler.ui.theme.codeMono

private val BUILD_TAB_METHODS = listOf(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE)
private val SelectorChipShape = RoundedCornerShape(8.dp)

private fun buildErrorMessageRes(error: BuildUrlError): Int = when (error) {
    BuildUrlError.REQUIRED -> R.string.error_url_required
    BuildUrlError.INVALID_SCHEME -> R.string.error_url_scheme
}

@Composable
fun BuildTab(
    uiState: RequestFlowUiState,
    onMethodChange: (HttpMethod) -> Unit,
    onUrlChange: (String) -> Unit,
    onHeaderKeyChange: (Int, String) -> Unit,
    onHeaderValueChange: (Int, String) -> Unit,
    onAddHeader: () -> Unit,
    onRemoveHeader: (Int) -> Unit,
    onBodyModeChange: (BodyMode) -> Unit,
    onBodyJsonChange: (String) -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp),
    ) {
        item {
            MethodSelectorRow(
                options = BUILD_TAB_METHODS,
                selected = uiState.buildMethod,
                onSelect = onMethodChange,
                modifier = Modifier.padding(bottom = 18.dp),
            )

            FieldLabel(stringResource(R.string.field_label_url))
            var urlFieldValue by remember {
                mutableStateOf(TextFieldValue(
                    text = uiState.buildUrl,
                    selection = TextRange(uiState.buildUrl.length)
                ))
            }
            UrlField(
                value = urlFieldValue,
                onValueChange = { new ->
                    urlFieldValue = new
                    if (new.text != uiState.buildUrl) onUrlChange(new.text)
                },
            )
            UrlQuickInsertRow(
                onInsert = { snippet ->
                    val current = urlFieldValue
                    val newText = current.text.replaceRange(current.selection.min, current.selection.max, snippet)
                    val newCursor = current.selection.min + snippet.length
                    urlFieldValue = TextFieldValue(text = newText, selection = TextRange(newCursor))
                    onUrlChange(newText)
                },
                modifier = Modifier.padding(bottom = 12.dp),
            )
            if (uiState.buildError != null) {
                Text(
                    text = stringResource(buildErrorMessageRes(uiState.buildError)),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
            }

            FieldLabel(stringResource(R.string.field_label_headers))
        }

        itemsIndexed(
            items = uiState.buildHeaders,
            key = { _, header -> header.id }
        ) { index, header ->
            HeaderEditRow(
                keyText = header.key,
                valueText = header.value,
                onKeyChange = { onHeaderKeyChange(index, it) },
                onValueChange = { onHeaderValueChange(index, it) },
                onDelete = { onRemoveHeader(index) },
                modifier = Modifier
                    .animateItem()
                    .padding(bottom = 8.dp),
            )
        }

        item {
            Row(
                modifier = Modifier
                    .clickable(onClick = onAddHeader)
                    .padding(vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.action_add_header),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            AnimatedVisibility(
                visible = uiState.buildMethod != HttpMethod.GET,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Column {
                    Spacer(Modifier.height(14.dp))
                    FieldLabel(stringResource(R.string.field_label_body))
                    SegmentedControl(
                        options = listOf(
                            stringResource(R.string.body_mode_json),
                            stringResource(R.string.body_mode_form),
                            stringResource(R.string.body_mode_none),
                        ),
                        selectedIndex = uiState.buildBodyMode.ordinal,
                        onSelect = { onBodyModeChange(BodyMode.entries[it]) },
                        modifier = Modifier.padding(bottom = 12.dp),
                    )
                    when (uiState.buildBodyMode) {
                        BodyMode.JSON -> {
                            val focusRequester = remember { FocusRequester() }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 100.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .padding(13.dp)
                                    .tapToFocus(focusRequester),
                            ) {
                                BasicTextField(
                                    value = uiState.buildBodyJson,
                                    onValueChange = onBodyJsonChange,
                                    textStyle = codeMono.copy(color = MaterialTheme.colorScheme.onSurface),
                                    cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                                    decorationBox = { innerTextField ->
                                        if (uiState.buildBodyJson.isEmpty()) {
                                            Text(
                                                text = stringResource(R.string.json_body_placeholder),
                                                style = codeMono,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                        innerTextField()
                                    },
                                )
                            }
                        }
                        BodyMode.FORM -> {
                            Text(
                                text = stringResource(R.string.body_form_stub),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 12.dp),
                            )
                        }
                        BodyMode.NONE -> Unit
                    }
                }
            }

            Spacer(Modifier.height(18.dp))
            Button(
                onClick = onContinue,
                shape = PillShape,
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(id = R.string.action_continue_review),
                    style = MaterialTheme.typography.labelLarge
                )
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun MethodSelectorRow(
    options: List<HttpMethod>,
    selected: HttpMethod,
    onSelect: (HttpMethod) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        options.forEach { method ->
            SelectableMethodChip(
                method = method,
                isSelected = method == selected,
                onClick = { onSelect(method) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/**
 * Selection state is the dominant signal here (full [MethodChip] treatment only when
 * selected, a plain faint outline otherwise), and taps trigger a springy Material
 * Expressive-style scale bounce (press shrinks, release/selection overshoots back).
 */
@Composable
private fun SelectableMethodChip(
    method: HttpMethod,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(SelectorChipShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        if (isSelected) {
            MethodChip(
                method = method,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(SelectorChipShape)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = SelectorChipShape
                    )
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = method.name,
                    style = codeMono.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                )
            }
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        style = codeMono.copy(fontSize = 10.5.sp, fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 7.dp),
    )
}

@Composable
private fun UrlField(value: TextFieldValue, onValueChange: (TextFieldValue) -> Unit) {
    val focusRequester = remember { FocusRequester() }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(14.dp))
            .padding(horizontal = 13.dp, vertical = 12.dp)
            .tapToFocus(focusRequester),
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = codeMono.copy(fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurface),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
            decorationBox = { innerTextField ->
                if (value.text.isEmpty()) {
                    Text(
                        text = stringResource(R.string.url_placeholder),
                        style = codeMono.copy(fontSize = 11.5.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                innerTextField()
            },
        )
    }
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun UrlQuickInsertRow(
    onInsert: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val snippets = stringArrayResource(R.array.url_quick_inserts)
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        snippets.forEach { snippet ->
            UrlQuickInsertChip(text = snippet, onClick = { onInsert(snippet) })
        }
    }
}

@Composable
private fun UrlQuickInsertChip(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(SelectorChipShape)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = SelectorChipShape,
            )
            .clickable(onClickLabel = stringResource(R.string.cd_url_quick_insert, text), onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = codeMono.copy(fontSize = 11.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(showBackground = true, heightDp = 700)
@Composable
private fun BuildTabPreview() {
    CurlerTheme {
        BuildTab(
            uiState = RequestFlowUiState(
                buildMethod = HttpMethod.POST,
                buildUrl = "https://api.example.com/orders",
                buildHeaders = listOf(
                    HeaderFieldState("Authorization", "Bearer xxx"),
                    HeaderFieldState("Content-Type", "application/json"),
                    HeaderFieldState(),
                ),
                buildBodyJson = "{\n  \"item\": \"sku_123\",\n  \"qty\": 2\n}",
            ),
            onMethodChange = {},
            onUrlChange = {},
            onHeaderKeyChange = { _, _ -> },
            onHeaderValueChange = { _, _ -> },
            onAddHeader = {},
            onRemoveHeader = {},
            onBodyModeChange = {},
            onBodyJsonChange = {},
            onContinue = {},
        )
    }
}

@Preview(showBackground = true, heightDp = 700)
@Composable
private fun BuildTabGetPreview() {
    CurlerTheme {
        BuildTab(
            uiState = RequestFlowUiState(
                buildMethod = HttpMethod.GET,
                buildUrl = "https://api.example.com/orders",
                buildHeaders = listOf(
                    HeaderFieldState("Authorization", "Bearer xxx"),
                    HeaderFieldState(),
                ),
            ),
            onMethodChange = {},
            onUrlChange = {},
            onHeaderKeyChange = { _, _ -> },
            onHeaderValueChange = { _, _ -> },
            onAddHeader = {},
            onRemoveHeader = {},
            onBodyModeChange = {},
            onBodyJsonChange = {},
            onContinue = {},
        )
    }
}
