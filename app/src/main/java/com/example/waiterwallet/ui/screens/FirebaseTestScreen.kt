package com.example.waiterwallet.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.waiterwallet.data.firebase.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

/**
 * Test screen for Firebase Firestore operations.
 * This screen allows testing CRUD operations for all data models.
 * 
 * Usage: Add this screen to your navigation for testing, remove in production.
 */
@Composable
fun FirebaseTestScreen() {
    val repository = remember { FirestoreRepository() }
    val scope = rememberCoroutineScope()
    
    var statusMessage by remember { mutableStateOf("Ready to test Firebase operations") }
    var isLoading by remember { mutableStateOf(false) }
    
    // Sample data for testing
    val testEntry = FirestoreDailyEntry(
        id = LocalDate.now().toString(),
        date = LocalDate.now().toString(),
        turnover = 1500.0,
        tipsCash = 75.0,
        tipsCard = 45.0,
        notes = "Test entry from Firebase test screen"
    )
    
    val testJob = FirestoreJob(
        id = "",
        name = "Test Restaurant"
    )
    
    val testGoal = FirestoreMonthlyGoal(
        id = YearMonth.now().toString(),
        yearMonth = String.format("%04d-%02d", YearMonth.now().year, YearMonth.now().monthValue),
        goalTips = 2000.0,
        commissionPercent = 0.01
    )
    
    val testSettings = FirestoreUserSettings(
        commissionPercent = 0.015,
        reminderEnabled = true,
        reminderHour = 20,
        reminderMinute = 30
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "Firebase Firestore Test",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Status card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (statusMessage.contains("Error") || statusMessage.contains("Failed"))
                    MaterialTheme.colorScheme.errorContainer
                else if (statusMessage.contains("Success"))
                    MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Text(
                text = statusMessage,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        
        if (isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Test buttons
        Text(
            "Daily Entry Operations",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Button(
            onClick = {
                isLoading = true
                scope.launch {
                    val result = repository.upsertEntry(testEntry)
                    statusMessage = if (result.isSuccess) {
                        "✅ Success: Entry created for ${testEntry.date}"
                    } else {
                        "❌ Error: ${result.exceptionOrNull()?.message}"
                    }
                    isLoading = false
                }
            },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        ) {
            Text("Create Test Entry")
        }
        
        Button(
            onClick = {
                isLoading = true
                scope.launch {
                    repository.getEntryByDate(LocalDate.now()).collect { entry ->
                        statusMessage = if (entry != null) {
                            "✅ Success: Found entry - Turnover: R$${entry.turnover}, Tips: R$${(entry.tipsCash ?: 0.0) + (entry.tipsCard ?: 0.0)}"
                        } else {
                            "ℹ️ No entry found for today"
                        }
                        isLoading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        ) {
            Text("Read Today's Entry")
        }
        
        Button(
            onClick = {
                isLoading = true
                scope.launch {
                    val start = YearMonth.now().atDay(1)
                    val end = YearMonth.now().atEndOfMonth()
                    repository.getEntriesBetween(start, end).collect { entries ->
                        statusMessage = "✅ Success: Found ${entries.size} entries this month"
                        isLoading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Text("Read This Month's Entries")
        }
        
        Divider(modifier = Modifier.padding(vertical = 8.dp))
        
        Text(
            "Job Operations",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        var createdJobId by remember { mutableStateOf<String?>(null) }
        
        Button(
            onClick = {
                isLoading = true
                scope.launch {
                    val result = repository.upsertJob(testJob)
                    if (result.isSuccess) {
                        createdJobId = result.getOrNull()
                        statusMessage = "✅ Success: Job created with ID: $createdJobId"
                    } else {
                        statusMessage = "❌ Error: ${result.exceptionOrNull()?.message}"
                    }
                    isLoading = false
                }
            },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        ) {
            Text("Create Test Job")
        }
        
        Button(
            onClick = {
                isLoading = true
                scope.launch {
                    repository.getAllJobs().collect { jobs ->
                        statusMessage = "✅ Success: Found ${jobs.size} jobs - ${jobs.joinToString { it.name }}"
                        isLoading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Text("Read All Jobs")
        }
        
        Divider(modifier = Modifier.padding(vertical = 8.dp))
        
        Text(
            "Monthly Goal Operations",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Button(
            onClick = {
                isLoading = true
                scope.launch {
                    val result = repository.upsertGoal(testGoal)
                    statusMessage = if (result.isSuccess) {
                        "✅ Success: Goal set for ${testGoal.yearMonth} - R$${testGoal.goalTips}"
                    } else {
                        "❌ Error: ${result.exceptionOrNull()?.message}"
                    }
                    isLoading = false
                }
            },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        ) {
            Text("Create Test Goal")
        }
        
        Button(
            onClick = {
                isLoading = true
                scope.launch {
                    repository.getGoalForMonth(YearMonth.now()).collect { goal ->
                        statusMessage = if (goal != null) {
                            "✅ Success: Goal found - R$${goal.goalTips} (${(goal.commissionPercent * 100)}% commission)"
                        } else {
                            "ℹ️ No goal set for this month"
                        }
                        isLoading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Text("Read This Month's Goal")
        }
        
        Divider(modifier = Modifier.padding(vertical = 8.dp))
        
        Text(
            "Settings Operations",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Button(
            onClick = {
                isLoading = true
                scope.launch {
                    val result = repository.saveSettings(testSettings)
                    statusMessage = if (result.isSuccess) {
                        "✅ Success: Settings saved"
                    } else {
                        "❌ Error: ${result.exceptionOrNull()?.message}"
                    }
                    isLoading = false
                }
            },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        ) {
            Text("Save Test Settings")
        }
        
        Button(
            onClick = {
                isLoading = true
                scope.launch {
                    repository.getSettings().collect { settings ->
                        statusMessage = if (settings != null) {
                            "✅ Success: Settings - ${(settings.commissionPercent * 100)}% commission, Reminder: ${if (settings.reminderEnabled) "Enabled" else "Disabled"}"
                        } else {
                            "ℹ️ No settings found"
                        }
                        isLoading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Text("Read Settings")
        }
        
        Divider(modifier = Modifier.padding(vertical = 16.dp))
        
        Text(
            "⚠️ Note: This is a test screen for Firebase operations.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            "All operations are performed on your real Firebase database.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            "Check Firebase Console to verify data.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )
    }
}
