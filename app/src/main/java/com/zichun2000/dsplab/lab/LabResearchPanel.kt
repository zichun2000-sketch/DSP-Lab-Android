package com.zichun2000.dsplab.lab

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun LabResearchPanel(
    labId: String,
    question: String,
    parameters: Map<String, String> = emptyMap()
) {
    val context = LocalContext.current
    val store = remember(context) { ExperimentRecordStore(context) }
    var response by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }
    var saveFailed by remember { mutableStateOf(false) }

    Column(
        Modifier.fillMaxWidth().padding(top = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Reflection response")
        Text(question)
        OutlinedTextField(
            value = response,
            onValueChange = { response = it },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            label = { Text("Your answer") }
        )
        Button(
            onClick = {
                val recordSaved = store.save(
                    ExperimentRecord(
                        labId,
                        parameters,
                        response.trim().ifEmpty { "No reflection provided" }
                    )
                )
                val completionSaved = recordSaved && store.markLabCompleted(labId)
                submitted = completionSaved
                saveFailed = !completionSaved
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (submitted) "Experiment submitted ✓" else "Submit Experiment") }
        if (submitted) Text("Saved locally and marked complete for the teaching-research record.")
        if (saveFailed) Text("Unable to save this experiment. Please tap Submit Experiment again.")
    }
}
