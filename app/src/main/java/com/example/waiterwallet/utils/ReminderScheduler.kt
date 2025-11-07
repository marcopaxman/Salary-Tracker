package com.example.waiterwallet.utils

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.waiterwallet.workers.DailyReminderWorker
import java.time.LocalTime
import java.util.concurrent.TimeUnit

object ReminderScheduler {
    
    fun scheduleReminder(context: Context, enabled: Boolean, time: LocalTime) {
        val workManager = WorkManager.getInstance(context)
        
        if (!enabled) {
            // Cancel existing reminders
            workManager.cancelUniqueWork(DailyReminderWorker.WORK_NAME)
            return
        }
        
        // Calculate initial delay until the specified time
        val now = LocalTime.now()
        val delayMinutes = if (time.isAfter(now)) {
            // Schedule for today
            java.time.Duration.between(now, time).toMinutes()
        } else {
            // Schedule for tomorrow
            java.time.Duration.between(now, time.plusHours(24)).toMinutes()
        }
        
        // Create periodic work request (daily)
        val workRequest = PeriodicWorkRequestBuilder<DailyReminderWorker>(
            24, TimeUnit.HOURS,
            15, TimeUnit.MINUTES // Flex interval
        )
            .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
            .build()
        
        // Schedule work
        workManager.enqueueUniquePeriodicWork(
            DailyReminderWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }
    
    fun cancelReminder(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(DailyReminderWorker.WORK_NAME)
    }
}
