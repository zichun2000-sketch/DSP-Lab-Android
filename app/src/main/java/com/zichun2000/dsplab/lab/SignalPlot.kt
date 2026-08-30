package com.zichun2000.dsplab.lab

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

@Composable
fun SignalPlot(values: List<Double>, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        if (values.isEmpty()) return@Canvas
        val left = 24f
        val right = size.width - 16f
        val top = 18f
        val bottom = size.height - 18f
        val center = (top + bottom) / 2f
        val maxAbs = (values.maxOfOrNull { kotlin.math.abs(it) } ?: 1.0).coerceAtLeast(1e-9).toFloat()
        val scale = (bottom - top) * 0.42f / maxAbs
        val width = right - left
        drawLine(Color.Black, Offset(left, center), Offset(right, center), strokeWidth = 2f)
        values.forEachIndexed { index, value ->
            val x = if (values.size == 1) left else left + width * index / (values.size - 1)
            val y = center - value.toFloat() * scale
            drawLine(Color.Black, Offset(x, center), Offset(x, y), strokeWidth = 2f)
            drawCircle(Color.Black, 4f, Offset(x, y))
        }
    }
}
