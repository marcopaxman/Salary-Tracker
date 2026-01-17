package com.example.waiterwallet.data

import java.time.YearMonth

/**
 * Data class representing total earnings for a single month.
 */
data class MonthlyEarnings(
    val yearMonth: YearMonth,
    val tips: Double,
    val commission: Double,
    val hourlyWages: Double
) {
    val total: Double get() = tips + commission + hourlyWages
}
