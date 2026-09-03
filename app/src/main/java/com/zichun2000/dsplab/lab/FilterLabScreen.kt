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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun FilterLabScreen() {
    var cutoff by rememberSaveable { mutableFloatStateOf(0.25f) }
    var n by rememberSaveable { mutableIntStateOf(41) }
    val center = n / 2
    val taps = (0 until n).map { i ->
        val k = i - center
        if (k == 0) 2.0 * cutoff else sin(2.0 * PI * cutoff * k) / (PI * k)
    }
    val dcGain = taps.sum()
    val cutoffMagnitude = responseMagnitude(taps, cutoff.toDouble())
    val stopMagnitude = responseMagnitude(taps, minOf(0.45, cutoff.toDouble() + 0.20))
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("FIR Low-Pass Filter", style = MaterialTheme.typography.headlineSmall)
        Text("Design a finite low-pass filter and investigate how cutoff and filter length affect its impulse and frequency responses.", style = MaterialTheme.typography.bodyMedium)
        ElevatedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("1 · Filter parameters", style = MaterialTheme.typography.titleMedium)
            Text("Normalized cutoff  ${"%.2f".format(cutoff)} × Fs"); Slider(cutoff, { cutoff = it }, valueRange = 0.05f..0.45f)
            Text("Tap count  $n"); Slider(value = n.toFloat(), onValueChange = { newValue -> n = listOf(11, 21, 41, 61).minBy { kotlin.math.abs(it - newValue) } }, valueRange = 11f..61f, steps = 2)
            Text("Task: compare a short and a long filter while keeping cutoff fixed.")
        }}
        ElevatedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("2 · Impulse response", style = MaterialTheme.typography.titleMedium)
            SignalPlot(taps, Modifier.fillMaxWidth().height(200.dp))
        }}
        ElevatedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("3 · Frequency response", style = MaterialTheme.typography.titleMedium)
            FrequencyResponsePlot(taps, Modifier.fillMaxWidth().height(210.dp))
            Text("DC gain: ${"%.3f".format(dcGain)}")
            Text("Magnitude near cutoff: ${"%.3f".format(cutoffMagnitude)}")
            Text("Magnitude at cutoff + 0.20Fs: ${"%.3f".format(stopMagnitude)}")
            Text(if (n >= 41) "Longer filter: sharper transition is expected." else "Shorter filter: transition is expected to be wider.")
        }}
        ElevatedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("4 · Investigation task", style = MaterialTheme.typography.titleMedium)
            Text("Keep cutoff = 0.25Fs. Compare N = 11, 41, and 61. Then keep N = 41 and compare cutoff = 0.15Fs, 0.25Fs, and 0.40Fs.")
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(onClick = { n = 11; cutoff = 0.25f }) { Text("N=11") }
                Button(onClick = { n = 61; cutoff = 0.25f }) { Text("N=61") }
                Button(onClick = { n = 41; cutoff = 0.40f }) { Text("fc=0.40") }
            }
            Text("Question: how do filter length and cutoff affect transition width, oscillation, and attenuation?")
            Button(onClick = { cutoff = 0.25f; n = 41 }) { Text("Reset experiment") }
        }}
        ElevatedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("5 · Reflection", style = MaterialTheme.typography.titleMedium)
            LabResearchPanel("LAB05_FIR", "Compare short/long filters and low/high cutoff settings. Use the impulse and frequency responses to explain the trade-off between filter length, transition width, and cutoff frequency.", mapOf("cutoff" to "%.3f".format(cutoff), "tapCount" to n.toString(), "dcGain" to "%.3f".format(dcGain), "cutoffMagnitude" to "%.3f".format(cutoffMagnitude)))
        }}
    }
}

private fun responseMagnitude(taps: List<Double>, normalizedFrequency: Double): Double {
    var real = 0.0
    var imag = 0.0
    taps.forEachIndexed { k, h ->
        val angle = -2.0 * PI * normalizedFrequency * k
        real += h * cos(angle)
        imag += h * sin(angle)
    }
    return sqrt(real * real + imag * imag)
}

@Composable
private fun FrequencyResponsePlot(taps: List<Double>, modifier: Modifier) {
    Canvas(modifier.padding(vertical = 8.dp)) {
        if (taps.isEmpty()) return@Canvas
        val left = 30f; val right = size.width - 15f; val top = 15f; val bottom = size.height - 20f
        val width = right - left
        val samples = 80
        val values = (0 until samples).map { i -> responseMagnitude(taps, 0.5 * i / (samples - 1)) }
        val maxValue = (values.maxOrNull() ?: 1.0).coerceAtLeast(1e-9)
        drawLine(Color.Black, Offset(left, bottom), Offset(right, bottom), strokeWidth = 2f)
        for (i in 0 until samples - 1) {
            val x1 = left + width * i / (samples - 1)
            val x2 = left + width * (i + 1) / (samples - 1)
            val y1 = bottom - (values[i] / maxValue).toFloat() * (bottom - top)
            val y2 = bottom - (values[i + 1] / maxValue).toFloat() * (bottom - top)
            drawLine(Color.Black, Offset(x1, y1), Offset(x2, y2), strokeWidth = 2f)
        }
    }
}
