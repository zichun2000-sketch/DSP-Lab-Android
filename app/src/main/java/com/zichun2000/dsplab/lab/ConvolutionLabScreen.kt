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
import androidx.compose.material3.ElevatedCard
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.zichun2000.dsplab.dsp.convolutionStep
import com.zichun2000.dsplab.dsp.convolve
import kotlin.math.max

@Composable
fun ConvolutionLabScreen() {
    var selectedStep by rememberSaveable { mutableIntStateOf(2) }
    var mode by rememberSaveable { mutableIntStateOf(0) }
    val x = if (mode == 0) listOf(1.0, 2.0, 1.0) else listOf(1.0, 2.0, 1.0, 0.0)
    val h = if (mode == 0) listOf(1.0, -1.0, 0.5) else listOf(0.25, 0.5, 0.25)
    val y = convolve(x, h)
    val step = selectedStep.coerceIn(0, y.lastIndex)
    val detail = convolutionStep(x, h, step)
    val manual = detail.products.sum()
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Discrete Convolution", style = MaterialTheme.typography.headlineSmall)
        Text("Compute linear convolution, inspect the overlap at each output index, and verify one output sample by hand.", style = MaterialTheme.typography.bodyMedium)
        ElevatedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("1 · Experiment mode", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { mode = 0; selectedStep = 2 }) { Text("Basic") }
                Button(onClick = { mode = 1; selectedStep = 2 }) { Text("Smoothing") }
            }
            Text(if (mode == 0) "x[n] = [1, 2, 1], h[n] = [1, −1, 0.5]" else "x[n] = [1, 2, 1, 0], h[n] = [0.25, 0.5, 0.25]")
            Text("Task: predict one y[n] before moving the step slider, then compare your calculation with the program result.")
        }}
        ElevatedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("2 · Convolution step", style = MaterialTheme.typography.titleMedium)
            Text("Output index n = $step")
            Slider(value = step.toFloat(), onValueChange = { selectedStep = it.toInt().coerceIn(0, y.lastIndex) }, valueRange = 0f..y.lastIndex.toFloat(), steps = max(0, y.size - 2))
            Text("Products: ${detail.products.joinToString { "%.2f".format(it) }}")
            Text("Sum of products = ${"%.2f".format(manual)}")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { Button(onClick = { selectedStep = 0 }) { Text("Start") }; Button(onClick = { selectedStep = (step + 1).coerceAtMost(y.lastIndex) }) { Text("Next") } }
        }}
        ElevatedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("3 · Output", style = MaterialTheme.typography.titleMedium)
            StemPlot(y, selected = step, Modifier.fillMaxWidth().height(220.dp))
            Text("y[n] = ${y.joinToString(prefix = "[", postfix = "]") { "%.2f".format(it) }}")
        }}
        ElevatedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("4 · System interpretation", style = MaterialTheme.typography.titleMedium)
            Text(if (mode == 1) "The smoothing kernel averages neighboring samples, so rapid changes in x[n] are reduced." else "The output is formed by weighted overlap between x[n] and a reversed-and-shifted h[n].")
            Text("At n = $step, manual sum = ${"%.2f".format(manual)} and computed y[$step] = ${"%.2f".format(y[step])}.")
        }}
        ElevatedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("5 · Reflection", style = MaterialTheme.typography.titleMedium)
            LabResearchPanel("LAB03_CONVOLUTION", "Choose Basic and Smoothing. Verify one output sample by hand. Which input samples overlap at the selected n, and why does the smoothing kernel reduce rapid variation?", mapOf("mode" to if (mode == 0) "basic" else "smoothing", "step" to step.toString(), "manualSum" to "%.2f".format(manual)))
        }}
    }
}

@Composable
private fun StemPlot(values: List<Double>, selected: Int, modifier: Modifier) {
    Canvas(modifier.padding(vertical = 8.dp)) {
        if (values.isEmpty()) return@Canvas
        val left = 35f; val right = size.width - 15f; val top = 20f; val bottom = size.height - 20f
        val center = (top + bottom) / 2f; val scale = (bottom - top) / 5f; val width = right - left
        drawLine(Color.Black, Offset(left, center), Offset(right, center), strokeWidth = 2f)
        values.forEachIndexed { i, value ->
            val x = left + width * i / (values.size - 1).coerceAtLeast(1)
            val yPoint = center - value.toFloat() * scale
            drawLine(Color.Black, Offset(x, center), Offset(x, yPoint), strokeWidth = if (i == selected) 5f else 2f)
            drawCircle(Color.Black, if (i == selected) 7f else 5f, Offset(x, yPoint))
        }
    }
}
