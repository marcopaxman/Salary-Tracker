package com.example.waiterwallet.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface JobDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(job: Job): Long

    @Query("SELECT * FROM jobs ORDER BY name ASC")
    fun observeJobs(): Flow<List<Job>>

    @Query("SELECT * FROM jobs WHERE id = :id")
    fun jobById(id: Long): Flow<Job?>
}
