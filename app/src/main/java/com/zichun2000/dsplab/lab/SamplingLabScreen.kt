package com.zichun2000.dsplab.lab

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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

    val result = sampleSineSignal(
        signalFrequencyHz = signalFrequency.toDouble(),
        samplingFrequencyHz = samplingFrequency.toDouble(),
        sampleCount = sampleCount
    )
    val nyquistOk = satisfiesNyquist(signalFrequency.toDouble(), samplingFrequency.toDouble())

    Column(
        Modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Lab 02 · Sampling & Aliasing", style = MaterialTheme.typography.titleLarge)
        Text("Change the sampling frequency and observe when the sampled signal violates the Nyquist criterion.")

        Text("Signal frequency: ${signalFrequency.toInt()} Hz")
        Slider(value = signalFrequency, onValueChange = { signalFrequency = it }, valueRange = 500f..5000f)

        Text("Sampling frequency: ${samplingFrequency.toInt()} Hz")
        Slider(value = samplingFrequency, onValueChange = { samplingFrequency = it }, valueRange = 2000f..16000f)

        Text("Samples: $sampleCount")
        Slider(value = sampleCount.toFloat(), onValueChange = { sampleCount = it.toInt() }, valueRange = 16f..64f, steps = 11)

        Text(
            if (nyquistOk) "✓ Nyquist condition satisfied: Fs ≥ 2f"
            else "⚠ Aliasing occurs: Fs < 2f",
            style = MaterialTheme.typography.titleMedium
        )
        Text("Nyquist frequency: ${"%.0f".format(samplingFrequency / 2f)} Hz")
        Text("Observed alias frequency: ${"%.0f".format(result.aliasedFrequencyHz)} Hz")

        Text("Sampled waveform", style = MaterialTheme.typography.titleMedium)
        SamplingPlot(
            values = result.values,
            signalFrequencyHz = signalFrequency.toDouble(),
            samplingFrequencyHz = samplingFrequency.toDouble(),
            modifier = Modifier.fillMaxWidth().height(280.dp)
        )

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = {
                signalFrequency = 3000f
                samplingFrequency = 8000f
                sampleCount = 32
            }) { Text("Reset") }
            Text("f = ${signalFrequency.toInt()} Hz")
        }

        Text("Reflection: What happens to the observed frequency when the sampling frequency becomes lower than twice the signal frequency?")
    }
}

@Composable
private fun SamplingPlot(
    values: List<Double>,
    signalFrequencyHz: Double,
    samplingFrequencyHz: Double,
    modifier: Modifier
) {
    Canvas(modifier.padding(vertical = 8.dp)) {
        if (values.isEmpty()) return@Canvas
        val left = 42f
        val right = size.width - 16f
        val top = 18f
        val bottom = size.height - 22f
        val centerY = (top + bottom) / 2f
        val scaleY = (bottom - top) / 2.5f
        val width = right - left
        val maxPoints = values.size

        drawLine(Offset(left, centerY), Offset(right, centerY), strokeWidth = 2f)
        val path = androidx.compose.ui.graphics.Path()
        values.forEachIndexed { i, value ->
            val x = if (maxPoints == 1) left else left + width * i / (maxPoints - 1)
            val y = centerY - value.toFloat() * scaleY
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            drawLine(Offset(x, centerY), Offset(x, y), strokeWidth = 2f)
            drawCircle(Offset(x, y), 5f)
        }
        drawPath(path, androidx.compose.ui.graphics.drawscope.Stroke(width = 2f))

        // Reference line: the continuous sine is sampled conceptually at the shown points.
        val reference = androidx.compose.ui.graphics.Path()
        val alias = resultAliasFrequency(signalFrequencyHz, samplingFrequencyHz)
        val cycles = max(0.5, alias / samplingFrequencyHz * values.size)
        for (i in 0 until 160) {
            val x = left + width * i / 159f
            val y = centerY - kotlin.math.sin(2.0 * PI * cycles * i / 159.0).toFloat() * scaleY
            if (i == 0) reference.moveTo(x, y) else reference.lineTo(x, y)
        }
        drawPath(reference, androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f))
    }
}

private fun resultAliasFrequency(frequencyHz: Double, samplingFrequencyHz: Double): Double {
    val folded = frequencyHz % samplingFrequencyHz
    return minOf(folded, kotlin.math.abs(samplingFrequencyHz - folded))
}
