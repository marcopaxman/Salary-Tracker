package com.example.waiterwallet.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.waiterwallet.WaiterWalletApp
import com.example.waiterwallet.data.MonthlyGoal
import com.example.waiterwallet.data.MonthlyGoalDao
import com.example.waiterwallet.data.SettingsStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth

class SettingsViewModel(
    private val store: SettingsStore,
    private val goalDao: MonthlyGoalDao,
    app: Application
) : AndroidViewModel(app) {
    val commissionPercent: Flow<Double> = store.commissionPercent
    val reminderEnabled: Flow<Boolean> = store.reminderEnabled
    val reminderTime: Flow<LocalTime> = store.reminderTime

    fun goalForMonth(date: LocalDate) = goalDao.goalForMonth(MonthlyGoal.key(YearMonth.from(date)))

    fun saveSettings(pct: Double, enabled: Boolean, time: LocalTime) {
        viewModelScope.launch {
            store.setCommissionPercent(pct)
            store.setReminder(enabled, time)
            // Schedule/cancel reminder based on settings
            com.example.waiterwallet.utils.ReminderScheduler.scheduleReminder(
                getApplication(),
                enabled,
                time
            )
        }
    }

    fun saveMonthlyGoal(date: LocalDate, goalTipsAmount: Double, commissionPercent: Double) {
        viewModelScope.launch {
            val ym = YearMonth.from(date)
            val key = MonthlyGoal.key(ym)
            goalDao.upsert(MonthlyGoal(yearMonth = key, goalTips = goalTipsAmount, commissionPercent = commissionPercent))
        }
    }

    object Factory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val app = WaiterWalletAppHolder.app
            val db = app.database
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(SettingsStore(app), db.goalDao(), app) as T
        }
    }
}
