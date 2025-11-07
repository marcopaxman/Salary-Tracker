package com.example.waiterwallet.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.waiterwallet.ui.viewmodel.SettingsViewModel
import java.time.LocalTime

@Composable
fun SettingsScreen(onSaved: () -> Unit, vm: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)) {
    val commission by vm.commissionPercent.collectAsState(initial = 0.01)
    val reminderEnabled by vm.reminderEnabled.collectAsState(initial = true)
    val reminderTime by vm.reminderTime.collectAsState(initial = LocalTime.of(22, 0))

    var commissionText by remember { mutableStateOf((commission * 100).toString()) }
    var hourText by remember { mutableStateOf(reminderTime.hour.toString()) }
    var minuteText by remember { mutableStateOf(reminderTime.minute.toString()) }
    var reminders by remember { mutableStateOf(reminderEnabled) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Settings")
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = commissionText,
            onValueChange = { commissionText = it },
            label = { Text("Commission % (e.g., 1)" )},
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        Text("Daily Reminder")
        Spacer(Modifier.height(8.dp))
        Switch(checked = reminders, onCheckedChange = { reminders = it })
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = hourText, onValueChange = { hourText = it }, label = { Text("Hour (0-23)") })
        OutlinedTextField(value = minuteText, onValueChange = { minuteText = it }, label = { Text("Minute (0-59)") })
        Spacer(Modifier.height(16.dp))
        Button(onClick = {
            val pct = (commissionText.toDoubleOrNull() ?: 1.0) / 100.0
            val hour = hourText.toIntOrNull()?.coerceIn(0,23) ?: 22
            val minute = minuteText.toIntOrNull()?.coerceIn(0,59) ?: 0
            vm.saveSettings(pct, reminders, LocalTime.of(hour, minute))
            onSaved()
        }, modifier = Modifier.fillMaxWidth()) { Text("Save") }
    }
}
