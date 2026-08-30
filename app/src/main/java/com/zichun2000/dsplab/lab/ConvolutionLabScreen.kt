package com.zichun2000.dsplab.lab

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import com.zichun2000.dsplab.dsp.convolutionStep
import com.zichun2000.dsplab.dsp.convolve
import kotlin.math.max

@Composable
fun ConvolutionLabScreen() {
    var selectedStep by rememberSaveable { mutableIntStateOf(2) }
    val x = listOf(1.0, 2.0, 1.0)
    val h = listOf(1.0, -1.0, 0.5)
    val y = convolve(x, h)
    val step = selectedStep.coerceIn(0, y.lastIndex)
    val detail = convolutionStep(x, h, step)

    Column(
        Modifier.padding(20.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Lab 03 · Discrete Convolution", style = MaterialTheme.typography.titleLarge)
        Text("Explore y[n] = x[n] * h[n] one output sample at a time.")

        Text("Input x[n] = ${x.joinToString(prefix = "[", postfix = "]")}")
        Text("System h[n] = ${h.joinToString(prefix = "[", postfix = "]")}")

        Text("Output index n = $step")
        Slider(
            value = step.toFloat(),
            onValueChange = { selectedStep = it.toInt().coerceIn(0, y.lastIndex) },
            valueRange = 0f..y.lastIndex.toFloat(),
            steps = max(0, y.size - 2)
        )

        Text("Step calculation", style = MaterialTheme.typography.titleMedium)
        Text("Products: ${detail.products.joinToString { "%.2f".format(it) }}")
        Text("y[$step] = ${"%.2f".format(detail.sum)}")

        Text("Convolution result", style = MaterialTheme.typography.titleMedium)
        StemPlot(y, selected = step, Modifier.fillMaxWidth().height(250.dp))

        Text("y[n] = ${y.joinToString(prefix = "[", postfix = "]") { "%.2f".format(it) }}")
        Text("Reflection: Which samples of x[n] and h[n] contribute to y[$step]? How does the overlap change as n increases?")

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { selectedStep = 0 }) { Text("Start") }
            Button(onClick = { selectedStep = (step + 1).coerceAtMost(y.lastIndex) }) { Text("Next step") }
        }
    }
}

@Composable
private fun StemPlot(values: List<Double>, selected: Int, modifier: Modifier) {
    Canvas(modifier.padding(vertical = 8.dp)) {
        if (values.isEmpty()) return@Canvas
        val left = 35f
        val right = size.width - 15f
        val top = 20f
        val bottom = size.height - 20f
        val center = (top + bottom) / 2f
        val scale = (bottom - top) / 5f
        val width = right - left
        drawLine(Offset(left, center), Offset(right, center), strokeWidth = 2f)
        values.forEachIndexed { i, value ->
            val x = left + width * i / (values.size - 1).coerceAtLeast(1)
            val y = center - value.toFloat() * scale
            drawLine(Offset(x, center), Offset(x, y), strokeWidth = if (i == selected) 5f else 2f)
            drawCircle(Offset(x, y), if (i == selected) 7f else 5f)
        }
    }
}
