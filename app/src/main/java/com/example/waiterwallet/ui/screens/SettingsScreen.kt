package com.example.waiterwallet.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import com.example.waiterwallet.ui.viewmodel.SettingsViewModel
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun SettingsScreen(onSaved: () -> Unit, vm: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)) {
    val commission by vm.commissionPercent.collectAsState(initial = 0.01)
    val reminderEnabled by vm.reminderEnabled.collectAsState(initial = true)
    val reminderTime by vm.reminderTime.collectAsState(initial = LocalTime.of(22, 0))
    val today = LocalDate.now()
    val currentGoal by vm.goalForMonth(today).collectAsState(initial = null)

    var commissionText by remember(commission) { mutableStateOf((commission * 100).toString()) }
    var hourText by remember(reminderTime) { mutableStateOf(reminderTime.hour.toString()) }
    var minuteText by remember(reminderTime) { mutableStateOf(reminderTime.minute.toString()) }
    var reminders by remember(reminderEnabled) { mutableStateOf(reminderEnabled) }
    var goalText by remember(currentGoal) { mutableStateOf(currentGoal?.goalTips?.toString() ?: "") }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Settings & Goals", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        
        // Commission Section
        Text("Commission Settings", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = commissionText,
            onValueChange = { commissionText = it },
            label = { Text("Commission % (e.g., 1)") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(Modifier.height(16.dp))
        
        // Monthly Goal Section
        Text("Monthly Goal", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = goalText,
            onValueChange = { goalText = it },
            label = { Text("Goal Tips Amount (R$)") },
            modifier = Modifier.fillMaxWidth(),
            supportingText = { Text("Set your tips goal for ${today.month.name}") }
        )
        
        Spacer(Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(Modifier.height(16.dp))
        
        // Reminder Section
        Text("Daily Reminder", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Enable Reminders", modifier = Modifier.weight(1f))
            Switch(checked = reminders, onCheckedChange = { reminders = it })
        }
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = hourText,
                onValueChange = { hourText = it },
                label = { Text("Hour (0-23)") },
                modifier = Modifier.weight(1f).padding(end = 4.dp)
            )
            OutlinedTextField(
                value = minuteText,
                onValueChange = { minuteText = it },
                label = { Text("Minute (0-59)") },
                modifier = Modifier.weight(1f).padding(start = 4.dp)
            )
        }
        
        Spacer(Modifier.height(24.dp))
        Button(onClick = {
            val pct = (commissionText.toDoubleOrNull() ?: 1.0) / 100.0
            val hour = hourText.toIntOrNull()?.coerceIn(0, 23) ?: 22
            val minute = minuteText.toIntOrNull()?.coerceIn(0, 59) ?: 0
            val goalAmount = goalText.toDoubleOrNull() ?: 0.0
            
            vm.saveSettings(pct, reminders, LocalTime.of(hour, minute))
            if (goalAmount > 0) {
                vm.saveMonthlyGoal(today, goalAmount, pct)
            }
            onSaved()
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Save All Settings")
        }
    }
}
