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
    val jobId: Long? = null // optional job association
) {
    val totalTips: Double get() = (tipsCash ?: 0.0) + (tipsCard ?: 0.0)
}
