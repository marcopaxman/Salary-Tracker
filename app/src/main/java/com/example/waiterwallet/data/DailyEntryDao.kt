package com.example.waiterwallet.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface DailyEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: DailyEntry)

    @Query("SELECT * FROM daily_entries WHERE date = :date")
    fun entryByDate(date: LocalDate): Flow<DailyEntry?>

    @Query("SELECT * FROM daily_entries WHERE date BETWEEN :start AND :end ORDER BY date ASC")
    fun entriesBetween(start: LocalDate, end: LocalDate): Flow<List<DailyEntry>>

    @Query("SELECT SUM(COALESCE(tipsCash,0) + COALESCE(tipsCard,0)) FROM daily_entries WHERE date BETWEEN :start AND :end")
    fun totalTipsForMonth(start: LocalDate, end: LocalDate): Flow<Double?>

    @Query("SELECT SUM(turnover) FROM daily_entries WHERE date BETWEEN :start AND :end")
    fun totalTurnoverForMonth(start: LocalDate, end: LocalDate): Flow<Double?>

    // Job-filtered queries
    @Query("SELECT * FROM daily_entries WHERE date BETWEEN :start AND :end AND jobId = :jobId ORDER BY date ASC")
    fun entriesBetweenForJob(start: LocalDate, end: LocalDate, jobId: Long): Flow<List<DailyEntry>>

    @Query("SELECT SUM(COALESCE(tipsCash,0) + COALESCE(tipsCard,0)) FROM daily_entries WHERE date BETWEEN :start AND :end AND jobId = :jobId")
    fun totalTipsForMonthByJob(start: LocalDate, end: LocalDate, jobId: Long): Flow<Double?>

    @Query("SELECT SUM(turnover) FROM daily_entries WHERE date BETWEEN :start AND :end AND jobId = :jobId")
    fun totalTurnoverForMonthByJob(start: LocalDate, end: LocalDate, jobId: Long): Flow<Double?>

    @Delete
    suspend fun delete(entry: DailyEntry)
}
