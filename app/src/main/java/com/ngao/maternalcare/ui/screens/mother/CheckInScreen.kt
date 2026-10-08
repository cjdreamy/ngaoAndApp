package com.ngao.maternalcare.ui.screens.mother

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ngao.maternalcare.ui.theme.TextSecondary

private val symptomOptions = listOf(
    "Vaginal bleeding", "Dizziness", "Severe headache", "Blurred vision",
    "Severe abdominal pain", "Reduced fetal movement", "Swelling", "Convulsions", "Fever"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckInScreen(
    viewModel: MotherDashboardViewModel,
    onDone: () -> Unit
) {
    var systolic by remember { mutableStateOf("") }
    var diastolic by remember { mutableStateOf("") }
    var heartRate by remember { mutableStateOf("") }
    var fetalMovement by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    val selectedSymptoms = remember { mutableStateListOf<String>() }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daily Health Check-in") },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Vital Signs", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(10.dp))
            Row {
                OutlinedTextField(
                    value = systolic,
                    onValueChange = { systolic = it.filter { c -> c.isDigit() } },
                    label = { Text("BP Systolic") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(10.dp))
                OutlinedTextField(
                    value = diastolic,
                    onValueChange = { diastolic = it.filter { c -> c.isDigit() } },
                    label = { Text("BP Diastolic") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(10.dp))
            Row {
                OutlinedTextField(
                    value = heartRate,
                    onValueChange = { heartRate = it.filter { c -> c.isDigit() } },
                    label = { Text("Heart Rate (bpm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(10.dp))
                OutlinedTextField(
                    value = fetalMovement,
                    onValueChange = { fetalMovement = it.filter { c -> c.isDigit() } },
                    label = { Text("Fetal Movement (count)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(20.dp))
            Text("Any warning signs today?", style = MaterialTheme.typography.titleMedium)
            Text(
                "Select all that apply — this helps us flag your check-in for review",
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(8.dp))

            symptomOptions.forEach { symptom ->
                val checked = symptom in selectedSymptoms
                Row(
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .toggleable(value = checked, onValueChange = { isChecked ->
                            if (isChecked) selectedSymptoms.add(symptom) else selectedSymptoms.remove(symptom)
                        })
                        .padding(vertical = 4.dp)
                ) {
                    Checkbox(checked = checked, onCheckedChange = null)
                    Spacer(Modifier.width(8.dp))
                    Text(symptom)
                }
            }

            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Additional notes (optional)") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            errorMessage?.let {
                Spacer(Modifier.height(10.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    isSubmitting = true
                    errorMessage = null
                    viewModel.submitCheckIn(
                        bpSystolic = systolic.toIntOrNull(),
                        bpDiastolic = diastolic.toIntOrNull(),
                        heartRate = heartRate.toIntOrNull(),
                        fetalMovement = fetalMovement.toIntOrNull(),
                        symptoms = selectedSymptoms.toList(),
                        notes = notes
                    ) { success, error ->
                        isSubmitting = false
                        if (success) onDone() else errorMessage = error
                    }
                },
                enabled = !isSubmitting,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Submit Check-in")
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
