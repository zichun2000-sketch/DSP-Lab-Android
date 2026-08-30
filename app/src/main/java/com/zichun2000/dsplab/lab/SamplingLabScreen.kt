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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
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

    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Sampling & Aliasing", style = MaterialTheme.typography.headlineSmall)
        Text("Adjust the signal and sampling rates, then connect the waveform to the Nyquist criterion.", style = MaterialTheme.typography.bodyMedium)

        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("1 · Parameters", style = MaterialTheme.typography.titleMedium)
                Text("Signal frequency  ${signalFrequency.toInt()} Hz")
                Slider(signalFrequency, { signalFrequency = it }, valueRange = 500f..5000f)
                Text("Sampling frequency  ${samplingFrequency.toInt()} Hz")
                Slider(samplingFrequency, { samplingFrequency = it }, valueRange = 2000f..16000f)
                Text("Samples  $sampleCount")
                Slider(sampleCount.toFloat(), { sampleCount = it.toInt().coerceIn(16, 64) }, valueRange = 16f..64f, steps = 11)
            }
        }

        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("2 · Visualization", style = MaterialTheme.typography.titleMedium)
                SamplingPlot(result.values, signalFrequency.toDouble(), samplingFrequency.toDouble(), Modifier.fillMaxWidth().height(220.dp))
            }
        }

        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("3 · Result", style = MaterialTheme.typography.titleMedium)
                Text(if (nyquistOk) "✓ Nyquist condition satisfied: Fs ≥ 2f" else "⚠ Aliasing occurs: Fs < 2f", style = MaterialTheme.typography.titleSmall)
                Text("Nyquist frequency: ${"%.0f".format(samplingFrequency / 2f)} Hz")
                Text("Observed alias frequency: ${"%.0f".format(result.aliasedFrequencyHz)} Hz")
                Button(onClick = { signalFrequency = 3000f; samplingFrequency = 8000f; sampleCount = 32 }) { Text("Reset experiment") }
            }
        }

        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("4 · Reflection", style = MaterialTheme.typography.titleMedium)
                Text("What happens to the observed frequency when Fs becomes lower than twice the signal frequency?")
            }
        }
    }
}

@Composable
private fun SamplingPlot(values: List<Double>, signalFrequencyHz: Double, samplingFrequencyHz: Double, modifier: Modifier) {
    Canvas(modifier.padding(vertical = 8.dp)) {
        if (values.isEmpty()) return@Canvas
        val left = 42f; val right = size.width - 16f; val top = 18f; val bottom = size.height - 22f
        val centerY = (top + bottom) / 2f; val scaleY = (bottom - top) / 2.5f; val width = right - left
        drawLine(Color.Black, Offset(left, centerY), Offset(right, centerY), strokeWidth = 2f)
        val path = Path()
        values.forEachIndexed { i, value ->
            val x = if (values.size == 1) left else left + width * i / (values.size - 1)
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
