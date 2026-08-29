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
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import kotlin.math.PI
import kotlin.math.sin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SignalLabScreen()
                }
            }
        }
    }
}

private enum class SignalType(val title: String) {
    SINE("Sine"), IMPULSE("Unit Impulse"), STEP("Unit Step")
}

private fun generateSignal(type: SignalType, n: Int, frequency: Float, phase: Float): Float {
    return when (type) {
        SignalType.SINE -> sin(2.0 * PI * frequency * n / 32.0 + phase).toFloat()
        SignalType.IMPULSE -> if (n == 0) 1f else 0f
        SignalType.STEP -> if (n >= 0) 1f else 0f
    }
}

@androidx.compose.runtime.Composable
private fun SignalLabScreen() {
    var signalTypeIndex by rememberSaveable { mutableIntStateOf(0) }
    var frequency by rememberSaveable { mutableFloatStateOf(2f) }
    var phase by rememberSaveable { mutableFloatStateOf(0f) }
    var samples by rememberSaveable { mutableIntStateOf(32) }
    val signalType = SignalType.entries[signalTypeIndex]
    val values = (0 until samples).map { generateSignal(signalType, it, frequency, phase) }

    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("DSP Lab", style = MaterialTheme.typography.headlineMedium)
        Text("Lab 01 · Discrete-Time Signal Visualization", style = MaterialTheme.typography.titleMedium)
        Text("Explore how signal parameters change the discrete-time waveform.")

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SignalType.entries.forEachIndexed { index, type ->
                if (index == signalTypeIndex) {
                    Button(onClick = { signalTypeIndex = index }) { Text(type.title) }
                } else {
                    OutlinedButton(onClick = { signalTypeIndex = index }) { Text(type.title) }
                }
            }
        }

        Text("Frequency: ${"%.1f".format(frequency)} cycles / 32 samples")
        Slider(value = frequency, onValueChange = { frequency = it }, valueRange = 0.5f..12f)

        Text("Phase: ${"%.0f".format(phase * 180 / PI)}°")
        Slider(value = phase, onValueChange = { phase = it }, valueRange = 0f..(2f * PI.toFloat()))

        Text("Samples: $samples")
        Slider(value = samples.toFloat(), onValueChange = { samples = it.toInt() }, valueRange = 16f..64f, steps = 2)

        Text("Time-domain waveform", style = MaterialTheme.typography.titleMedium)
        SignalPlot(values = values, modifier = Modifier.fillMaxWidth().weight(1f))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("x[0] = ${"%.3f".format(values.first())}")
            Text("x[${values.lastIndex}] = ${"%.3f".format(values.last())}")
        }
        Spacer(Modifier.height(4.dp))
        Text("Learning focus: amplitude, frequency, phase, and discrete-time representation.")
    }
}

@androidx.compose.runtime.Composable
private fun SignalPlot(values: List<Float>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.padding(vertical = 8.dp)) {
        if (values.isEmpty()) return@Canvas
        val left = 36f
        val right = size.width - 12f
        val top = 16f
        val bottom = size.height - 20f
        val centerY = top + (bottom - top) / 2f
        val scaleY = (bottom - top) / 2.4f
        val usableWidth = right - left

        drawLine(Offset(left, centerY), Offset(right, centerY), strokeWidth = 2f)
        drawLine(Offset(left, top), Offset(left, bottom), strokeWidth = 2f)

        val path = Path()
        values.forEachIndexed { i, value ->
            val x = if (values.size == 1) left else left + usableWidth * i / (values.size - 1)
            val y = centerY - value * scaleY
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            drawLine(Offset(x, centerY), Offset(x, y), strokeWidth = 2f)
            drawCircle(Offset(x, y), radius = 4f)
        }
        drawPath(path, style = Stroke(width = 3f))
    }
}
