package com.zichun2000.dsplab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.zichun2000.dsplab.dsp.SignalParameters
import com.zichun2000.dsplab.dsp.SignalType
import com.zichun2000.dsplab.dsp.generateDiscreteSignal
import kotlin.math.PI
import kotlin.math.cos

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(Modifier.fillMaxSize()) { SignalLabScreen() }
            }
        }
    }
}

@Composable
private fun SignalLabScreen() {
    var typeIndex by rememberSaveable { mutableIntStateOf(0) }
    var amplitude by rememberSaveable { mutableFloatStateOf(1f) }
    var frequency by rememberSaveable { mutableFloatStateOf(2f) }
    var phaseDegrees by rememberSaveable { mutableFloatStateOf(0f) }
    var decay by rememberSaveable { mutableFloatStateOf(0.06f) }
    var sampleCount by rememberSaveable { mutableIntStateOf(32) }
    val type = SignalType.entries[typeIndex]
    val phase = phaseDegrees * PI.toFloat() / 180f
    val values = generateDiscreteSignal(
        type,
        sampleCount,
        SignalParameters(amplitude.toDouble(), frequency.toDouble(), phase.toDouble(), decay.toDouble())
    )

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("DSP Lab", style = MaterialTheme.typography.headlineMedium)
        Text("Lab 01 · Discrete-Time Signals", style = MaterialTheme.typography.titleLarge)
        Text("Explore how amplitude, frequency, phase, and sampling density affect a discrete-time signal.")

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            SignalType.entries.forEachIndexed { index, signalType ->
                FilterChip(
                    selected = index == typeIndex,
                    onClick = { typeIndex = index },
                    label = { Text(signalType.displayName) }
                )
            }
        }

        ParameterSlider("Amplitude", amplitude, 0.1f..2f, "%.2f") { amplitude = it }
        if (type == SignalType.SINE) {
            ParameterSlider("Frequency (cycles/window)", frequency, 0.25f..12f, "%.2f") { frequency = it }
            ParameterSlider("Phase", phaseDegrees, 0f..360f, "%.0f°") { phaseDegrees = it }
        }
        if (type == SignalType.EXPONENTIAL) {
            ParameterSlider("Decay", decay, 0.01f..0.20f, "%.2f") { decay = it }
        }
        Text("Samples: $sampleCount")
        Slider(
            value = sampleCount.toFloat(),
            onValueChange = { sampleCount = it.toInt().coerceIn(16, 64) },
            valueRange = 16f..64f,
            steps = 11
        )

        Text("Time-domain representation", style = MaterialTheme.typography.titleMedium)
        SignalPlot(values, Modifier.fillMaxWidth().height(300.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("x[0] = ${"%.3f".format(values.first())}")
            Text("x[${values.lastIndex}] = ${"%.3f".format(values.last())}")
        }

        Text("Experiment guide", style = MaterialTheme.typography.titleMedium)
        Text("1. Select a signal type.\n2. Change one parameter at a time.\n3. Observe the discrete samples and describe the change in the waveform.")
        Text("Reflection: How does increasing frequency change the number of oscillations observed in the same sample window?")

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                amplitude = 1f; frequency = 2f; phaseDegrees = 0f; decay = 0.06f; sampleCount = 32
            }) { Text("Reset") }
            OutlinedButton(onClick = { typeIndex = (typeIndex + 1) % SignalType.entries.size }) {
                Text("Next signal")
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun ParameterSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    format: String,
    onValueChange: (Float) -> Unit
) {
    Text("$label: ${format.format(value)}")
    Slider(value = value, onValueChange = onValueChange, valueRange = range)
}

@Composable
private fun SignalPlot(values: List<Double>, modifier: Modifier = Modifier) {
    Canvas(modifier.padding(vertical = 8.dp)) {
        if (values.isEmpty()) return@Canvas
        val left = 42f
        val right = size.width - 16f
        val top = 20f
        val bottom = size.height - 30f
        val centerY = (top + bottom) / 2f
        val scaleY = (bottom - top) / 2.5f
        val width = right - left

        drawLine(Offset(left, centerY), Offset(right, centerY), strokeWidth = 2f)
        drawLine(Offset(left, top), Offset(left, bottom), strokeWidth = 2f)

        val path = Path()
        values.forEachIndexed { i, value ->
            val x = if (values.size == 1) left else left + width * i / (values.size - 1)
            val y = centerY - value.toFloat() * scaleY
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            // Stem plot: each sample is connected to the zero axis.
            drawLine(Offset(x, centerY), Offset(x, y), strokeWidth = 2f)
            drawCircle(Offset(x, y), radius = 5f)
        }
        // A light connecting curve helps students see the overall waveform trend.
        drawPath(path, style = Stroke(width = 2f))
    }
}
