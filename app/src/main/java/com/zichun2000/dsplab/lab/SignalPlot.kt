package com.zichun2000.dsplab.lab

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import kotlin.math.abs

@Composable
fun SignalPlot(values: List<Double>, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        if (values.isEmpty()) return@Canvas

        val left = 62f
        val right = size.width - 18f
        val top = 24f
        val bottom = size.height - 42f
        val center = (top + bottom) / 2f
        val maxAbs = (values.maxOfOrNull { abs(it) } ?: 1.0).coerceAtLeast(1e-9).toFloat()
        val scale = (bottom - top) * 0.42f / maxAbs
        val width = right - left

        val textPaint = Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 24f
            isAntiAlias = true
        }
        val smallTextPaint = Paint(textPaint).apply { textSize = 20f }

        // Axes.
        drawLine(Color.Black, Offset(left, center), Offset(right, center), strokeWidth = 2f)
        drawLine(Color.Black, Offset(left, top), Offset(left, bottom), strokeWidth = 2f)

        // X-axis ticks: first, middle, last sample index.
        val xTickIndices = listOf(0, (values.lastIndex / 2), values.lastIndex).distinct()
        xTickIndices.forEach { index ->
            val x = if (values.size == 1) left else left + width * index / values.lastIndex
            drawLine(Color.Black, Offset(x, center - 5f), Offset(x, center + 5f), strokeWidth = 1.5f)
            drawContext.canvas.nativeCanvas.drawText(index.toString(), x - 8f, bottom + 24f, smallTextPaint)
        }

        // Y-axis ticks: +max, 0, -max.
        val yTicks = listOf(maxAbs, 0f, -maxAbs)
        yTicks.forEach { value ->
            val y = center - value * scale
            drawLine(Color.Black, Offset(left - 5f, y), Offset(left + 5f, y), strokeWidth = 1.5f)
            val label = if (abs(value) < 1e-6f) "0" else "%.2f".format(value)
            drawContext.canvas.nativeCanvas.drawText(label, 4f, y + 7f, smallTextPaint)
        }

        // Axis labels.
        drawContext.canvas.nativeCanvas.drawText("n", right - 4f, bottom + 34f, textPaint)
        drawContext.canvas.nativeCanvas.save()
        drawContext.canvas.nativeCanvas.rotate(-90f, 18f, center)
        drawContext.canvas.nativeCanvas.drawText("Amplitude", 18f, center, textPaint)
        drawContext.canvas.nativeCanvas.restore()

        // Discrete-time samples.
        values.forEachIndexed { index, value ->
            val x = if (values.size == 1) left else left + width * index / values.lastIndex
            val y = center - value.toFloat() * scale
            drawLine(Color.Black, Offset(x, center), Offset(x, y), strokeWidth = 2f)
            drawCircle(Color.Black, 4f, Offset(x, y))
        }
    }
}
