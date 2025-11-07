package com.example.waiterwallet.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.YearMonth

@Entity(tableName = "monthly_goals")
data class MonthlyGoal(
    @PrimaryKey val yearMonth: String, // format YYYY-MM
    val goalTips: Double, // goal for total tips
    val commissionPercent: Double // user-set commission percent (default 1%)
) {
    companion object {
        fun key(ym: YearMonth) = String.format("%04d-%02d", ym.year, ym.monthValue)
    }
}
