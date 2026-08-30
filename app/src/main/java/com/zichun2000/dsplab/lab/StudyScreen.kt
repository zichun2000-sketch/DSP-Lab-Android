package com.zichun2000.dsplab.lab

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun StudyScreen() {
    var stage by rememberSaveable { mutableIntStateOf(0) }
    val answers = rememberSaveable { mutableStateMapOf<String, Int>() }
    Column(Modifier.padding(20.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("DSP Learning Study", style = MaterialTheme.typography.headlineSmall)
        Text(when (stage) { 0 -> "Pre-test: establish baseline DSP knowledge."; 1 -> "Complete Labs 01–05, then return here for the post-test."; else -> "Post-test: measure learning gain." })
        if (stage == 1) {
            Text("Study sequence", style = MaterialTheme.typography.titleMedium)
            listOf("01 Signals", "02 Sampling", "03 Convolution", "04 DFT", "05 FIR Filter").forEach { Text("• $it") }
            Button(onClick = { stage = 2 }) { Text("Start Post-test") }
        } else {
            LearningAssessment.prePostItems.forEach { item ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text(item.question, style = MaterialTheme.typography.titleMedium)
                        item.options.forEachIndexed { index, option ->
                            Row {
                                RadioButton(selected = answers[item.id] == index, onClick = { answers[item.id] = index })
                                Text(option, Modifier.padding(top = 12.dp))
                            }
                        }
                    }
                }
            }
            val score = LearningAssessment.score(answers)
            Text("Current score: $score / ${LearningAssessment.prePostItems.size}")
            Button(onClick = { stage = if (stage == 0) 1 else 2 }) { Text(if (stage == 0) "Submit Pre-test" else "Submit Post-test") }
        }
    }
}
