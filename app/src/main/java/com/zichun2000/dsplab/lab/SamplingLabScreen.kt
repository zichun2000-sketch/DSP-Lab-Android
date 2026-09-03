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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import com.zichun2000.dsplab.dsp.sampleSineSignal
import com.zichun2000.dsplab.dsp.satisfiesNyquist
import kotlin.math.PI
import kotlin.math.max

@Composable
fun SamplingLabScreen() {
    var signalFrequency by rememberSaveable { mutableFloatStateOf(3000f) }
    var samplingFrequency by rememberSaveable { mutableFloatStateOf(8000f) }
    var sampleCount by rememberSaveable { mutableIntStateOf(32) }
    val result = sampleSineSignal(frequencyHz = signalFrequency.toDouble(), samplingFrequencyHz = samplingFrequency.toDouble(), sampleCount = sampleCount)
    val nyquistOk = satisfiesNyquist(signalFrequency.toDouble(), samplingFrequency.toDouble())
    val ratio = samplingFrequency / (2f * signalFrequency)
    val marginHz = samplingFrequency / 2f - signalFrequency
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Sampling & Aliasing", style = MaterialTheme.typography.headlineSmall)
        Text("Verify the sampling theorem experimentally by changing only Fs, then compare safe sampling, the Nyquist boundary, and aliasing.", style = MaterialTheme.typography.bodyMedium)
        ElevatedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("1 · Parameters", style = MaterialTheme.typography.titleMedium)
            Text("Signal frequency  ${signalFrequency.toInt()} Hz")
            Slider(signalFrequency, { signalFrequency = it }, valueRange = 500f..5000f)
            Text("Sampling frequency  ${samplingFrequency.toInt()} Hz")
            Slider(samplingFrequency, { samplingFrequency = it }, valueRange = 2000f..16000f)
            Text("Samples  $sampleCount")
            Slider(sampleCount.toFloat(), { sampleCount = it.toInt().coerceIn(16, 64) }, valueRange = 16f..64f, steps = 11)
        }}
        ElevatedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("2 · Visualization", style = MaterialTheme.typography.titleMedium)
            SamplingPlot(result.values, signalFrequency.toDouble(), samplingFrequency.toDouble(), Modifier.fillMaxWidth().height(220.dp))
        }}
        ElevatedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("3 · Experimental diagnosis", style = MaterialTheme.typography.titleMedium)
            Text(if (nyquistOk) "✓ No theoretical aliasing: Fs / (2f) = ${"%.2f".format(ratio)}" else "⚠ Aliasing expected: Fs / (2f) = ${"%.2f".format(ratio)}", style = MaterialTheme.typography.titleSmall)
            Text("Nyquist frequency: ${"%.0f".format(samplingFrequency / 2f)} Hz")
            Text("Distance from signal to Nyquist frequency: ${"%.0f".format(marginHz)} Hz")
            Text("Observed alias frequency: ${"%.0f".format(result.aliasedFrequencyHz)} Hz")
        }}
        ElevatedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("4 · Investigation task", style = MaterialTheme.typography.titleMedium)
            Text("For a 3 kHz signal, compare three cases: Fs = 8 kHz (safe), Fs = 6 kHz (boundary), and Fs = 4 kHz (aliasing).")
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(onClick = { signalFrequency = 3000f; samplingFrequency = 8000f }) { Text("Safe") }
                Button(onClick = { signalFrequency = 3000f; samplingFrequency = 6000f }) { Text("Boundary") }
                Button(onClick = { signalFrequency = 3000f; samplingFrequency = 4000f }) { Text("Aliasing") }
            }
            Text("Question: what changes in the sampled waveform and observed frequency as Fs crosses 2f?", style = MaterialTheme.typography.bodyMedium)
            Button(onClick = { signalFrequency = 3000f; samplingFrequency = 8000f; sampleCount = 32 }) { Text("Reset experiment") }
        }}
        ElevatedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("5 · Reflection", style = MaterialTheme.typography.titleMedium)
            LabResearchPanel("LAB02_SAMPLING", "Use the Safe, Boundary, and Aliasing cases to explain the Nyquist criterion. Why can two different analog frequencies produce the same sampled sequence?", mapOf("signalFrequencyHz" to signalFrequency.toInt().toString(), "samplingFrequencyHz" to samplingFrequency.toInt().toString(), "nyquistRatio" to "%.2f".format(ratio), "aliasFrequencyHz" to "%.0f".format(result.aliasedFrequencyHz)))
        }}
    }
}

@Composable
private fun SamplingPlot(values: List<Double>, signalFrequencyHz: Double, samplingFrequencyHz: Double, modifier: Modifier) {
    Canvas(modifier.padding(vertical = 8.dp)) {
        if (values.isEmpty()) return@Canvas
        val left = 62f
        val right = size.width - 18f
        val top = 24f
        val bottom = size.height - 42f
        val centerY = (top + bottom) / 2f
        val scaleY = (bottom - top) / 2.5f
        val width = right - left

        val textPaint = Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 24f
            isAntiAlias = true
        }
        val smallTextPaint = Paint(textPaint).apply { textSize = 20f }

        drawLine(Color.Black, Offset(left, centerY), Offset(right, centerY), strokeWidth = 2f)
        drawLine(Color.Black, Offset(left, top), Offset(left, bottom), strokeWidth = 2f)

        val xTicks = listOf(0, values.lastIndex / 2, values.lastIndex).distinct()
        xTicks.forEach { index ->
            val x = if (values.size == 1) left else left + width * index / values.lastIndex
            drawLine(Color.Black, Offset(x, centerY - 5f), Offset(x, centerY + 5f), strokeWidth = 1.5f)
            drawContext.canvas.nativeCanvas.drawText(index.toString(), x - 8f, bottom + 24f, smallTextPaint)
        }
        listOf(1f, 0f, -1f).forEach { value ->
            val y = centerY - value * scaleY
            drawLine(Color.Black, Offset(left - 5f, y), Offset(left + 5f, y), strokeWidth = 1.5f)
            drawContext.canvas.nativeCanvas.drawText(if (value == 0f) "0" else "%.1f".format(value), 10f, y + 7f, smallTextPaint)
        }

        drawContext.canvas.nativeCanvas.drawText("Sample n", right - 74f, bottom + 34f, textPaint)
        drawContext.canvas.nativeCanvas.save()
        drawContext.canvas.nativeCanvas.rotate(-90f, 18f, centerY)
        drawContext.canvas.nativeCanvas.drawText("Amplitude", 18f, centerY, textPaint)
        drawContext.canvas.nativeCanvas.restore()

        val path = Path()
        values.forEachIndexed { i, value ->
            val x = if (values.size == 1) left else left + width * i / values.lastIndex
            val y = centerY - value.toFloat() * scaleY
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            drawLine(Color.Black, Offset(x, centerY), Offset(x, y), strokeWidth = 2f)
            drawCircle(Color.Black, 5f, Offset(x, y))
        }
        drawPath(path, Color.Black, style = Stroke(width = 2f))

        val alias = resultAliasFrequency(signalFrequencyHz, samplingFrequencyHz)
        val cycles = max(0.5, alias / samplingFrequencyHz * values.size)
        val reference = Path()
        for (i in 0 until 160) {
            val x = left + width * i / 159f
            val y = centerY - kotlin.math.sin(2.0 * PI * cycles * i / 159.0).toFloat() * scaleY
            if (i == 0) reference.moveTo(x, y) else reference.lineTo(x, y)
        }
        drawPath(reference, Color.Black, style = Stroke(width = 1.5f))
    }
}

private fun resultAliasFrequency(frequencyHz: Double, samplingFrequencyHz: Double): Double {
    val folded = frequencyHz % samplingFrequencyHz
    return minOf(folded, kotlin.math.abs(samplingFrequencyHz - folded))
}
