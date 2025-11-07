package com.example.waiterwallet.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import java.time.YearMonth

class DailyEntryRepository(private val dao: DailyEntryDao) {
    suspend fun upsert(entry: DailyEntry) = dao.upsert(entry)

    fun entryByDate(date: LocalDate): Flow<DailyEntry?> = dao.entryByDate(date)

    fun entriesBetween(start: LocalDate, end: LocalDate): Flow<List<DailyEntry>> = dao.entriesBetween(start, end)

    fun totalTipsForMonth(date: LocalDate): Flow<Double?> {
        val (start, end) = monthRange(date)
        return dao.totalTipsForMonth(start, end)
    }

    fun totalTurnoverForMonth(date: LocalDate): Flow<Double?> {
        val (start, end) = monthRange(date)
        return dao.totalTurnoverForMonth(start, end)
    }

    // Job-filtered methods
    fun entriesBetweenForJob(start: LocalDate, end: LocalDate, jobId: Long): Flow<List<DailyEntry>> =
        dao.entriesBetweenForJob(start, end, jobId)

    fun totalTipsForMonthByJob(date: LocalDate, jobId: Long): Flow<Double?> {
        val (start, end) = monthRange(date)
        return dao.totalTipsForMonthByJob(start, end, jobId)
    }

    fun totalTurnoverForMonthByJob(date: LocalDate, jobId: Long): Flow<Double?> {
        val (start, end) = monthRange(date)
        return dao.totalTurnoverForMonthByJob(start, end, jobId)
    }

    companion object {
        fun monthRange(anchor: LocalDate): Pair<LocalDate, LocalDate> {
            val start = anchor.withDayOfMonth(1)
            val end = anchor.withDayOfMonth(anchor.lengthOfMonth())
            return start to end
        }
    }
}
