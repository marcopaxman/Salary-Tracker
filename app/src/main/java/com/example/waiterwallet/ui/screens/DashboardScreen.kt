package com.example.waiterwallet.ui.screens

import android.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.waiterwallet.ui.viewmodel.JobsViewModel
import com.example.waiterwallet.ui.viewmodel.OverviewViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    vm: OverviewViewModel = viewModel(factory = OverviewViewModel.Factory),
    jobsVm: JobsViewModel = viewModel(factory = JobsViewModel.Factory)
) {
    val today = LocalDate.now()
    val currentMonth = YearMonth.from(today)
    
    // Job filter state
    val jobs by jobsVm.allJobs.collectAsState(initial = emptyList())
    var selectedJobId by remember { mutableStateOf<Long?>(null) }
    var jobDropdownExpanded by remember { mutableStateOf(false) }
    
    val totalTipsNullable by vm.totalTipsForMonth(today, selectedJobId).collectAsState(initial = 0.0)
    val totalTips = totalTipsNullable ?: 0.0
    val totalTurnover by vm.totalTurnoverForMonth(today, selectedJobId).collectAsState(initial = 0.0)
    val totalHoursWorked by vm.totalHoursWorkedForMonth(today, selectedJobId).collectAsState(initial = 0.0)
    val commissionPercent by vm.commissionPercent.collectAsState(initial = 0.01)
    val hourlyRate by vm.hourlyRate.collectAsState(initial = 0.0)
    val estimatedCommission = vm.estimateCommission(totalTurnover, commissionPercent)
    val totalHourlyWages = (totalHoursWorked ?: 0.0) * hourlyRate
    val goal by vm.goalForMonth(today).collectAsState(initial = null)
    val goalAmount = goal?.goalTips ?: 0.0
    val progress = if (goalAmount > 0) (totalTips / goalAmount).coerceIn(0.0, 1.0) else 0.0
    
    // Get last 7 days of entries for chart
    val sevenDaysAgo = today.minusDays(6)
    val weekEntries by vm.entriesBetween(sevenDaysAgo, today, selectedJobId).collectAsState(initial = emptyList())
    
    // Get monthly earnings history for line chart
    val monthlyEarnings by vm.getMonthlyEarningsHistory(12).collectAsState(initial = emptyList())

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
        
        // Job Filter
        if (jobs.isNotEmpty()) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Filter by Job",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            Icons.Default.Menu,
                            contentDescription = "Filter",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    ExposedDropdownMenuBox(
                        expanded = jobDropdownExpanded,
                        onExpandedChange = { jobDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = jobs.find { it.id == selectedJobId }?.name ?: "All Jobs",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Select Job") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = jobDropdownExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                        )
                        ExposedDropdownMenu(
                            expanded = jobDropdownExpanded,
                            onDismissRequest = { jobDropdownExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("All Jobs") },
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
                }
            }
        }
        
        // Income Summary Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Income",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        currentMonth.format(DateTimeFormatter.ofPattern("MMM yyyy")),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                Spacer(Modifier.height(16.dp))
                
                // Total Income (New Addition)
                val totalIncome = totalTips + estimatedCommission + totalHourlyWages
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Total Made",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        "R${"%.2f".format(totalIncome)}",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                }
                
                // Divider or Spacer
                androidx.compose.material3.HorizontalDivider(
                    modifier = Modifier.padding(bottom = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                
                // Total Tips
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "Tips",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp).padding(end = 8.dp)
                        )
                        Text(
                            "Tips",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Text(
                        "R${"%.2f".format(totalTips)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                // Commission
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Commission",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp).padding(end = 8.dp)
                        )
                        Text(
                            "Commission (${(commissionPercent * 100).toInt()}%)",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Text(
                        "R${"%.2f".format(estimatedCommission)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                // Hourly Wages
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Hourly Wages",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            "R${"%.2f".format(totalHourlyWages)}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
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
        
        Spacer(Modifier.height(16.dp))
        
        // Monthly Earnings Chart Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Monthly Earnings Trend",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                if (monthlyEarnings.isNotEmpty()) {
                    MonthlyEarningsLineChart(monthlyEarnings = monthlyEarnings)
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No data available",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
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

@Composable
fun MonthlyEarningsLineChart(monthlyEarnings: List<com.example.waiterwallet.data.MonthlyEarnings>) {
    AndroidView(
        factory = { context ->
            com.github.mikephil.charting.charts.LineChart(context).apply {
                description.isEnabled = false
                setDrawGridBackground(false)
                setTouchEnabled(true)
                isDragEnabled = true
                setScaleEnabled(false)
                setPinchZoom(false)
                legend.isEnabled = false
                axisRight.isEnabled = false
                axisLeft.axisMinimum = 0f
                axisLeft.textColor = Color.DKGRAY
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.setDrawGridLines(false)
                xAxis.textColor = Color.DKGRAY
                xAxis.granularity = 1f
            }
        },
        update = { chart ->
            if (monthlyEarnings.isEmpty()) {
                chart.clear()
                chart.invalidate()
                return@AndroidView
            }
            
            val lineEntries = monthlyEarnings.mapIndexed { index, earning ->
                com.github.mikephil.charting.data.Entry(index.toFloat(), earning.total.toFloat())
            }
            
            val dataSet = com.github.mikephil.charting.data.LineDataSet(lineEntries, "Total Earnings").apply {
                color = Color.rgb(33, 150, 243) // Blue
                lineWidth = 3f
                setCircleColor(Color.rgb(33, 150, 243))
                circleRadius = 5f
                setDrawCircleHole(false)
                valueTextColor = Color.BLACK
                valueTextSize = 10f
                mode = com.github.mikephil.charting.data.LineDataSet.Mode.CUBIC_BEZIER
                setDrawFilled(true)
                fillColor = Color.rgb(33, 150, 243)
                fillAlpha = 50
            }
            
            val lineData = com.github.mikephil.charting.data.LineData(dataSet)
            chart.data = lineData
            
            // Format X-axis with month names
            chart.xAxis.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val index = value.toInt()
                    return if (index in monthlyEarnings.indices) {
                        monthlyEarnings[index].yearMonth.format(
                            java.time.format.DateTimeFormatter.ofPattern("MMM yy")
                        )
                    } else ""
                }
            }
            
            chart.invalidate() // Refresh
        },
        modifier = Modifier.fillMaxWidth().height(250.dp)
    )
}
