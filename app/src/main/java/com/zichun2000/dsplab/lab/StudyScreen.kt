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
    var postSubmitted by rememberSaveable { mutableIntStateOf(0) }
    var postSaveFailed by rememberSaveable { mutableIntStateOf(0) }
    val answers = remember { mutableStateMapOf<String, Int>() }
    val items = LearningAssessment.prePostItems
    val score = LearningAssessment.score(answers)
    val answered = answers.size

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
            Button(onClick = { stage = 2; answers.clear(); postSubmitted = 0; postSaveFailed = 0 }, modifier = Modifier.fillMaxWidth()) { Text("Start Post-test") }
        } else if (postSubmitted == 1) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text("✓ Post-test submitted", style = MaterialTheme.typography.titleMedium)
                    Text("Score: $postScore / ${items.size}")
                    if (preScore >= 0) {
                        val max = items.size.toDouble()
                        val gain = if (max - preScore > 0) (postScore - preScore) / (max - preScore) else 0.0
                        Text("Normalized learning gain <g>: ${"%.3f".format(gain)}")
                    }
                    Text("The result has been saved for the Research dashboard.")
                }
            }
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
            Text("Answered: $answered / ${items.size}    Score: $score / ${items.size}", style = MaterialTheme.typography.titleMedium)
            Button(
                onClick = {
                    if (answered < items.size) return@Button
                    if (stage == 0) {
                        preScore = score
                        store.saveAssessment("PRE_TEST", score, items.size)
                        answers.clear()
                        stage = 1
                    } else {
                        postScore = score
                        postSaveFailed = if (store.saveAssessment("POST_TEST", score, items.size)) 0 else 1
                        if (postSaveFailed == 0) postSubmitted = 1
                    }
                },
                enabled = answered == items.size,
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (stage == 0) "Submit Pre-test" else "Submit Post-test") }
            if (answered < items.size) Text("Answer all ${items.size} questions before submitting.")
            if (postSaveFailed == 1) Text("⚠ Unable to save the Post-test result. Please try Submit again.")
        }
    }
}
