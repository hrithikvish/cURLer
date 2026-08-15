@file:OptIn(ExperimentalMaterial3Api::class)

package com.hrithikvish.curler.ui.screens.response

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import android.content.ClipData
import com.hrithikvish.curler.R
import com.hrithikvish.curler.data.model.HttpMethod
import com.hrithikvish.curler.data.model.HttpRequestModel
import com.hrithikvish.curler.data.model.HttpResponseModel
import com.hrithikvish.curler.ui.components.EmptyState
import com.hrithikvish.curler.ui.components.JsonLoadingIndicator
import com.hrithikvish.curler.ui.components.JsonText
import com.hrithikvish.curler.ui.components.StatusCard
import com.hrithikvish.curler.ui.components.isLargeBody
import com.hrithikvish.curler.ui.components.rememberJsonLines
import com.hrithikvish.curler.ui.theme.CurlerTheme
import com.hrithikvish.curler.ui.theme.SignalError
import com.hrithikvish.curler.ui.theme.codeMono
import kotlinx.coroutines.launch

@Composable
fun ResponseScreen(
    request: HttpRequestModel,
    response: HttpResponseModel?,
    errorMessage: String?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    ResponseContent(
        request = request,
        response = response,
        errorMessage = errorMessage,
        onBack = onBack,
        onCopy = {
            response?.body?.let { body ->
                scope.launch { clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("response", body))) }
            }
        },
        modifier = modifier,
    )
}

@Composable
private fun ResponseContent(
    request: HttpRequestModel,
    response: HttpResponseModel?,
    errorMessage: String?,
    onBack: () -> Unit,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
                title = { Text(stringResource(R.string.response_title), style = MaterialTheme.typography.titleLarge) },
                actions = {
                    if (response != null) {
                        TextButton(onClick = onCopy) {
                            Text(stringResource(R.string.response_cd_copy))
                        }
                    }
                },
            )
        },
    ) { padding ->
        val baseModifier = Modifier
            .padding(padding)
            .padding(horizontal = 20.dp)
            .fillMaxSize()

        if (response != null && isLargeBody(response.body)) {
            Column(modifier = baseModifier) {
                ResponseStatusCard(
                    request = request,
                    response = response,
                    modifier = Modifier.padding(vertical = 16.dp),
                )
                LargeResponseBody(
                    body = response.body,
                    isJson = response.isBodyJson,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                )
            }
        } else {
            Column(modifier = baseModifier.verticalScroll(rememberScrollState())) {
                if (response != null) {
                    ResponseStatusCard(
                        request = request,
                        response = response,
                        modifier = Modifier.padding(vertical = 16.dp),
                    )
                    if (response.body.isNotEmpty()) {
                        SmallResponseBody(body = response.body, isJson = response.isBodyJson)
                    } else {
                        EmptyState(title = stringResource(R.string.response_empty_body), description = "")
                    }
                } else {
                    ErrorState(
                        message = errorMessage ?: stringResource(R.string.response_error_description_fallback),
                        modifier = Modifier.padding(top = 16.dp),
                    )
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun ResponseStatusCard(
    request: HttpRequestModel,
    response: HttpResponseModel,
    modifier: Modifier = Modifier,
) {
    val isSuccess = response.statusCode in 200..299
    StatusCard(
        isSuccess = isSuccess,
        statusLabel = stringResource(
            if (isSuccess) R.string.response_status_success else R.string.response_status_error,
        ),
        statusCode = "${response.statusCode} ${response.statusMessage}",
        metaItems = listOf(
            stringResource(R.string.response_meta_duration, response.durationMs),
            formatSize(response.sizeBytes),
            "${request.method.name} ${pathOf(request.url)}",
        ),
        modifier = modifier,
    )
}

/** Compact, content-sized body card — only for bodies under [isLargeBody]'s threshold. */
@Composable
private fun SmallResponseBody(body: String, isJson: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(18.dp))
            .padding(14.dp),
    ) {
        if (isJson) {
            JsonText(rawJson = body)
        } else {
            Text(text = body, style = codeMono, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

/**
 * Line-virtualized body card for large responses — a single `Text` node over
 * tens of KB is what was freezing the screen on open, so this renders one
 * `LazyColumn` item per line instead and requires a bounded-height [modifier]
 * (e.g. `Modifier.weight(1f)` in an enclosing `Column`).
 */
@Composable
private fun LargeResponseBody(body: String, isJson: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(18.dp)),
    ) {
        if (isJson) {
            val lines = rememberJsonLines(body)
            if (lines == null) {
                JsonLoadingIndicator(modifier = Modifier.fillMaxSize())
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                    items(lines.size) { index ->
                        Text(
                            text = lines[index],
                            style = codeMono,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        } else {
            val lines = remember(body) { body.lines() }
            LazyColumn(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                items(lines.size) { index ->
                    Text(
                        text = lines[index],
                        style = codeMono,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorState(message: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SignalError.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
            .padding(20.dp),
    ) {
        Text(
            text = stringResource(R.string.response_error_title),
            style = MaterialTheme.typography.titleMedium,
            color = SignalError,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun formatSize(bytes: Long): String = if (bytes < 1024) {
    stringResource(R.string.response_meta_size_bytes, bytes)
} else {
    stringResource(R.string.response_meta_size_kb, bytes / 1024f)
}

private fun pathOf(url: String): String {
    val withoutScheme = url.substringAfter("://", url)
    val path = withoutScheme.substringAfter('/', "")
    return if (path.isEmpty()) "/" else "/$path".substringBefore('?')
}

@Preview(showBackground = true)
@Composable
private fun ResponseContentSuccessPreview() {
    CurlerTheme {
        ResponseContent(
            request = HttpRequestModel(HttpMethod.POST, "https://api.example.com/orders"),
            response = HttpResponseModel(
                statusCode = 200,
                statusMessage = "OK",
                headers = emptyList(),
                body = "{\"id\":\"ord_9F2c\",\"status\":\"confirmed\",\"total\":48.0}",
                isBodyJson = true,
                durationMs = 342,
                sizeBytes = 1200,
            ),
            errorMessage = null,
            onBack = {},
            onCopy = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ResponseContentErrorPreview() {
    CurlerTheme {
        ResponseContent(
            request = HttpRequestModel(HttpMethod.GET, "https://api.example.com/users/42"),
            response = null,
            errorMessage = "Unable to resolve host \"api.example.com\"",
            onBack = {},
            onCopy = {},
        )
    }
}
