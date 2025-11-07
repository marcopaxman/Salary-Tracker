package com.example.waiterwallet.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.waiterwallet.ui.viewmodel.DailyEntryViewModel

@Composable
fun DataEntryScreen(onSaved: () -> Unit, vm: DailyEntryViewModel = viewModel(factory = DailyEntryViewModel.Factory)) {
    var turnover by remember { mutableStateOf("") }
    var tipsCash by remember { mutableStateOf("") }
    var tipsCard by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Add Entry")
        Spacer(Modifier.height(8.dp))
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
                turnover = turnover.toDoubleOrNull() ?: 0.0,
                tipsCash = tipsCash.toDoubleOrNull(),
                tipsCard = tipsCard.toDoubleOrNull(),
                notes = notes.ifBlank { null }
            )
            onSaved()
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Save")
        }
    }
}
