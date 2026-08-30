package com.zichun2000.dsplab.lab

import android.content.Context
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun StudyScreen() {
    val context = LocalContext.current
    val store = ExperimentRecordStore(context)
    var stage by rememberSaveable { mutableIntStateOf(0) }
    var preScore by rememberSaveable { mutableIntStateOf(-1) }
    var postScore by rememberSaveable { mutableIntStateOf(-1) }
    val answers = rememberSaveable { mutableStateMapOf<String, Int>() }
    val items = LearningAssessment.prePostItems
    val score = LearningAssessment.score(answers)
    Column(Modifier.padding(20.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("DSP Learning Study", style = MaterialTheme.typography.headlineSmall)
        Text(when (stage) { 0 -> "Pre-test: establish baseline DSP knowledge."; 1 -> "Learning activity: complete Labs 01–05."; else -> "Post-test: measure learning gain." })
        if (stage == 1) {
            Text("Study sequence", style = MaterialTheme.typography.titleMedium)
            listOf("01 Signals", "02 Sampling", "03 Convolution", "04 DFT", "05 FIR Filter").forEach { Text("• $it") }
            Button(onClick = { stage = 2; answers.clear() }) { Text("Start Post-test") }
        } else {
            items.forEach { item ->
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
            Text("Current score: $score / ${items.size}")
            Button(onClick = {
                if (stage == 0) {
                    preScore = score
                    store.save(ExperimentRecord("PRE_TEST", emptyMap(), "score=$score/${items.size}"))
                    answers.clear()
                    stage = 1
                } else {
                    postScore = score
                    store.save(ExperimentRecord("POST_TEST", emptyMap(), "score=$score/${items.size}"))
                }
            }) { Text(if (stage == 0) "Submit Pre-test" else "Submit Post-test") }
            if (postScore >= 0 && preScore >= 0) {
                val max = items.size.toDouble()
                val gain = if (max - preScore > 0) (postScore - preScore) / (max - preScore) else 0.0
                Text("Pre-test: $preScore / ${items.size}")
                Text("Post-test: $postScore / ${items.size}")
                Text("Normalized learning gain <g>: ${"%.3f".format(gain)}")
                Text("The score and gain are stored locally for later educational-research analysis.")
            }
        }
    }
}
