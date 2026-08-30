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
import androidx.compose.material3.OutlinedButton
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
    val records = remember(refreshKey) { ExperimentRecordStore(context).loadAll() }
    val pre = records.lastOrNull { it.labId == "PRE_TEST" }?.observation?.substringAfter("score=")?.substringBefore("/")?.toIntOrNull()
    val post = records.lastOrNull { it.labId == "POST_TEST" }?.observation?.substringAfter("score=")?.substringBefore("/")?.toIntOrNull()
    Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Research", style = MaterialTheme.typography.headlineSmall)
        Text("Local learning-study summary", style = MaterialTheme.typography.bodyMedium)
        Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Assessment", style = MaterialTheme.typography.titleMedium)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Pre-test"); Text(pre?.toString() ?: "—") }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Post-test"); Text(post?.toString() ?: "—") }
            if (pre != null && post != null) {
                val max = 4.0
                val gain = if (max - pre > 0) (post - pre) / (max - pre) else 0.0
                Text("Normalized learning gain <g>", style = MaterialTheme.typography.labelLarge)
                Text("${"%.3f".format(gain)}", style = MaterialTheme.typography.headlineMedium)
            } else Text("Complete both assessments to calculate <g>.")
        }}
        Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text("Activity", style = MaterialTheme.typography.titleMedium)
            Text("Recorded events: ${records.size}")
            Text("Data remain on this device in the prototype.")
        }}
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { refreshKey++ }, modifier = Modifier.weight(1f)) { Text("Refresh") }
            OutlinedButton(onClick = { refreshKey++ }, modifier = Modifier.weight(1f)) { Text("Reload") }
        }
        Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(14.dp)) {
            Text("Research note", style = MaterialTheme.typography.titleMedium)
            Text("Use aggregated and de-identified records for educational-research analysis.")
        }}
    }
}
