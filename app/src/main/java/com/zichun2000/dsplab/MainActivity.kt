package com.zichun2000.dsplab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zichun2000.dsplab.dsp.SignalParameters
import com.zichun2000.dsplab.dsp.SignalType
import com.zichun2000.dsplab.dsp.generateDiscreteSignal
import com.zichun2000.dsplab.lab.ConvolutionLabScreen
import com.zichun2000.dsplab.lab.DftLabScreen
import com.zichun2000.dsplab.lab.FilterLabScreen
import com.zichun2000.dsplab.lab.LabResearchPanel
import com.zichun2000.dsplab.lab.ResearchDashboard
import com.zichun2000.dsplab.lab.SamplingLabScreen
import com.zichun2000.dsplab.lab.SignalPlot
import com.zichun2000.dsplab.lab.StudyScreen
import kotlin.math.PI

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { Surface(Modifier.fillMaxSize()) { DspLabApp() } } }
    }
}

private enum class Section { HOME, LABS, STUDY, RESEARCH }
private enum class Lab(val number: String, val title: String, val description: String) {
    SIGNAL("01", "Discrete-Time Signals", "Generate and analyze basic discrete signals."),
    SAMPLING("02", "Sampling & Aliasing", "Explore sampling rate and aliasing."),
    CONVOLUTION("03", "Discrete Convolution", "Understand convolution through interactive examples."),
    DFT("04", "DFT & Frequency Spectrum", "Observe the transition from time to frequency domain."),
    FILTER("05", "FIR Low-Pass Filter", "Explore FIR filtering and frequency response.")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DspLabApp() {
    var section by rememberSaveable { mutableStateOf(Section.HOME) }
    var selectedLab by rememberSaveable { mutableStateOf<Lab?>(null) }
    val showLab = selectedLab != null
    Scaffold(
        topBar = { if (showLab) androidx.compose.material3.TopAppBar(title = { Text("Lab ${selectedLab!!.number} · ${selectedLab!!.title}") }, navigationIcon = { IconButton(onClick = { selectedLab = null }) { Icon(Icons.Default.ArrowBack, "Back") } }) else androidx.compose.material3.TopAppBar(title = { Text("DSP Learning Platform") }) },
        bottomBar = { if (!showLab) NavigationBar(Modifier.navigationBarsPadding()) {
            NavigationBarItem(selected = section == Section.HOME, onClick = { section = Section.HOME }, icon = { Icon(Icons.Default.Home, null) }, label = { Text("Home") })
            NavigationBarItem(selected = section == Section.LABS, onClick = { section = Section.LABS }, icon = { Icon(Icons.Default.List, null) }, label = { Text("Labs") })
            NavigationBarItem(selected = section == Section.STUDY, onClick = { section = Section.STUDY }, icon = { Icon(Icons.Default.Home, null) }, label = { Text("Study") })
            NavigationBarItem(selected = section == Section.RESEARCH, onClick = { section = Section.RESEARCH }, icon = { Icon(Icons.Default.List, null) }, label = { Text("Research") })
        }}
    ) { innerPadding ->
        Column(Modifier.fillMaxSize().padding(innerPadding)) {
            if (selectedLab != null) when (selectedLab!!) {
                Lab.SIGNAL -> SignalLabScreen()
                Lab.SAMPLING -> SamplingLabScreen()
                Lab.CONVOLUTION -> ConvolutionLabScreen()
                Lab.DFT -> DftLabScreen()
                Lab.FILTER -> FilterLabScreen()
            } else when (section) {
                Section.HOME -> HomeScreen(onLabs = { section = Section.LABS }, onStudy = { section = Section.STUDY })
                Section.LABS -> LabsScreen { selectedLab = it }
                Section.STUDY -> StudyScreen()
                Section.RESEARCH -> ResearchDashboard()
            }
        }
    }
}

@Composable
private fun HomeScreen(onLabs: () -> Unit, onStudy: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Digital Signal Processing", style = MaterialTheme.typography.headlineMedium)
        Text("Interactive Android experiments for learning DSP concepts through visualization and parameter manipulation.", style = MaterialTheme.typography.bodyLarge)
        Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("5 interactive laboratories", style = MaterialTheme.typography.titleLarge); Text("Signals → Sampling → Convolution → DFT → FIR Filter"); Button(onClick = onLabs, modifier = Modifier.fillMaxWidth()) { Text("Explore Labs") } }}
        Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("Learning Study", style = MaterialTheme.typography.titleLarge); Text("Complete a pre-test, learning activities, and post-test to estimate learning gain."); OutlinedButton(onClick = onStudy, modifier = Modifier.fillMaxWidth()) { Text("Start Study") } }}
        Text("Designed as a lightweight teaching-research prototype.", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun LabsScreen(onSelect: (Lab) -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("DSP Laboratories", style = MaterialTheme.typography.headlineSmall)
        Text("Choose an experiment. Each lab is optimized for portrait-phone use.")
        Lab.entries.forEach { lab -> Card(onClick = { onSelect(lab) }, modifier = Modifier.fillMaxWidth()) { Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) { Text(lab.number, style = MaterialTheme.typography.titleLarge); Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) { Text(lab.title, style = MaterialTheme.typography.titleMedium); Text(lab.description, style = MaterialTheme.typography.bodyMedium) } }} }
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
    val mean = values.average()
    val maxValue = values.maxOrNull() ?: 0.0
    val minValue = values.minOrNull() ?: 0.0
    val peakToPeak = maxValue - minValue
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Discrete-Time Signals", style = MaterialTheme.typography.headlineSmall)
        Text("Generate a signal, measure its basic properties, and investigate how amplitude, frequency, phase, and decay affect a discrete sequence.", style = MaterialTheme.typography.bodyMedium)
        ElevatedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) { Text("1 · Signal type", style = MaterialTheme.typography.titleMedium); Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { SignalType.entries.forEachIndexed { index, signalType -> FilterChip(selected = index == typeIndex, onClick = { typeIndex = index }, label = { Text(signalType.displayName) }) } } }}
        ElevatedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) { Text("2 · Parameters", style = MaterialTheme.typography.titleMedium); ParameterSlider("Amplitude", amplitude, 0.1f..2f, "%.2f") { amplitude = it }; if (type == SignalType.SINE) { ParameterSlider("Frequency", frequency, 0.25f..12f, "%.2f") { frequency = it }; ParameterSlider("Phase", phaseDegrees, 0f..360f, "%.0f°") { phaseDegrees = it } }; if (type == SignalType.EXPONENTIAL) ParameterSlider("Decay", decay, 0.01f..0.20f, "%.2f") { decay = it }; Text("Samples  $sampleCount"); Slider(value = sampleCount.toFloat(), onValueChange = { sampleCount = it.toInt().coerceIn(16, 64) }, valueRange = 16f..64f, steps = 11) }}
        ElevatedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("3 · Visualization", style = MaterialTheme.typography.titleMedium); SignalPlot(values, Modifier.fillMaxWidth().height(220.dp)); Text("Samples shown: $sampleCount") }}
        ElevatedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("4 · Quantitative observation", style = MaterialTheme.typography.titleMedium)
            Text("Maximum: ${"%.3f".format(maxValue)}   Minimum: ${"%.3f".format(minValue)}")
            Text("Mean: ${"%.3f".format(mean)}   Peak-to-peak: ${"%.3f".format(peakToPeak)}")
            Text("Investigation: keep samples fixed, then change only one parameter. Compare the waveform and these measurements before writing your conclusion.")
            Button(onClick = { amplitude = 1f; frequency = 2f; phaseDegrees = 0f; decay = 0.06f; sampleCount = 32 }) { Text("Reset experiment") }
        }}
        ElevatedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) { Text("5 · Reflection", style = MaterialTheme.typography.titleMedium); LabResearchPanel("LAB01_SIGNALS", "Investigation: compare two frequencies with the same sample count. Then compare two amplitudes. What changes in oscillation rate, peak-to-peak value, and mean? Explain the effect of phase for a sinusoid.", mapOf("signalType" to type.displayName, "amplitude" to "%.2f".format(amplitude), "frequency" to "%.2f".format(frequency), "phaseDegrees" to "%.0f".format(phaseDegrees), "samples" to sampleCount.toString(), "mean" to "%.3f".format(mean), "peakToPeak" to "%.3f".format(peakToPeak))) }}
    }
}

@Composable
private fun ParameterSlider(label: String, value: Float, range: ClosedFloatingPointRange<Float>, format: String, onValueChange: (Float) -> Unit) { Text("$label  ${format.format(value)}"); Slider(value = value, onValueChange = onValueChange, valueRange = range) }