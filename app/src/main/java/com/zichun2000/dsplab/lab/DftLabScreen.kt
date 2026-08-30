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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
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
        Modifier.padding(20.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Lab 04 · DFT / Frequency Spectrum", style = MaterialTheme.typography.titleLarge)
        Text("Transform a sampled sine wave from the time domain into its discrete frequency spectrum.")

        Text("Signal frequency: ${"%.0f".format(frequency)} Hz")
        Slider(frequency, { frequency = it }, valueRange = 250f..3500f)
        Text("Sampling frequency: ${"%.0f".format(sampleRate)} Hz")
        Slider(sampleRate, { sampleRate = it }, valueRange = 4000f..16000f)

        Text("Time-domain samples", style = MaterialTheme.typography.titleMedium)
        SpectrumPlot(samples.map { it }, selected = -1, Modifier.fillMaxWidth().height(180.dp))

        Text("Magnitude spectrum", style = MaterialTheme.typography.titleMedium)
        SpectrumPlot(spectrum.map { it.magnitude }, selected = spectrum.indexOf(peak), Modifier.fillMaxWidth().height(250.dp))

        Text("Frequency resolution: ${"%.1f".format(resolution)} Hz/bin")
        Text("Detected peak: ${"%.1f".format(peak?.frequency ?: 0.0)} Hz")
        Text(
            if (peak != null && kotlin.math.abs(peak.frequency - frequency) <= resolution)
                "✓ Peak is consistent with the input frequency within one DFT bin."
            else "⚠ The peak is shifted because the input frequency does not fall exactly on a DFT bin."
        )

        Text("Reflection: Why does changing N or the sampling rate change frequency resolution?")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { frequency = 1000f; sampleRate = 8000f }) { Text("Reset") }
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
        drawLine(Offset(left, bottom), Offset(right, bottom), strokeWidth = 2f)
        values.forEachIndexed { index, value ->
            val x = left + step * (index + 0.5f)
            val y = bottom - (value.toFloat() / maxValue) * (size.height - 35f)
            drawLine(Offset(x, bottom), Offset(x, y), strokeWidth = if (index == selected) 5f else 2f)
            if (index == selected) drawCircle(Offset(x, y), 7f)
        }
    }
}
