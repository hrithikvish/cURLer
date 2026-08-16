@file:OptIn(ExperimentalMaterial3Api::class)

package com.hrithikvish.curler.ui.screens.review

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hrithikvish.curler.R
import com.hrithikvish.curler.data.model.HttpMethod
import com.hrithikvish.curler.data.model.HttpRequestModel
import com.hrithikvish.curler.data.query.QueryParamsParser
import com.hrithikvish.curler.ui.components.CurlerFab
import com.hrithikvish.curler.ui.components.EmptyState
import com.hrithikvish.curler.ui.components.FabBottomClearance
import com.hrithikvish.curler.ui.components.JsonLoadingIndicator
import com.hrithikvish.curler.ui.components.JsonText
import com.hrithikvish.curler.ui.components.KeyValueList
import com.hrithikvish.curler.ui.components.isLargeBody
import com.hrithikvish.curler.ui.components.rememberJsonLines
import com.hrithikvish.curler.ui.components.MethodChip
import com.hrithikvish.curler.ui.components.SegmentedControl
import com.hrithikvish.curler.ui.theme.CurlerTheme
import com.hrithikvish.curler.ui.theme.PillShape
import com.hrithikvish.curler.ui.theme.SignalSuccess
import com.hrithikvish.curler.ui.theme.codeMono
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

private enum class ReviewTab { Headers, Body, Query }

@Composable
fun ReviewScreen(
    reviewCapable: ReviewCapable,
    isSending: Boolean,
    onBack: () -> Unit,
    onSendComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val request by reviewCapable.request.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    ReviewContent(
        request = request,
        isSending = isSending,
        onBack = onBack,
        onSend = {
            scope.launch {
                reviewCapable.send()
                onSendComplete()
            }
        },
        modifier = modifier,
    )
}

@Composable
private fun ReviewContent(
    request: HttpRequestModel,
    isSending: Boolean,
    onBack: () -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(pageCount = { ReviewTab.entries.size })
    val scope = rememberCoroutineScope()
    val tabs = listOf(
        stringResource(R.string.review_tab_headers),
        stringResource(R.string.review_tab_body),
        stringResource(R.string.review_tab_query),
    )

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
                title = { Text(stringResource(R.string.review_title), style = MaterialTheme.typography.titleLarge) },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clip(PillShape)
                            .background(SignalSuccess.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.review_valid_badge),
                            style = codeMono.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                            color = SignalSuccess,
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            CurlerFab(onClick = onSend) {
                if (isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.ic_send_plane),
                        contentDescription = stringResource(R.string.review_cd_send),
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            SegmentedControl(
                options = tabs,
                selectedIndex = pagerState.currentPage,
                onSelect = { index -> scope.launch { pagerState.animateScrollToPage(index) } },
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            )
            UrlCard(
                request = request,
                modifier = Modifier.padding(
                    start = 20.dp,
                    end = 20.dp,
                    bottom = 16.dp
                )
            )
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) { page ->
                when (ReviewTab.entries[page]) {
                    ReviewTab.Headers -> HeadersContent(
                        request.headers,
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .padding(start = 20.dp, end = 20.dp, bottom = FabBottomClearance),
                    )
                    ReviewTab.Body -> BodyContent(
                        request.body,
                        modifier = Modifier.fillMaxSize(),
                    )
                    ReviewTab.Query -> QueryContent(
                        request.url,
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .padding(start = 20.dp, end = 20.dp, bottom = FabBottomClearance),
                    )
                }
            }
        }
    }
}

private const val UrlCardAutoCollapseDelayMillis = 1000L
private const val UrlCardCollapsedMaxLines = 2

@Composable
private fun UrlCard(request: HttpRequestModel, modifier: Modifier = Modifier) {
    var isExpanded by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(UrlCardAutoCollapseDelayMillis.milliseconds)
        isExpanded = false
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { isExpanded = !isExpanded }
            .animateContentSize()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        MethodChip(method = request.method)
        Text(
            text = request.url.removePrefix("https://").removePrefix("http://"),
            style = codeMono.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = if (isExpanded) Int.MAX_VALUE else UrlCardCollapsedMaxLines,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun HeadersContent(headers: List<Pair<String, String>>, modifier: Modifier = Modifier) {
    if (headers.isEmpty()) {
        EmptyState(
            title = stringResource(R.string.review_empty_headers_title),
            description = stringResource(R.string.review_empty_headers_desc),
            modifier = modifier,
        )
    } else {
        KeyValueList(items = headers, modifier = modifier)
    }
}

private val BodyContentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = FabBottomClearance)

@Composable
private fun BodyContent(body: String?, modifier: Modifier = Modifier) {
    when {
        body.isNullOrEmpty() -> EmptyState(
            title = stringResource(R.string.review_empty_body_title),
            description = stringResource(R.string.review_empty_body_desc),
            modifier = modifier
                .verticalScroll(rememberScrollState())
                .padding(BodyContentPadding),
        )
        isLargeBody(body) -> Box(
            modifier = modifier
                .padding(BodyContentPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(14.dp)),
        ) {
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
        }
        else -> Box(
            modifier = modifier
                .verticalScroll(rememberScrollState())
                .padding(BodyContentPadding)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(14.dp))
                .padding(14.dp),
        ) {
            JsonText(rawJson = body)
        }
    }
}

@Composable
private fun QueryContent(url: String, modifier: Modifier = Modifier) {
    val params = remember(url) { QueryParamsParser.parse(url) }
    if (params.isEmpty()) {
        EmptyState(
            title = stringResource(R.string.review_empty_query_title),
            description = stringResource(R.string.review_empty_query_desc),
            modifier = modifier,
        )
    } else {
        KeyValueList(items = params, modifier = modifier)
    }
}

@Preview(showBackground = true)
@Composable
private fun ReviewContentPreview() {
    CurlerTheme {
        ReviewContent(
            request = HttpRequestModel(
                method = HttpMethod.POST,
                url = "https://api.example.com/orders",
                headers = listOf("Authorization" to "Bearer xxx", "Content-Type" to "application/json"),
                body = "{\"item\":\"sku_123\",\"qty\":2}",
            ),
            isSending = false,
            onBack = {},
            onSend = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ReviewContentSendingPreview() {
    CurlerTheme {
        ReviewContent(
            request = HttpRequestModel(method = HttpMethod.GET, url = "https://api.example.com/users/42"),
            isSending = true,
            onBack = {},
            onSend = {},
        )
    }
}
