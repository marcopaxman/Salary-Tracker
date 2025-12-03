package com.example.waiterwallet.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.waiterwallet.data.DailyEntryRepository
import com.example.waiterwallet.ui.viewmodel.JobsViewModel
import com.example.waiterwallet.ui.viewmodel.OverviewViewModel
import com.example.waiterwallet.utils.PdfExporter
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun EntriesScreen(
    onAddEntry: () -> Unit,
    onExportSuccess: (String) -> Unit = {},
    vm: OverviewViewModel = viewModel(factory = OverviewViewModel.Factory),
    jobsVm: JobsViewModel = viewModel(factory = JobsViewModel.Factory)
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val today = LocalDate.now()
    val currentMonth = YearMonth.from(today)
    
    // Get jobs for export
    val jobs by jobsVm.allJobs.collectAsState(initial = emptyList())
    
    // Get month entries for export
    val (monthStart, monthEnd) = DailyEntryRepository.monthRange(today)
    val monthEntries by vm.entriesBetween(monthStart, monthEnd).collectAsState(initial = emptyList())
    
    // Get hourly rate for PDF export
    val hourlyRate by vm.hourlyRate.collectAsState(initial = 0.0)
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Text(
            "Entries",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            "Manage your daily entries",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // Add Entry Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "New Entry",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    "Record your daily turnover, tips, and hours worked.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Button(
                    onClick = onAddEntry,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                    Text("Add Entry", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        // Export Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "Export Data",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    "Export your ${currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"))} entries as a PDF report.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    "${monthEntries.size} entries this month",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                FilledTonalButton(
                    onClick = {
                        scope.launch {
                            val file = PdfExporter.exportToPDF(
                                context = context,
                                entries = monthEntries,
                                jobs = jobs,
                                month = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                                hourlyRate = hourlyRate
                            )
                            if (file != null) {
                                PdfExporter.sharePDF(context, file)
                                onExportSuccess("PDF report exported successfully!")
                            } else {
                                Toast.makeText(context, "Export failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                    Text("Export Month to PDF", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
        
        Spacer(Modifier.height(16.dp))
    }
}
