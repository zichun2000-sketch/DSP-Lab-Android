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
import com.zichun2000.dsplab.dsp.*
import com.zichun2000.dsplab.lab.*
import kotlin.math.PI

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { Surface(Modifier.fillMaxSize()) { DspLabApp() } } }
    }
}

private enum class Page(val title: String) {
    HOME("Home"), SIGNAL("01 · Signals"), SAMPLING("02 · Sampling"), CONVOLUTION("03 · Convolution"), DFT("04 · DFT"), FILTER("05 · FIR Filter"), STUDY("Study"), DASHBOARD("Research")
}

@Composable
private fun DspLabApp() {
    var page by rememberSaveable { mutableStateOf(Page.HOME) }
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(10.dp), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            Page.entries.forEach { item -> FilterChip(selected = page == item, onClick = { page = item }, label = { Text(item.title) }) }
        }
        when (page) {
            Page.HOME -> HomeScreen(onStudy = { page = Page.STUDY }, onDashboard = { page = Page.DASHBOARD })
            Page.SIGNAL -> SignalLabScreen()
            Page.SAMPLING -> SamplingLabScreen()
            Page.CONVOLUTION -> ConvolutionLabScreen()
            Page.DFT -> DftLabScreen()
            Page.FILTER -> FilterLabScreen()
            Page.STUDY -> StudyScreen()
            Page.DASHBOARD -> ResearchDashboard()
        }
    }
}

@Composable
private fun HomeScreen(onStudy: () -> Unit, onDashboard: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("DSP Learning Platform", style = MaterialTheme.typography.headlineMedium)
        Text("Android-based interactive experiments for Digital Signal Processing.")
        Text("Course pathway", style = MaterialTheme.typography.titleLarge)
        listOf("01  Discrete-Time Signals", "02  Sampling & Aliasing", "03  Discrete Convolution", "04  DFT & Frequency Spectrum", "05  FIR Low-Pass Filter").forEach { Text("• $it") }
        Text("Research mode", style = MaterialTheme.typography.titleLarge)
        Text("Run a simple pre-test / learning activity / post-test sequence and inspect the local research summary.")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onStudy) { Text("Study & Assessment") }
            OutlinedButton(onClick = onDashboard) { Text("Research Dashboard") }
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
        Text("Lab 01 · Discrete-Time Signals", style = MaterialTheme.typography.titleLarge)
        Text("Explore amplitude, frequency, phase, and sampling density.")
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { SignalType.entries.forEachIndexed { index, signalType -> FilterChip(index == typeIndex, { typeIndex = index }, label = { Text(signalType.displayName) }) } }
        ParameterSlider("Amplitude", amplitude, 0.1f..2f, "%.2f") { amplitude = it }
        if (type == SignalType.SINE) {
            ParameterSlider("Frequency", frequency, 0.25f..12f, "%.2f") { frequency = it }
            ParameterSlider("Phase", phaseDegrees, 0f..360f, "%.0f°") { phaseDegrees = it }
        }
        if (type == SignalType.EXPONENTIAL) ParameterSlider("Decay", decay, 0.01f..0.20f, "%.2f") { decay = it }
        Text("Samples: $sampleCount")
        Slider(sampleCount.toFloat(), { sampleCount = it.toInt().coerceIn(16, 64) }, valueRange = 16f..64f, steps = 11)
        SignalPlot(values, Modifier.fillMaxWidth().height(300.dp))
        Text("Reflection: How does increasing frequency change the number of oscillations in the same sample window?")
    }
}

@Composable
private fun ParameterSlider(label: String, value: Float, range: ClosedFloatingPointRange<Float>, format: String, onValueChange: (Float) -> Unit) { Text("$label: ${format.format(value)}"); Slider(value, onValueChange, valueRange = range) }
