package com.example.waiterwallet.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.waiterwallet.WaiterWalletApp
import com.example.waiterwallet.data.SettingsStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.time.LocalTime

class SettingsViewModel(private val store: SettingsStore, app: Application) : AndroidViewModel(app) {
    val commissionPercent: Flow<Double> = store.commissionPercent
    val reminderEnabled: Flow<Boolean> = store.reminderEnabled
    val reminderTime: Flow<LocalTime> = store.reminderTime

    fun saveSettings(pct: Double, enabled: Boolean, time: LocalTime) {
        viewModelScope.launch {
            store.setCommissionPercent(pct)
            store.setReminder(enabled, time)
            // Scheduling of reminders is handled by ReminderScheduler (added later)
        }
    }

    object Factory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val app = WaiterWalletAppHolder.app
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(SettingsStore(app), app) as T
        }
    }
}
