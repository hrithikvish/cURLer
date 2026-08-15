package com.hrithikvish.curler.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hrithikvish.curler.data.json.JsonPrettyPrinter
import com.hrithikvish.curler.ui.theme.codeMono
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val KEY_REGEX = Regex("\"([^\"]+)\"\\s*:")
private val STRING_VALUE_REGEX = Regex(":\\s*\"([^\"]*)\"")
private val NUMBER_REGEX = Regex(":\\s*(-?\\d+(\\.\\d+)?)")

/**
 * Above this raw length, callers should switch from a single-node [JsonText]
 * to a line-by-line [rememberJsonLines] fed into a `LazyColumn`. Laying out
 * tens of KB of text as one Compose `Text` node is what actually freezes the
 * frame — pretty-printing itself is cheap enough to background either way,
 * but Compose still has to measure a giant single node synchronously, and
 * only virtualizing avoids that. Below this size a single node is fine and
 * cheaper than LazyColumn's item overhead.
 */
private const val LARGE_BODY_THRESHOLD = 4_000

fun isLargeBody(raw: String): Boolean = raw.length >= LARGE_BODY_THRESHOLD

/** Compact, single-node rendering — only for bodies under [LARGE_BODY_THRESHOLD]. */
@Composable
fun JsonText(
    rawJson: String,
    modifier: Modifier = Modifier,
    style: TextStyle = codeMono,
) {
    val keyColor = MaterialTheme.colorScheme.onSurface
    val toneColor = MaterialTheme.colorScheme.onSurfaceVariant
    val annotated = remember(rawJson, keyColor, toneColor) {
        highlightJson(JsonPrettyPrinter.prettyPrint(rawJson), keyColor, toneColor)
    }
    Text(text = annotated, style = style, modifier = modifier)
}

/**
 * Pretty-printed + highlighted lines for a large JSON body, computed off the
 * main thread. Null while formatting is in progress — feed the result into a
 * `LazyColumn`'s `items(...)` so only the lines actually on screen ever get
 * measured/laid out.
 */
@Composable
fun rememberJsonLines(rawJson: String): List<AnnotatedString>? {
    val keyColor = MaterialTheme.colorScheme.onSurface
    val toneColor = MaterialTheme.colorScheme.onSurfaceVariant
    val lines by produceState<List<AnnotatedString>?>(
        initialValue = null,
        rawJson,
        keyColor,
        toneColor,
    ) {
        value = withContext(Dispatchers.Default) {
            JsonPrettyPrinter.prettyPrint(rawJson)
                .lineSequence()
                .map { line -> highlightJson(line, keyColor, toneColor) }
                .toList()
        }
    }
    return lines
}

/** Centered spinner sized to fill whatever bounded modifier the caller gives it. */
@Composable
fun JsonLoadingIndicator(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            modifier = Modifier.size(28.dp),
            strokeWidth = 2.dp,
        )
    }
}

private fun highlightJson(
    text: String,
    keyColor: Color,
    toneColor: Color,
): AnnotatedString =
    buildAnnotatedString {
        append(text)
        for (match in KEY_REGEX.findAll(text)) {
            val range = match.groups[1]!!.range
            addStyle(SpanStyle(fontWeight = FontWeight.Bold, color = keyColor), range.first, range.last + 1)
        }
        for (match in STRING_VALUE_REGEX.findAll(text)) {
            val range = match.groups[1]!!.range
            addStyle(SpanStyle(color = toneColor), range.first, range.last + 1)
        }
        for (match in NUMBER_REGEX.findAll(text)) {
            val range = match.groups[1]!!.range
            addStyle(SpanStyle(color = toneColor, fontWeight = FontWeight.Medium), range.first, range.last + 1)
        }
    }
