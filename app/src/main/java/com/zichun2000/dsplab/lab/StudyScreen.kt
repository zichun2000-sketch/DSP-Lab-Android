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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun StudyScreen() {
    val context = LocalContext.current
    val store = remember(context) { ExperimentRecordStore(context) }
    var stage by rememberSaveable { mutableIntStateOf(0) }
    var preScore by rememberSaveable { mutableIntStateOf(-1) }
    var postScore by rememberSaveable { mutableIntStateOf(-1) }
    // SnapshotStateMap is not directly saveable by rememberSaveable on all Compose versions.
    // The answers are only transient during the current assessment, so remember is sufficient.
    val answers = remember { mutableStateMapOf<String, Int>() }
    val items = LearningAssessment.prePostItems
    val score = LearningAssessment.score(answers)
    Column(
        Modifier.padding(horizontal = 16.dp, vertical = 12.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Learning Study", style = MaterialTheme.typography.headlineSmall)
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    if (stage == 0) "1 · Pre-test" else if (stage == 1) "2 · Learning activity" else "3 · Post-test",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    when (stage) {
                        0 -> "Check your starting DSP knowledge."
                        1 -> "Complete Labs 01–05, then take the post-test."
                        else -> "Check what you learned after the laboratory activities."
                    }
                )
            }
        }
        if (stage == 1) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text("Learning pathway", style = MaterialTheme.typography.titleMedium)
                    listOf("01  Signals", "02  Sampling", "03  Convolution", "04  DFT", "05  FIR Filter").forEach { Text(it) }
                }
            }
            Button(onClick = { stage = 2; answers.clear() }, modifier = Modifier.fillMaxWidth()) { Text("Start Post-test") }
        } else {
            items.forEachIndexed { number, item ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Q${number + 1}", style = MaterialTheme.typography.labelLarge)
                        Text(item.question, style = MaterialTheme.typography.titleMedium)
                        item.options.forEachIndexed { index, option ->
                            Row(Modifier.fillMaxWidth()) {
                                RadioButton(selected = answers[item.id] == index, onClick = { answers[item.id] = index })
                                Text(option, Modifier.padding(top = 12.dp))
                            }
                        }
                    }
                }
            }
            Text("Score: $score / ${items.size}", style = MaterialTheme.typography.titleMedium)
            Button(
                onClick = {
                    if (stage == 0) {
                        preScore = score
                        store.save(ExperimentRecord("PRE_TEST", emptyMap(), "score=$score/${items.size}"))
                        answers.clear()
                        stage = 1
                    } else {
                        postScore = score
                        store.save(ExperimentRecord("POST_TEST", emptyMap(), "score=$score/${items.size}"))
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (stage == 0) "Submit Pre-test" else "Submit Post-test") }
            if (postScore >= 0 && preScore >= 0) {
                val max = items.size.toDouble()
                val gain = if (max - preScore > 0) (postScore - preScore) / (max - preScore) else 0.0
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Learning outcome", style = MaterialTheme.typography.titleMedium)
                        Text("Pre-test: $preScore / ${items.size}")
                        Text("Post-test: $postScore / ${items.size}")
                        Text("Normalized learning gain <g>: ${"%.3f".format(gain)}")
                    }
                }
            }
        }
    }
}
