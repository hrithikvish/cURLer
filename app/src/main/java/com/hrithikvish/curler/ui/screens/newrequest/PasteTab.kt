package com.hrithikvish.curler.ui.screens.newrequest

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hrithikvish.curler.R
import com.hrithikvish.curler.data.model.HttpMethod
import com.hrithikvish.curler.ui.components.tapToFocus
import com.hrithikvish.curler.ui.theme.CurlerTheme
import com.hrithikvish.curler.ui.theme.PillShape
import com.hrithikvish.curler.ui.theme.codeMono
import kotlinx.coroutines.launch

@Composable
fun PasteTab(
    text: String,
    error: String?,
    recentChips: List<RecentCurlChip>,
    onTextChange: (String) -> Unit,
    onParse: () -> Unit,
    onChipClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val clipboard = LocalClipboard.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(20.dp))
                .padding(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(R.string.paste_hint_label),
                    style = codeMono.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                IconButton(
                    onClick = {
                        scope.launch {
                            val pasted = clipboard.getClipEntry()
                                ?.clipData
                                ?.takeIf { it.itemCount > 0 }
                                ?.getItemAt(0)
                                ?.coerceToText(context)
                                ?.toString()
                            if (!pasted.isNullOrEmpty()) onTextChange(pasted)
                        }
                    },
                    modifier = Modifier.size(28.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_paste),
                        contentDescription = stringResource(R.string.cd_paste_from_clipboard),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(15.dp),
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            PasteTextField(
                text = text,
                onTextChange = onTextChange,
            )
        }
        if (error != null) {
            Spacer(Modifier.height(8.dp))
            Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onParse,
            shape = PillShape,
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
            Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.action_parse_validate), style = MaterialTheme.typography.labelLarge)
        }
        if (recentChips.isNotEmpty()) {
            Spacer(Modifier.height(14.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(recentChips) { chip ->
                    RecentChipView(chip = chip, onClick = { onChipClick(chip.rawCurlText) })
                }
            }
        }
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun PasteTextField(text: String, onTextChange: (String) -> Unit) {
    val focusRequester = remember { FocusRequester() }
    Box(modifier = Modifier.fillMaxSize().tapToFocus(focusRequester)) {
        BasicTextField(
            value = text,
            onValueChange = onTextChange,
            textStyle = codeMono.copy(color = MaterialTheme.colorScheme.onSurface),
            modifier = Modifier.fillMaxSize().focusRequester(focusRequester),
            decorationBox = { innerTextField ->
                if (text.isEmpty()) {
                    Text(
                        text = stringResource(R.string.paste_placeholder),
                        style = codeMono,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                innerTextField()
            },
        )
    }
}

@Composable
private fun RecentChipView(chip: RecentCurlChip, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(PillShape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, PillShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = chip.method.name,
            style = codeMono.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = chip.label,
            style = codeMono.copy(fontSize = 11.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(showBackground = true, heightDp = 500)
@Composable
private fun PasteTabPreview() {
    CurlerTheme {
        PasteTab(
            text = "curl -X POST https://api.example.com/orders \\\n  -H \"Authorization: Bearer xxx\" \\\n  -d '{\"item\":\"sku_123\",\"qty\":2}'",
            error = null,
            recentChips = listOf(
                RecentCurlChip(HttpMethod.GET, "/users", "curl https://api.example.com/users"),
                RecentCurlChip(HttpMethod.POST, "/login", "curl -X POST https://api.example.com/login"),
                RecentCurlChip(HttpMethod.DELETE, "/cart/9", "curl -X DELETE https://api.example.com/cart/9"),
            ),
            onTextChange = {},
            onParse = {},
            onChipClick = {},
        )
    }
}

@Preview(showBackground = true, heightDp = 500)
@Composable
private fun PasteTabErrorPreview() {
    CurlerTheme {
        PasteTab(
            text = "curl api.example.com/orders",
            error = "URL should start with http:// or https:// — got \"api.example.com/orders\"",
            recentChips = emptyList(),
            onTextChange = {},
            onParse = {},
            onChipClick = {},
        )
    }
}
