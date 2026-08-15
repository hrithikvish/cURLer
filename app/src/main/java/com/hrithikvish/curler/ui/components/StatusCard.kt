package com.hrithikvish.curler.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hrithikvish.curler.ui.theme.PillShape
import com.hrithikvish.curler.ui.theme.SignalError
import com.hrithikvish.curler.ui.theme.White
import com.hrithikvish.curler.ui.theme.codeMono

@Composable
fun StatusCard(
    isSuccess: Boolean,
    statusLabel: String,
    statusCode: String,
    metaItems: List<String>,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(colors.inverseSurface)
            .padding(20.dp),
    ) {
        Row {
            Box(
                modifier = Modifier
                    .clip(PillShape)
                    .background(if (isSuccess) colors.inverseOnSurface else SignalError)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text(
                    text = statusLabel,
                    style = codeMono.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                    color = if (isSuccess) colors.inverseSurface else White,
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = statusCode,
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 34.sp, fontWeight = FontWeight.Bold),
            color = colors.inverseOnSurface,
        )
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            metaItems.forEach { item ->
                Text(
                    text = item,
                    style = codeMono.copy(fontSize = 11.sp),
                    color = colors.inverseOnSurface.copy(alpha = 0.7f),
                )
            }
        }
    }
}
