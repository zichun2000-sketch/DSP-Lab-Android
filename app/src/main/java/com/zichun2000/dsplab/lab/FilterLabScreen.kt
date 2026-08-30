package com.zichun2000.dsplab.lab

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
    Column(Modifier.padding(20.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Lab 05 · FIR Low-Pass Filter", style = MaterialTheme.typography.titleLarge)
        Text("Inspect a simple windowless sinc FIR impulse response and observe how the cutoff controls the coefficients.")
        Text("Normalized cutoff: ${"%.2f".format(cutoff)} × Fs")
        Slider(cutoff, { cutoff = it }, valueRange = 0.05f..0.45f)
        Text("Impulse response", style = MaterialTheme.typography.titleMedium)
        SignalPlot(taps, Modifier.fillMaxWidth().height(260.dp))
        Text("Tap count: $n")
        Text("Center tap: ${"%.4f".format(taps[center])}")
        Text("Reflection: How does increasing the cutoff change the width and oscillation of the FIR impulse response?")
    }
}
