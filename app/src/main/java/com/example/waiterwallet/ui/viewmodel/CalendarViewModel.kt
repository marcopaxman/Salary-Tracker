package com.example.waiterwallet.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.waiterwallet.data.DailyEntryRepository
import com.example.waiterwallet.data.UnifiedRepository
import com.example.waiterwallet.data.UnifiedRepositoryFactory
import java.time.LocalDate

class CalendarViewModel(private val unifiedRepo: UnifiedRepository) : ViewModel() {
    fun entriesForMonth(date: LocalDate): kotlinx.coroutines.flow.Flow<List<com.example.waiterwallet.data.DailyEntry>> {
        val (start, end) = DailyEntryRepository.monthRange(date)
        return unifiedRepo.entriesBetween(start, end)
    }

    object Factory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val app = WaiterWalletAppHolder.app
            val unifiedRepo = UnifiedRepositoryFactory.getInstance(app)
            @Suppress("UNCHECKED_CAST")
            return CalendarViewModel(unifiedRepo) as T
        }
    }
}
