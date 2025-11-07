package com.example.waiterwallet.ui.screens

import android.graphics.Color
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.waiterwallet.ui.viewmodel.JobsViewModel
import com.example.waiterwallet.ui.viewmodel.OverviewViewModel
import com.example.waiterwallet.utils.CsvExporter
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun DashboardScreen(
    onAddEntry: () -> Unit,
    vm: OverviewViewModel = viewModel(factory = OverviewViewModel.Factory),
    jobsVm: JobsViewModel = viewModel(factory = JobsViewModel.Factory)
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val today = LocalDate.now()
    val currentMonth = YearMonth.from(today)
    
    val totalTipsNullable by vm.totalTipsForMonth(today).collectAsState(initial = 0.0)
    val totalTips = totalTipsNullable ?: 0.0
    val totalTurnover by vm.totalTurnoverForMonth(today).collectAsState(initial = 0.0)
    val commissionPercent by vm.commissionPercent.collectAsState(initial = 0.01)
    val estimatedCommission = vm.estimateCommission(totalTurnover, commissionPercent)
    val goal by vm.goalForMonth(today).collectAsState(initial = null)
    val goalAmount = goal?.goalTips ?: 0.0
    val progress = if (goalAmount > 0) (totalTips / goalAmount).coerceIn(0.0, 1.0) else 0.0
    
    // Get last 7 days of entries for chart
    val sevenDaysAgo = today.minusDays(6)
    val weekEntries by vm.entriesBetween(sevenDaysAgo, today).collectAsState(initial = emptyList())
    
    // Get month entries for export
    val (monthStart, monthEnd) = com.example.waiterwallet.data.DailyEntryRepository.monthRange(today)
    val monthEntries by vm.entriesBetween(monthStart, monthEnd).collectAsState(initial = emptyList())
    val jobs by jobsVm.allJobs.collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Text(
            "Dashboard",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Monthly Summary Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "Monthly Summary",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Tips
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Total Tips",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        "R${"%.2f".format(totalTips)}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Turnover
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Total Turnover",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        "R${"%.2f".format(totalTurnover)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                // Commission
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Est. Commission (${(commissionPercent*100).toInt()}%)",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        "R${"%.2f".format(estimatedCommission)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        // Goal Progress Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Monthly Goal",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        "R${"%.2f".format(goalAmount)}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                
                LinearProgressIndicator(
                    progress = { progress.toFloat() },
                    modifier = Modifier.fillMaxWidth().height(12.dp),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
                
                Text(
                    "${(progress * 100).toInt()}% Complete",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        // Weekly Tips Chart Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Last 7 Days Tips",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                WeeklyTipsChart(entries = weekEntries, startDate = sevenDaysAgo, endDate = today)
            }
        }
        
        Spacer(Modifier.height(20.dp))
        
        // Action Buttons
        Button(
            onClick = onAddEntry,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
            Text("Add Entry", style = MaterialTheme.typography.titleMedium)
        }
        
        Spacer(Modifier.height(12.dp))
        
        FilledTonalButton(
            onClick = {
                scope.launch {
                    val file = CsvExporter.exportToCSV(context, monthEntries, jobs, currentMonth.toString())
                    if (file != null) {
                        CsvExporter.shareCSV(context, file)
                    } else {
                        Toast.makeText(context, "Export failed", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
            Text("Export Month to CSV", style = MaterialTheme.typography.titleMedium)
        }
        
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
fun WeeklyTipsChart(entries: List<com.example.waiterwallet.data.DailyEntry>, startDate: LocalDate, endDate: LocalDate) {
    AndroidView(
        factory = { context ->
            BarChart(context).apply {
                description.isEnabled = false
                setDrawGridBackground(false)
                setTouchEnabled(false)
                legend.isEnabled = false
                axisRight.isEnabled = false
                axisLeft.axisMinimum = 0f
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.setDrawGridLines(false)
                xAxis.granularity = 1f
            }
        },
        update = { chart ->
            val dateRange = generateSequence(startDate) { it.plusDays(1) }.takeWhile { it <= endDate }.toList()
            val barEntries = dateRange.mapIndexed { index, date ->
                val tips = entries.find { it.date == date }?.totalTips?.toFloat() ?: 0f
                BarEntry(index.toFloat(), tips)
            }
            
            val dataSet = BarDataSet(barEntries, "Tips").apply {
                color = Color.rgb(0, 150, 136) // Teal
                valueTextColor = Color.BLACK
                valueTextSize = 10f
            }
            
            val barData = BarData(dataSet)
            chart.data = barData
            
            // Format X-axis with dates
            chart.xAxis.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val index = value.toInt()
                    return if (index in dateRange.indices) {
                        dateRange[index].format(DateTimeFormatter.ofPattern("MM/dd"))
                    } else ""
                }
            }
            
            chart.invalidate() // Refresh
        },
        modifier = Modifier.fillMaxWidth().height(200.dp)
    )
}
