package com.hrithikvish.curler.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hrithikvish.curler.data.model.HttpMethod
import com.hrithikvish.curler.ui.theme.codeMono

private val ChipShape = RoundedCornerShape(8.dp)

/**
 * Reusable monochrome method-differentiation piece: GET is outline-only,
 * POST is solid filled, PUT/PATCH use a dashed border, DELETE uses the
 * SignalError fill — hue/weight/line-style stand in for color-coding.
 */
@Composable
fun MethodChip(
    method: HttpMethod,
    modifier: Modifier = Modifier,
    label: String = method.name,
) {
    val colors = MaterialTheme.colorScheme
    val contentColor: Color
    var chipModifier = modifier.clip(ChipShape)

    when (method) {
        HttpMethod.POST -> {
            contentColor = colors.onPrimary
            chipModifier = chipModifier.background(colors.primary, ChipShape)
        }
        HttpMethod.DELETE -> {
            contentColor = colors.onError
            chipModifier = chipModifier.background(colors.error, ChipShape)
        }
        HttpMethod.PUT, HttpMethod.PATCH -> {
            contentColor = colors.onSurface
            chipModifier = chipModifier.dashedBorder(colors.onSurface, ChipShape)
        }
        else -> {
            contentColor = colors.onSurface
            chipModifier = chipModifier.border(1.5.dp, colors.outline, ChipShape)
        }
    }

    Box(
        modifier = chipModifier.padding(horizontal = 10.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = codeMono.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
            color = contentColor,
        )
    }
}

private fun Modifier.dashedBorder(color: Color, shape: Shape, strokeWidth: Dp = 1.5.dp): Modifier =
    drawBehind {
        val stroke = Stroke(
            width = strokeWidth.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f),
        )
        val outline = shape.createOutline(size, layoutDirection, this)
        drawOutline(outline, color = color, style = stroke)
    }
