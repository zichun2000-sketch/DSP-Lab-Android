package com.zichun2000.dsplab.lab

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun FilterLabScreen() {
    var cutoff by rememberSaveable { mutableFloatStateOf(0.25f) }
    val n = 41
    val center = n / 2
    val taps = (0 until n).map { i ->
        val k = i - center
        if (k == 0) 2.0 * cutoff else sin(2.0 * PI * cutoff * k) / (PI * k)
    }
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("FIR Low-Pass Filter", style = MaterialTheme.typography.headlineSmall)
        Text("Relate cutoff frequency to the shape and coefficients of a finite impulse response.", style = MaterialTheme.typography.bodyMedium)
        ElevatedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("1 · Filter parameter", style = MaterialTheme.typography.titleMedium)
            Text("Normalized cutoff  ${"%.2f".format(cutoff)} × Fs")
            Slider(cutoff, { cutoff = it }, valueRange = 0.05f..0.45f)
            Text("Tap count  $n")
        }}
        ElevatedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("2 · Impulse response", style = MaterialTheme.typography.titleMedium)
            SignalPlot(taps, Modifier.fillMaxWidth().height(220.dp))
        }}
        ElevatedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("3 · Observation", style = MaterialTheme.typography.titleMedium)
            Text("Center tap: ${"%.4f".format(taps[center])}", style = MaterialTheme.typography.titleSmall)
            Text("A higher cutoff changes both the center coefficient and the spacing of the sinc-like oscillations.")
            Button(onClick = { cutoff = 0.25f }) { Text("Reset experiment") }
        }}
        ElevatedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("4 · Reflection", style = MaterialTheme.typography.titleMedium)
            LabResearchPanel("LAB05_FIR", "How does increasing the cutoff change the width and oscillation of the FIR impulse response? What would a window change?", mapOf("cutoff" to "%.3f".format(cutoff), "tapCount" to n.toString()))
        }}
    }
}
