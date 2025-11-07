package com.example.waiterwallet.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MonthlyGoalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(goal: MonthlyGoal)

    @Query("SELECT * FROM monthly_goals WHERE yearMonth = :key")
    fun goalForMonth(key: String): Flow<MonthlyGoal?>
}
