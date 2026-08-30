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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ResearchDashboard() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val records = remember { ExperimentRecordStore(context).loadAll() }
    val pre = records.lastOrNull { it.labId == "PRE_TEST" }?.observation?.substringAfter("score=")?.substringBefore("/")?.toIntOrNull()
    val post = records.lastOrNull { it.labId == "POST_TEST" }?.observation?.substringAfter("score=")?.substringBefore("/")?.toIntOrNull()
    Column(Modifier.padding(20.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Research Dashboard", style = MaterialTheme.typography.headlineSmall)
        Text("Local summary for educational-research pilots. No network upload is performed.")
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Assessment summary", style = MaterialTheme.typography.titleMedium)
                Text("Pre-test: ${pre?.toString() ?: "—"}")
                Text("Post-test: ${post?.toString() ?: "—"}")
                if (pre != null && post != null) {
                    val max = 4.0
                    val gain = if (max - pre > 0) (post - pre) / (max - pre) else 0.0
                    Text("Normalized learning gain <g>: ${"%.3f".format(gain)}")
                } else Text("Complete both assessments to calculate <g>.")
            }
        }
        Text("Recorded events: ${records.size}")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { /* records are refreshed when the screen is recreated */ }) { Text("Refresh") }
        }
        Text("Research note: use aggregated and de-identified data for publication; the current prototype stores records locally on the device.")
    }
}
