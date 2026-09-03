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
import com.zichun2000.dsplab.dsp.dft
import com.zichun2000.dsplab.dsp.sineSamples
import kotlin.math.abs

@Composable
fun DftLabScreen() {
    var frequency by rememberSaveable { mutableFloatStateOf(1000f) }
    var sampleRate by rememberSaveable { mutableFloatStateOf(8000f) }
    var n by rememberSaveable { mutableIntStateOf(64) }
    val samples = sineSamples(frequency.toDouble(), sampleRate.toDouble(), n)
    val spectrum = dft(samples, sampleRate.toDouble())
    val positive = spectrum.take(n / 2 + 1)
    val peak = positive.maxByOrNull { it.magnitude }
    val resolution = sampleRate / n
    val peakBin = if (peak != null) (peak.frequency / resolution).toInt() else 0
    val binError = abs((peak?.frequency ?: 0.0) - frequency)
    val coherent = frequency / resolution - kotlin.math.round(frequency / resolution)
    val coherentTone = abs(coherent) < 1e-5
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("DFT & Frequency Spectrum", style = MaterialTheme.typography.headlineSmall)
        Text("Investigate frequency resolution by changing N, then compare a tone aligned with a DFT bin to one between bins.", style = MaterialTheme.typography.bodyMedium)
        ElevatedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("1 · Parameters", style = MaterialTheme.typography.titleMedium)
            Text("Signal frequency  ${"%.0f".format(frequency)} Hz"); Slider(frequency, { frequency = it }, valueRange = 500f..3500f)
            Text("Sampling frequency  ${"%.0f".format(sampleRate)} Hz"); Slider(sampleRate, { sampleRate = it }, valueRange = 4000f..16000f)
            Text("DFT length  N = $n"); Slider(n.toFloat(), { n = it.toInt().coerceIn(32, 256) }, valueRange = 32f..256f, steps = 6)
        }}
        ElevatedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("2 · Time domain", style = MaterialTheme.typography.titleMedium)
            SpectrumPlot(samples, -1, Modifier.fillMaxWidth().height(150.dp))
        }}
        ElevatedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("3 · Frequency domain", style = MaterialTheme.typography.titleMedium)
            SpectrumPlot(positive.map { it.magnitude }, positive.indexOf(peak), Modifier.fillMaxWidth().height(210.dp))
            Text("Resolution: ${"%.2f".format(resolution)} Hz/bin")
            Text("Detected peak: ${"%.2f".format(peak?.frequency ?: 0.0)} Hz  (bin $peakBin)", style = MaterialTheme.typography.titleSmall)
            Text(if (coherentTone) "✓ Input frequency is aligned with a DFT bin." else "⚠ Input frequency lies between bins; leakage is expected.")
            Text("Peak error: ${"%.2f".format(binError)} Hz")
        }}
        ElevatedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("4 · Investigation task", style = MaterialTheme.typography.titleMedium)
            Text("Compare N = 32, 64, 128, and 256 while keeping Fs fixed. Then compare 1000 Hz with 1100 Hz at N = 64.")
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(onClick = { n = 32; frequency = 1000f; sampleRate = 8000f }) { Text("N=32") }
                Button(onClick = { n = 128; frequency = 1000f; sampleRate = 8000f }) { Text("N=128") }
                Button(onClick = { n = 64; frequency = 1100f; sampleRate = 8000f }) { Text("Between bins") }
            }
            Text("Question: why does increasing N improve frequency resolution, and why does a non-bin-centered tone spread energy across neighboring bins?")
            Button(onClick = { frequency = 1000f; sampleRate = 8000f; n = 64 }) { Text("Reset experiment") }
        }}
        ElevatedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("5 · Reflection", style = MaterialTheme.typography.titleMedium)
            LabResearchPanel("LAB04_DFT", "Compare N = 32 and N = 128, then compare 1000 Hz and 1100 Hz at N = 64. Explain frequency resolution, DFT-bin alignment, and spectral leakage using the observed plots.", mapOf("frequencyHz" to "${"%.0f".format(frequency)}", "sampleRateHz" to "${"%.0f".format(sampleRate)}", "N" to n.toString(), "resolutionHz" to "%.2f".format(resolution), "peakBin" to peakBin.toString()))
        }}
    }
}

@Composable
private fun SpectrumPlot(values: List<Double>, selected: Int, modifier: Modifier) {
    Canvas(modifier.padding(vertical = 8.dp)) {
        if (values.isEmpty()) return@Canvas
        val left = 20f; val right = size.width - 15f; val bottom = size.height - 15f
        val maxValue = (values.maxOrNull() ?: 1.0).coerceAtLeast(1e-9).toFloat()
        val step = (right - left) / values.size
        drawLine(Color.Black, Offset(left, bottom), Offset(right, bottom), strokeWidth = 2f)
        values.forEachIndexed { index, value ->
            val x = left + step * (index + 0.5f)
            val y = bottom - (value.toFloat() / maxValue) * (size.height - 35f)
            drawLine(Color.Black, Offset(x, bottom), Offset(x, y), strokeWidth = if (index == selected) 5f else 2f)
            if (index == selected) drawCircle(Color.Black, 7f, Offset(x, y))
        }
    }
}
