package com.zichun2000.dsplab.lab

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.zichun2000.dsplab.dsp.dft
import com.zichun2000.dsplab.dsp.sineSamples

@Composable
fun DftLabScreen() {
    var frequency by rememberSaveable { mutableFloatStateOf(1000f) }
    var sampleRate by rememberSaveable { mutableFloatStateOf(8000f) }
    val n = 64
    val samples = sineSamples(frequency.toDouble(), sampleRate.toDouble(), n)
    val spectrum = dft(samples, sampleRate.toDouble())
    val peak = spectrum.maxByOrNull { it.magnitude }
    val resolution = sampleRate / n

    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("DFT & Frequency Spectrum", style = MaterialTheme.typography.headlineSmall)
        Text("Move between time and frequency domains and identify the dominant spectral component.", style = MaterialTheme.typography.bodyMedium)

        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("1 · Parameters", style = MaterialTheme.typography.titleMedium)
                Text("Signal frequency  ${"%.0f".format(frequency)} Hz")
                Slider(frequency, { frequency = it }, valueRange = 250f..3500f)
                Text("Sampling frequency  ${"%.0f".format(sampleRate)} Hz")
                Slider(sampleRate, { sampleRate = it }, valueRange = 4000f..16000f)
                Text("DFT length  N = $n")
            }
        }

        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("2 · Time domain", style = MaterialTheme.typography.titleMedium)
                SpectrumPlot(samples, -1, Modifier.fillMaxWidth().height(150.dp))
            }
        }

        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("3 · Frequency domain", style = MaterialTheme.typography.titleMedium)
                SpectrumPlot(spectrum.map { it.magnitude }, spectrum.indexOf(peak), Modifier.fillMaxWidth().height(210.dp))
                Text("Resolution: ${"%.1f".format(resolution)} Hz/bin")
                Text("Detected peak: ${"%.1f".format(peak?.frequency ?: 0.0)} Hz", style = MaterialTheme.typography.titleSmall)
                Text(if (peak != null && kotlin.math.abs(peak.frequency - frequency) <= resolution) "✓ Peak agrees with the input within one DFT bin." else "⚠ Spectral peak is shifted from the exact input frequency.")
                Button(onClick = { frequency = 1000f; sampleRate = 8000f }) { Text("Reset experiment") }
            }
        }

        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("4 · Reflection", style = MaterialTheme.typography.titleMedium)
                Text("Why do N and the sampling rate determine frequency resolution? What changes when the tone lies between DFT bins?")
            }
        }
    }
}

@Composable
private fun SpectrumPlot(values: List<Double>, selected: Int, modifier: Modifier) {
    Canvas(modifier.padding(vertical = 8.dp)) {
        if (values.isEmpty()) return@Canvas
        val left = 20f
        val right = size.width - 15f
        val bottom = size.height - 15f
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
