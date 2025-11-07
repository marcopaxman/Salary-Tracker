package com.example.waiterwallet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.waiterwallet.data.DailyEntry
import com.example.waiterwallet.ui.viewmodel.CalendarViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarScreen(
    onNavigateBack: () -> Unit,
    vm: CalendarViewModel = viewModel(factory = CalendarViewModel.Factory)
) {
    val today = LocalDate.now()
    val currentMonth = YearMonth.from(today)
    val entries by vm.entriesForMonth(today).collectAsState(initial = emptyList())
    
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    val selectedEntry = selectedDate?.let { date -> entries.find { it.date == date } }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Calendar - ${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentMonth.year}",
            style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Button(onClick = onNavigateBack) { Text("Back to Dashboard") }
        Spacer(Modifier.height(16.dp))

        // Weekday headers
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                Text(day, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
            }
        }
        Spacer(Modifier.height(8.dp))

        // Month grid
        val daysInMonth = generateCalendarDays(currentMonth)
        LazyVerticalGrid(columns = GridCells.Fixed(7), modifier = Modifier.fillMaxWidth()) {
            items(daysInMonth) { dateOrNull ->
                if (dateOrNull != null) {
                    val hasEntry = entries.any { it.date == dateOrNull }
                    DayCell(date = dateOrNull, hasEntry = hasEntry, isToday = dateOrNull == today, onClick = { selectedDate = dateOrNull })
                } else {
                    Box(modifier = Modifier.aspectRatio(1f))
                }
            }
        }

        // Detail dialog
        if (selectedEntry != null) {
            EntryDetailDialog(entry = selectedEntry!!, onDismiss = { selectedDate = null })
        } else if (selectedDate != null) {
            EmptyEntryDialog(date = selectedDate!!, onDismiss = { selectedDate = null })
        }
    }
}

@Composable
fun DayCell(date: LocalDate, hasEntry: Boolean, isToday: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(if (isToday) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
            .border(1.dp, if (hasEntry) MaterialTheme.colorScheme.primary else Color.Gray, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(date.dayOfMonth.toString(), style = MaterialTheme.typography.bodyMedium)
        if (hasEntry) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
fun EntryDetailDialog(entry: DailyEntry, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Entry for ${entry.date}") },
        text = {
            Column {
                Text("Turnover: R${"%.2f".format(entry.turnover)}")
                Text("Tips Cash: R${"%.2f".format(entry.tipsCash ?: 0.0)}")
                Text("Tips Card: R${"%.2f".format(entry.tipsCard ?: 0.0)}")
                Text("Total Tips: R${"%.2f".format(entry.totalTips)}")
                if (!entry.notes.isNullOrBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text("Notes: ${entry.notes}", style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
fun EmptyEntryDialog(date: LocalDate, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("No Entry") },
        text = { Text("No entry recorded for $date") },
        confirmButton = { TextButton(onClick = onDismiss) { Text("OK") } }
    )
}

// Helper to generate calendar grid with leading/trailing empty cells
fun generateCalendarDays(yearMonth: YearMonth): List<LocalDate?> {
    val firstDay = yearMonth.atDay(1)
    val lastDay = yearMonth.atEndOfMonth()
    val leadingEmptyDays = firstDay.dayOfWeek.value % 7  // Sunday = 0
    val totalDays = lastDay.dayOfMonth
    
    val days = mutableListOf<LocalDate?>()
    repeat(leadingEmptyDays) { days.add(null) }
    for (day in 1..totalDays) {
        days.add(yearMonth.atDay(day))
    }
    return days
}
