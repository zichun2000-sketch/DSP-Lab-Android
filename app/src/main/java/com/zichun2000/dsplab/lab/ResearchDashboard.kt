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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun ResearchDashboard() {
    val context = LocalContext.current
    var refreshKey by remember { mutableStateOf(0) }
    val store = remember(context, refreshKey) { ExperimentRecordStore(context) }
    val records = remember(refreshKey) { store.loadAll() }
    val preRecord = records.lastOrNull { it.labId == "PRE_TEST" }
    val postRecord = records.lastOrNull { it.labId == "POST_TEST" }
    val pre = store.getAssessmentScore("PRE_TEST") ?: preRecord?.observation?.substringAfter("score=")?.substringBefore("/")?.toIntOrNull()
    val post = store.getAssessmentScore("POST_TEST") ?: postRecord?.observation?.substringAfter("score=")?.substringBefore("/")?.toIntOrNull()
    val maxScore = (store.getAssessmentMax("POST_TEST") ?: store.getAssessmentMax("PRE_TEST") ?: LearningAssessment.prePostItems.size).toDouble()
    val labIds = listOf(
        "LAB01_SIGNALS",
        "LAB02_SAMPLING",
        "LAB03_CONVOLUTION",
        "LAB04_DFT",
        "LAB05_FIR"
    )
    val completedLabs = labIds.associateWith { id ->
        store.isLabCompleted(id) || records.any { it.labId == id }
    }
    val labCount = completedLabs.values.count { it }

    Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Research", style = MaterialTheme.typography.headlineSmall)
        Text("Local learning-study summary", style = MaterialTheme.typography.bodyMedium)
        Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Assessment", style = MaterialTheme.typography.titleMedium)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Pre-test"); Text(pre?.let { "$it / ${maxScore.toInt()}" } ?: "Not recorded") }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Post-test"); Text(post?.let { "$it / ${maxScore.toInt()}" } ?: "Not recorded") }
            if (pre != null && post != null) {
                val gain = if (maxScore - pre > 0) (post - pre) / (maxScore - pre) else 0.0
                Text("Normalized learning gain <g>", style = MaterialTheme.typography.labelLarge)
                Text("${"%.3f".format(gain)}", style = MaterialTheme.typography.headlineMedium)
            } else {
                Text("Submit both assessments to calculate <g>.")
            }
        }}
        Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text("Activity", style = MaterialTheme.typography.titleMedium)
            Text("Labs completed: $labCount / 5")
            labIds.forEachIndexed { index, id ->
                Text("Lab ${index + 1}: ${if (completedLabs[id] == true) "✓" else "—"}")
            }
            Text("Recorded events: ${records.size}")
            Text("Pre-test record: ${if (preRecord != null || pre != null) "✓" else "—"}    Post-test record: ${if (postRecord != null || post != null) "✓" else "—"}")
            Text("Data remain on this device in the prototype.")
        }}
        Button(onClick = { refreshKey++ }, modifier = Modifier.fillMaxWidth()) { Text("Refresh data") }
        Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(14.dp)) {
            Text("Research note", style = MaterialTheme.typography.titleMedium)
            Text("Use aggregated and de-identified records for educational-research analysis.")
        }}
    }
}
