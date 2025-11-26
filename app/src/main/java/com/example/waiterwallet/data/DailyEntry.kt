package com.example.waiterwallet.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "daily_entries")
data class DailyEntry(
    @PrimaryKey val date: LocalDate,
    val turnover: Double, // total turnover for the day
    val tipsCash: Double?,
    val tipsCard: Double?,
    val notes: String?,
    val jobId: Long? = null, // optional job association
    val hoursWorked: Double? = null // optional hours worked for hourly wage calculation
) {
    val totalTips: Double get() = (tipsCash ?: 0.0) + (tipsCard ?: 0.0)
    
    /**
     * Calculate hourly wages based on hours worked and hourly rate.
     * Returns 0.0 if no hours worked.
     */
    fun calculateHourlyWage(hourlyRate: Double): Double {
        return (hoursWorked ?: 0.0) * hourlyRate
    }
}
