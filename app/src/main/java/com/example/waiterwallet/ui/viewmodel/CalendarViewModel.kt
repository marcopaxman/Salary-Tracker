package com.example.waiterwallet.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.waiterwallet.data.DailyEntryRepository
import java.time.LocalDate

class CalendarViewModel(private val repo: DailyEntryRepository) : ViewModel() {
    fun entriesForMonth(date: LocalDate): kotlinx.coroutines.flow.Flow<List<com.example.waiterwallet.data.DailyEntry>> {
        val (start, end) = DailyEntryRepository.monthRange(date)
        return repo.entriesBetween(start, end)
    }

    object Factory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val app = WaiterWalletAppHolder.app
            @Suppress("UNCHECKED_CAST")
            return CalendarViewModel(DailyEntryRepository(app.database.dailyEntryDao())) as T
        }
    }
}
