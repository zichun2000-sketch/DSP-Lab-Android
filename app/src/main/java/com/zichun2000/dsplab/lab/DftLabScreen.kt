package com.zichun2000.dsplab.lab

import android.graphics.Paint
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
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import com.zichun2000.dsplab.dsp.dft
import com.zichun2000.dsplab.dsp.sineSamples
import kotlin.math.abs
import kotlin.math.max

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
            SpectrumPlot(samples, -1, (n - 1).toDouble(), "Sample n", "Amplitude", Modifier.fillMaxWidth().height(220.dp))
        }}
        ElevatedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("3 · Frequency domain", style = MaterialTheme.typography.titleMedium)
            SpectrumPlot(positive.map { it.magnitude }, positive.indexOf(peak), (sampleRate / 2f).toDouble(), "Frequency (Hz)", "Magnitude", Modifier.fillMaxWidth().height(280.dp))
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
private fun SpectrumPlot(
    values: List<Double>,
    selected: Int,
    xMax: Double,
    xLabel: String,
    yLabel: String,
    modifier: Modifier
) {
    Canvas(modifier.padding(vertical = 8.dp)) {
        if (values.isEmpty()) return@Canvas

        val left = 72f
        val right = size.width - 18f
        val top = 24f
        val bottom = size.height - 52f
        val width = right - left
        val height = bottom - top

        val minRaw = values.minOrNull()?.toFloat() ?: 0f
        val maxRaw = values.maxOrNull()?.toFloat() ?: 1f
        val hasNegative = minRaw < -1e-6f
        val maxAbs = max(abs(minRaw), abs(maxRaw)).coerceAtLeast(1e-6f)
        val yMin = if (hasNegative) -maxAbs else 0f
        val yMax = maxAbs
        val yRange = (yMax - yMin).coerceAtLeast(1e-6f)
        fun yOf(value: Float): Float = bottom - ((value - yMin) / yRange) * height
        val zeroY = yOf(0f)
        val step = if (values.size <= 1) width else width / (values.size - 1)

        val textPaint = Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 24f
            isAntiAlias = true
        }
        val smallTextPaint = Paint(textPaint).apply { textSize = 20f }

        drawLine(Color.Black, Offset(left, zeroY), Offset(right, zeroY), strokeWidth = 2f)
        drawLine(Color.Black, Offset(left, top), Offset(left, bottom), strokeWidth = 2f)

        listOf(0.0, xMax / 2.0, xMax).forEachIndexed { index, value ->
            val x = left + width * index / 2f
            drawLine(Color.Black, Offset(x, zeroY - 5f), Offset(x, zeroY + 5f), strokeWidth = 1.5f)
            val label = if (xMax >= 100.0) "%.0f".format(value) else "%.1f".format(value)
            drawContext.canvas.nativeCanvas.drawText(label, x - 18f, bottom + 25f, smallTextPaint)
        }

        if (hasNegative) {
            listOf(yMax, 0f, yMin).forEach { value ->
                val y = yOf(value)
                drawLine(Color.Black, Offset(left - 5f, y), Offset(left + 5f, y), strokeWidth = 1.5f)
                val label = "%.2f".format(value)
                drawContext.canvas.nativeCanvas.drawText(label, 6f, y + 7f, smallTextPaint)
            }
        } else {
            listOf(yMax, yMax / 2f, 0f).forEach { value ->
                val y = yOf(value)
                drawLine(Color.Black, Offset(left - 5f, y), Offset(left + 5f, y), strokeWidth = 1.5f)
                val label = if (yMax >= 10f) "%.0f".format(value) else "%.2f".format(value)
                drawContext.canvas.nativeCanvas.drawText(label, 6f, y + 7f, smallTextPaint)
            }
        }

        drawContext.canvas.nativeCanvas.drawText(xLabel, right - 125f, size.height - 4f, textPaint)
        drawContext.canvas.nativeCanvas.save()
        drawContext.canvas.nativeCanvas.rotate(-90f, 18f, (top + bottom) / 2f)
        drawContext.canvas.nativeCanvas.drawText(yLabel, 18f, (top + bottom) / 2f, textPaint)
        drawContext.canvas.nativeCanvas.restore()

        values.forEachIndexed { index, value ->
            val x = if (values.size <= 1) left else left + step * index
            val y = yOf(value.toFloat())
            drawLine(Color.Black, Offset(x, zeroY), Offset(x, y), strokeWidth = if (index == selected) 5f else 2f)
            if (index == selected) drawCircle(Color.Black, 7f, Offset(x, y))
        }
    }
}