package com.zichun2000.dsplab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zichun2000.dsplab.dsp.SignalParameters
import com.zichun2000.dsplab.dsp.SignalType
import com.zichun2000.dsplab.dsp.generateDiscreteSignal
import com.zichun2000.dsplab.lab.ConvolutionLabScreen
import com.zichun2000.dsplab.lab.DftLabScreen
import com.zichun2000.dsplab.lab.SamplingLabScreen
import kotlin.math.PI

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { Surface(Modifier.fillMaxSize()) { DspLabApp() } } }
    }
}

private enum class Lab(val title: String) {
    SIGNAL("01 · Signals"), SAMPLING("02 · Sampling"), CONVOLUTION("03 · Convolution"), DFT("04 · DFT"), FILTER("05 · FIR Filter")
}

@Composable
private fun DspLabApp() {
    var selectedLab by rememberSaveable { mutableIntStateOf(0) }
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Lab.entries.forEachIndexed { index, lab -> FilterChip(selected = selectedLab == index, onClick = { selectedLab = index }, label = { Text(lab.title) }) }
        }
        when (Lab.entries[selectedLab]) {
            Lab.SIGNAL -> SignalLabScreen()
            Lab.SAMPLING -> SamplingLabScreen()
            Lab.CONVOLUTION -> ConvolutionLabScreen()
            Lab.DFT -> DftLabScreen()
            Lab.FILTER -> FilterLabScreen()
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
    val values = generateDiscreteSignal(type, sampleCount, SignalParameters(amplitude.toDouble(), frequency.toDouble(), phase.toDouble(), decay.toDouble()))
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("DSP Lab", style = MaterialTheme.typography.headlineMedium)
        Text("Lab 01 · Discrete-Time Signals", style = MaterialTheme.typography.titleLarge)
        Text("Explore how amplitude, frequency, phase, and sampling density affect a discrete-time signal.")
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            SignalType.entries.forEachIndexed { index, signalType -> FilterChip(index == typeIndex, { typeIndex = index }, label = { Text(signalType.displayName) }) }
        }
        ParameterSlider("Amplitude", amplitude, 0.1f..2f, "%.2f") { amplitude = it }
        if (type == SignalType.SINE) {
            ParameterSlider("Frequency (cycles/window)", frequency, 0.25f..12f, "%.2f") { frequency = it }
            ParameterSlider("Phase", phaseDegrees, 0f..360f, "%.0f°") { phaseDegrees = it }
        }
        if (type == SignalType.EXPONENTIAL) ParameterSlider("Decay", decay, 0.01f..0.20f, "%.2f") { decay = it }
        Text("Samples: $sampleCount")
        Slider(sampleCount.toFloat(), { sampleCount = it.toInt().coerceIn(16, 64) }, valueRange = 16f..64f, steps = 11)
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
            Button(onClick = { amplitude = 1f; frequency = 2f; phaseDegrees = 0f; decay = 0.06f; sampleCount = 32 }) { Text("Reset") }
            OutlinedButton(onClick = { typeIndex = (typeIndex + 1) % SignalType.entries.size }) { Text("Next signal") }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun ParameterSlider(label: String, value: Float, range: ClosedFloatingPointRange<Float>, format: String, onValueChange: (Float) -> Unit) {
    Text("$label: ${format.format(value)}")
    Slider(value, onValueChange, valueRange = range)
}

@Composable
private fun FilterLabScreen() {
    var cutoff by rememberSaveable { mutableFloatStateOf(0.18f) }
    val sampleRate = 8000.0
    val input = mixedSignal(sampleRate, 96)
    val coefficients = designLowPassFir(9, cutoff.toDouble())
    val output = firFilter(input, coefficients)
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Lab 05 · FIR Low-Pass Filter", style = MaterialTheme.typography.titleLarge)
        Text("Remove the high-frequency component of a mixed discrete-time signal using a short FIR filter.")
        Text("Sampling rate: ${sampleRate.toInt()} Hz")
        Text("Normalized cutoff: ${"%.2f".format(cutoff)} · cutoff = ${"%.0f".format(cutoff * sampleRate / 2.0)} Hz")
        Slider(cutoff, { cutoff = it }, valueRange = 0.05f..0.45f)
        Text("Input: 500 Hz + 2200 Hz")
        SignalPlot(input, Modifier.fillMaxWidth().height(180.dp))
        Text("Filtered output", style = MaterialTheme.typography.titleMedium)
        SignalPlot(output, Modifier.fillMaxWidth().height(180.dp))
        Text("FIR coefficients", style = MaterialTheme.typography.titleMedium)
        Text(coefficients.joinToString(prefix = "[", postfix = "]") { "%.3f".format(it) })
        Text("Expected observation: when the cutoff is below the normalized 2200 Hz component, the output is dominated by the 500 Hz component.")
        Text("Reflection: What is the trade-off between cutoff frequency, filter length, and transition sharpness?")
        Button(onClick = { cutoff = 0.18f }) { Text("Reset") }
    }
}

private fun designLowPassFir(taps: Int, cutoff: Double): List<Double> {
    val m = taps - 1
    val raw = (0 until taps).map { n ->
        val k = n - m / 2.0
        val ideal = if (k == 0.0) 2.0 * cutoff else kotlin.math.sin(2.0 * PI * cutoff * k) / (PI * k)
        val window = 0.54 - 0.46 * kotlin.math.cos(2.0 * PI * n / m)
        ideal * window
    }
    val sum = raw.sum()
    return raw.map { it / sum }
}

private fun firFilter(input: List<Double>, coefficients: List<Double>): List<Double> = input.indices.map { n -> coefficients.indices.sumOf { k -> if (n - k >= 0) input[n - k] * coefficients[k] else 0.0 } }

private fun mixedSignal(sampleRate: Double, count: Int): List<Double> = (0 until count).map { n -> val t = n / sampleRate; kotlin.math.sin(2.0 * PI * 500.0 * t) + 0.6 * kotlin.math.sin(2.0 * PI * 2200.0 * t) }
