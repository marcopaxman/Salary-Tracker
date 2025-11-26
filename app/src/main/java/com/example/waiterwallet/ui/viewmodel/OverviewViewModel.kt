package com.example.waiterwallet.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.waiterwallet.data.DailyEntryRepository
import com.example.waiterwallet.data.MonthlyGoal
import com.example.waiterwallet.data.MonthlyGoalDao
import com.example.waiterwallet.data.SettingsStore
import com.example.waiterwallet.data.UnifiedRepository
import com.example.waiterwallet.data.UnifiedRepositoryFactory
import java.time.LocalDate
import java.time.YearMonth

class OverviewViewModel(
    private val unifiedRepo: UnifiedRepository,
    private val goalDao: MonthlyGoalDao,
    private val settings: SettingsStore
) : ViewModel() {
    fun totalTipsForMonth(date: LocalDate, jobId: Long? = null) = 
        if (jobId == null) unifiedRepo.totalTipsForMonth(date) 
        else unifiedRepo.totalTipsForMonthByJob(date, jobId)
    
    fun totalTurnoverForMonth(date: LocalDate, jobId: Long? = null) = 
        if (jobId == null) unifiedRepo.totalTurnoverForMonth(date)
        else unifiedRepo.totalTurnoverForMonthByJob(date, jobId)
    
    fun totalHoursWorkedForMonth(date: LocalDate, jobId: Long? = null) =
        if (jobId == null) unifiedRepo.totalHoursWorkedForMonth(date)
        else unifiedRepo.totalHoursWorkedForMonthByJob(date, jobId)
    
    fun estimateCommission(turnover: Double?, percent: Double) = (turnover ?: 0.0) * percent
    fun goalForMonth(date: LocalDate) = goalDao.goalForMonth(MonthlyGoal.key(YearMonth.from(date)))
    
    fun entriesBetween(start: LocalDate, end: LocalDate, jobId: Long? = null) = 
        if (jobId == null) unifiedRepo.entriesBetween(start, end)
        else unifiedRepo.entriesBetweenForJob(start, end, jobId)
    
    val commissionPercent = settings.commissionPercent
    val hourlyRate = settings.hourlyRate

    object Factory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val app = WaiterWalletAppHolder.app
            val db = app.database
            val unifiedRepo = UnifiedRepositoryFactory.getInstance(app)
            @Suppress("UNCHECKED_CAST")
            return OverviewViewModel(
                unifiedRepo = unifiedRepo,
                goalDao = db.goalDao(),
                settings = SettingsStore(app)
            ) as T
        }
    }
}
