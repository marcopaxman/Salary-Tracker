package com.example.waiterwallet.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.waiterwallet.data.DailyEntryRepository
import com.example.waiterwallet.data.MonthlyGoal
import com.example.waiterwallet.data.MonthlyGoalDao
import com.example.waiterwallet.data.SettingsStore
import java.time.LocalDate
import java.time.YearMonth

class OverviewViewModel(
    private val repo: DailyEntryRepository,
    private val goalDao: MonthlyGoalDao,
    private val settings: SettingsStore
) : ViewModel() {
    fun totalTipsForMonth(date: LocalDate, jobId: Long? = null) = 
        if (jobId == null) repo.totalTipsForMonth(date) 
        else repo.totalTipsForMonthByJob(date, jobId)
    
    fun totalTurnoverForMonth(date: LocalDate, jobId: Long? = null) = 
        if (jobId == null) repo.totalTurnoverForMonth(date) 
        else repo.totalTurnoverForMonthByJob(date, jobId)
    
    fun estimateCommission(turnover: Double?, percent: Double) = (turnover ?: 0.0) * percent
    fun goalForMonth(date: LocalDate) = goalDao.goalForMonth(MonthlyGoal.key(YearMonth.from(date)))
    
    fun entriesBetween(start: LocalDate, end: LocalDate, jobId: Long? = null) = 
        if (jobId == null) repo.entriesBetween(start, end) 
        else repo.entriesBetweenForJob(start, end, jobId)
    
    val commissionPercent = settings.commissionPercent

    object Factory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val app = WaiterWalletAppHolder.app
            val db = app.database
            @Suppress("UNCHECKED_CAST")
            return OverviewViewModel(
                repo = DailyEntryRepository(db.dailyEntryDao()),
                goalDao = db.goalDao(),
                settings = SettingsStore(app)
            ) as T
        }
    }
}
