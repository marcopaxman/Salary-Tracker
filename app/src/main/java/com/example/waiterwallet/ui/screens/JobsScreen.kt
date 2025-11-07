package com.example.waiterwallet.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.waiterwallet.data.Job
import com.example.waiterwallet.ui.viewmodel.JobsViewModel

@Composable
fun JobsScreen(
    onNavigateBack: () -> Unit,
    vm: JobsViewModel = viewModel(factory = JobsViewModel.Factory)
) {
    val jobs by vm.allJobs.collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    var editingJob by remember { mutableStateOf<Job?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Job")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Manage Jobs", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))
            Button(onClick = onNavigateBack) { Text("Back to Dashboard") }
            Spacer(Modifier.height(16.dp))

            if (jobs.isEmpty()) {
                Text(
                    "No jobs added yet. Tap + to add your first job.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(jobs) { job ->
                        JobCard(
                            job = job,
                            onEdit = { editingJob = job },
                            onDelete = { vm.deleteJob(job) }
                        )
                    }
                }
            }
        }
    }

    // Add/Edit Dialog
    if (showAddDialog) {
        JobDialog(
            job = null,
            onDismiss = { showAddDialog = false },
            onSave = { name ->
                vm.addJob(name)
                showAddDialog = false
            }
        )
    }

    if (editingJob != null) {
        JobDialog(
            job = editingJob,
            onDismiss = { editingJob = null },
            onSave = { name ->
                vm.updateJob(editingJob!!.copy(name = name))
                editingJob = null
            }
        )
    }
}

@Composable
fun JobCard(job: Job, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(job.name, style = MaterialTheme.typography.titleMedium)
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Job")
            }
        }
    }
}

@Composable
fun JobDialog(job: Job?, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var jobName by remember { mutableStateOf(job?.name ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (job == null) "Add Job" else "Edit Job") },
        text = {
            Column {
                OutlinedTextField(
                    value = jobName,
                    onValueChange = { jobName = it },
                    label = { Text("Job Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (jobName.isNotBlank()) onSave(jobName) },
                enabled = jobName.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
