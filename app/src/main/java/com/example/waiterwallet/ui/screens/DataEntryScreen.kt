package com.example.waiterwallet.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.waiterwallet.ui.viewmodel.DailyEntryViewModel
import com.example.waiterwallet.ui.viewmodel.JobsViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataEntryScreen(
    onSaved: () -> Unit,
    vm: DailyEntryViewModel = viewModel(factory = DailyEntryViewModel.Factory),
    jobsVm: JobsViewModel = viewModel(factory = JobsViewModel.Factory)
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var turnover by remember { mutableStateOf("") }
    var tipsCash by remember { mutableStateOf("") }
    var tipsCard by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    
    val jobs by jobsVm.allJobs.collectAsState(initial = emptyList())
    var selectedJobId by remember { mutableStateOf<Long?>(null) }
    var jobDropdownExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Add Entry", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        
        // Date picker
        OutlinedTextField(
            value = selectedDate.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")),
            onValueChange = {},
            label = { Text("Date") },
            readOnly = true,
            trailingIcon = {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = "Select date",
                    modifier = Modifier.clickable { showDatePicker = true }
                )
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        
        // Job selector (optional)
        if (jobs.isNotEmpty()) {
            ExposedDropdownMenuBox(
                expanded = jobDropdownExpanded,
                onExpandedChange = { jobDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = jobs.find { it.id == selectedJobId }?.name ?: "No job selected",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Job (Optional)") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = jobDropdownExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                )
                ExposedDropdownMenu(
                    expanded = jobDropdownExpanded,
                    onDismissRequest = { jobDropdownExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("No job") },
                        onClick = {
                            selectedJobId = null
                            jobDropdownExpanded = false
                        }
                    )
                    jobs.forEach { job ->
                        DropdownMenuItem(
                            text = { Text(job.name) },
                            onClick = {
                                selectedJobId = job.id
                                jobDropdownExpanded = false
                            }
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
        
        OutlinedTextField(
            value = turnover,
            onValueChange = { turnover = it },
            label = { Text("Turnover") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = tipsCash,
            onValueChange = { tipsCash = it },
            label = { Text("Cash Tips") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = tipsCard,
            onValueChange = { tipsCard = it },
            label = { Text("Card Tips") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notes (optional)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = {
            vm.save(
                date = selectedDate,
                turnover = turnover.toDoubleOrNull() ?: 0.0,
                tipsCash = tipsCash.toDoubleOrNull(),
                tipsCard = tipsCard.toDoubleOrNull(),
                notes = notes.ifBlank { null },
                jobId = selectedJobId
            )
            onSaved()
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Save")
        }
    }
    
    // Date picker dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        selectedDate = Instant.ofEpochMilli(it)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
