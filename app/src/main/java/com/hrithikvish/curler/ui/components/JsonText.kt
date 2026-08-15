package com.hrithikvish.curler.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import com.hrithikvish.curler.data.json.JsonPrettyPrinter
import com.hrithikvish.curler.ui.theme.codeMono

private val KEY_REGEX = Regex("\"([^\"]+)\"\\s*:")
private val STRING_VALUE_REGEX = Regex(":\\s*\"([^\"]*)\"")
private val NUMBER_REGEX = Regex(":\\s*(-?\\d+(\\.\\d+)?)")

/**
 * Pretty-prints and lightly highlights JSON using weight/tone rather than
 * hue — keys are bold, string/number values are the dimmer on-surface tone.
 */
@Composable
fun JsonText(rawJson: String, modifier: Modifier = Modifier, style: TextStyle = codeMono) {
    val pretty = remember(rawJson) { JsonPrettyPrinter.prettyPrint(rawJson) }
    val keyColor = MaterialTheme.colorScheme.onSurface
    val toneColor = MaterialTheme.colorScheme.onSurfaceVariant
    val annotated = remember(pretty, keyColor, toneColor) { highlightJson(pretty, keyColor, toneColor) }
    Text(text = annotated, style = style, modifier = modifier)
}

private fun highlightJson(pretty: String, keyColor: Color, toneColor: Color): AnnotatedString =
    buildAnnotatedString {
        append(pretty)
        for (match in KEY_REGEX.findAll(pretty)) {
            val range = match.groups[1]!!.range
            addStyle(SpanStyle(fontWeight = FontWeight.Bold, color = keyColor), range.first, range.last + 1)
        }
        for (match in STRING_VALUE_REGEX.findAll(pretty)) {
            val range = match.groups[1]!!.range
            addStyle(SpanStyle(color = toneColor), range.first, range.last + 1)
        }
        for (match in NUMBER_REGEX.findAll(pretty)) {
            val range = match.groups[1]!!.range
            addStyle(SpanStyle(color = toneColor, fontWeight = FontWeight.Medium), range.first, range.last + 1)
        }
    }
