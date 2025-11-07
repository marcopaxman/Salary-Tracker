package com.example.waiterwallet.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.waiterwallet.WaiterWalletApp
import com.example.waiterwallet.data.DailyEntry
import com.example.waiterwallet.data.DailyEntryRepository
import kotlinx.coroutines.launch
import java.time.LocalDate

class DailyEntryViewModel(private val repo: DailyEntryRepository, application: Application) : AndroidViewModel(application) {
    fun save(
        turnover: Double,
        tipsCash: Double?,
        tipsCard: Double?,
        notes: String?
    ) {
        viewModelScope.launch {
            val entry = DailyEntry(
                date = LocalDate.now(),
                turnover = turnover,
                tipsCash = tipsCash,
                tipsCard = tipsCard,
                notes = notes
            )
            repo.upsert(entry)
        }
    }

    object Factory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val app = WaiterWalletAppHolder.app
            val repo = DailyEntryRepository(app.database.dailyEntryDao())
            @Suppress("UNCHECKED_CAST")
            return DailyEntryViewModel(repo, app) as T
        }
    }
}

// Helper to access Application instance for simple factory creation
object WaiterWalletAppHolder {
    lateinit var app: WaiterWalletApp
}
