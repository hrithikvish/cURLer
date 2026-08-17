package com.hrithikvish.curler.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

private val HeaderHorizontalPadding = 12.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBottomSheet(
    title: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    scrollable: Boolean = true,
    maxHeightFraction: Float = 0.8f,
    contentPadding: PaddingValues = PaddingValues(horizontal = HeaderHorizontalPadding),
    content: @Composable ColumnScope.() -> Unit,
) {
    val maxSheetHeight = with(LocalDensity.current) {
        LocalWindowInfo.current.containerSize.height.toDp()
    } * maxHeightFraction

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        dragHandle = null,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = modifier
                .heightIn(max = maxSheetHeight)
                .padding(vertical = 4.dp),
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = HeaderHorizontalPadding)
                    .padding(bottom = 16.dp),
            )
            Column(
                modifier = if (scrollable) {
                    Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .padding(contentPadding)
                } else {
                    Modifier
                        .weight(1f, fill = true)
                        .padding(contentPadding)
                },
                content = content,
            )
        }
    }
}
