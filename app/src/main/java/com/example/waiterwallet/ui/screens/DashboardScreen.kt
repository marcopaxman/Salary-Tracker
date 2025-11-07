package com.example.waiterwallet.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.waiterwallet.ui.viewmodel.OverviewViewModel
import java.time.LocalDate

@Composable
fun DashboardScreen(
    onAddEntry: () -> Unit,
    onOpenSettings: () -> Unit,
    vm: OverviewViewModel = viewModel(factory = OverviewViewModel.Factory)
) {
    val today = LocalDate.now()
    val totalTipsNullable by vm.totalTipsForMonth(today).collectAsState(initial = 0.0)
    val totalTips = totalTipsNullable ?: 0.0
    val totalTurnover by vm.totalTurnoverForMonth(today).collectAsState(initial = 0.0)
    val commissionPercent by vm.commissionPercent.collectAsState(initial = 0.01)
    val estimatedCommission = vm.estimateCommission(totalTurnover, commissionPercent)
    val goal by vm.goalForMonth(today).collectAsState(initial = null)
    val goalAmount = goal?.goalTips ?: 0.0
    val progress = if (goalAmount > 0) (totalTips / goalAmount).coerceIn(0.0, 1.0) else 0.0

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Overview", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        Text("Month Tips: R${"%.2f".format(totalTips)}")
        Text("Month Turnover: R${"%.2f".format(totalTurnover)}")
        Text("Estimated Commission (${(commissionPercent*100).toInt()}%): R${"%.2f".format(estimatedCommission)}")
        Spacer(Modifier.height(8.dp))
        Text("Goal: R${"%.2f".format(goalAmount)}")
        LinearProgressIndicator(progress = progress.toFloat(), modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))
        Button(onClick = onAddEntry, modifier = Modifier.fillMaxWidth()) {
            Text("Add Today's Entry")
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = onOpenSettings, modifier = Modifier.fillMaxWidth()) {
            Text("Settings & Goals")
        }
        Spacer(Modifier.height(24.dp))
        Text("Charts coming soon…", style = MaterialTheme.typography.bodyMedium)
    }
}
