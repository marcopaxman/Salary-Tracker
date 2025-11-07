package com.example.waiterwallet.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.waiterwallet.WaiterWalletApp
import com.example.waiterwallet.data.DailyEntry
import com.example.waiterwallet.data.DailyEntryRepository
import com.example.waiterwallet.data.MonthlyGoal
import com.example.waiterwallet.data.MonthlyGoalDao
import com.example.waiterwallet.utils.GoalNotificationHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

class DailyEntryViewModel(
    private val repo: DailyEntryRepository,
    private val goalDao: MonthlyGoalDao,
    application: Application
) : AndroidViewModel(application) {
    
    fun save(
        date: LocalDate,
        turnover: Double,
        tipsCash: Double?,
        tipsCard: Double?,
        notes: String?,
        jobId: Long? = null
    ) {
        viewModelScope.launch {
            val entry = DailyEntry(
                date = date,
                turnover = turnover,
                tipsCash = tipsCash,
                tipsCard = tipsCard,
                notes = notes,
                jobId = jobId
            )
            repo.upsert(entry)
            
            // Check if goal is reached after saving
            checkGoalProgress()
        }
    }
    
    private suspend fun checkGoalProgress() {
        val today = LocalDate.now()
        val goalKey = MonthlyGoal.key(YearMonth.from(today))
        val goal = goalDao.goalForMonth(goalKey).first()
        
        if (goal != null && goal.goalTips > 0) {
            val totalTips = repo.totalTipsForMonth(today).first() ?: 0.0
            val percentage = ((totalTips / goal.goalTips) * 100).toInt()
            
            // Notify on 100% achievement
            if (totalTips >= goal.goalTips) {
                GoalNotificationHelper.showGoalAchievedNotification(
                    getApplication(),
                    goal.goalTips,
                    totalTips
                )
            }
            // Notify on milestones (50%, 75%, 90%)
            else if (percentage in listOf(50, 75, 90)) {
                GoalNotificationHelper.showMilestoneNotification(
                    getApplication(),
                    percentage,
                    goal.goalTips,
                    totalTips
                )
            }
        }
    }

    object Factory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val app = WaiterWalletAppHolder.app
            val db = app.database
            val repo = DailyEntryRepository(db.dailyEntryDao())
            @Suppress("UNCHECKED_CAST")
            return DailyEntryViewModel(repo, db.goalDao(), app) as T
        }
    }
}

// Helper to access Application instance for simple factory creation
object WaiterWalletAppHolder {
    lateinit var app: WaiterWalletApp
}
