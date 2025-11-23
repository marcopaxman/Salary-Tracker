package com.example.waiterwallet.data.sync

import android.util.Log
import com.example.waiterwallet.data.DailyEntry
import com.example.waiterwallet.data.DailyEntryRepository
import com.example.waiterwallet.data.Job
import com.example.waiterwallet.data.JobDao
import com.example.waiterwallet.data.MonthlyGoal
import com.example.waiterwallet.data.MonthlyGoalDao
import com.example.waiterwallet.data.firebase.*
import com.example.waiterwallet.utils.NetworkMonitor
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.YearMonth

/**
 * Manages bidirectional synchronization between Room (local) and Firestore (cloud).
 * Implements offline-first strategy where Room is the cache and Firestore is the source of truth.
 */
class SyncManager(
    private val roomEntryRepo: DailyEntryRepository,
    private val roomJobDao: JobDao,
    private val roomGoalDao: MonthlyGoalDao,
    private val firestoreRepo: FirestoreRepository,
    private val networkMonitor: NetworkMonitor
) {
    companion object {
        private const val TAG = "SyncManager"
        private const val SYNC_RANGE_YEARS = 2 // Sync last 2 years of data
    }
    
    /**
     * Sync local Room data to Firestore (upload)
     */
    suspend fun syncToCloud(): Result<SyncStats> {
        if (!networkMonitor.isOnline()) {
            Log.d(TAG, "Offline - skipping sync to cloud")
            return Result.failure(Exception("No internet connection"))
        }
        
        if (!firestoreRepo.isAuthenticated) {
            Log.w(TAG, "User not authenticated - skipping sync")
            return Result.failure(Exception("User not authenticated"))
        }
        
        val stats = SyncStats()
        
        try {
            Log.d(TAG, "Starting sync to cloud...")
            
            // Sync entries (last 2 years)
            val startDate = LocalDate.now().minusYears(SYNC_RANGE_YEARS.toLong())
            val endDate = LocalDate.now().plusDays(1)
            
            val entries = roomEntryRepo.entriesBetween(startDate, endDate).first()
            Log.d(TAG, "Syncing ${entries.size} entries to cloud")
            
            val firestoreEntries = entries.map { FirestoreDailyEntry.fromRoomEntity(it) }
            val batchResult = firestoreRepo.batchUpsertEntries(firestoreEntries)
            
            if (batchResult.isSuccess) {
                stats.entriesSynced = entries.size
                Log.d(TAG, "Successfully synced ${entries.size} entries")
            } else {
                Log.e(TAG, "Failed to batch sync entries", batchResult.exceptionOrNull())
            }
            
            // Sync jobs
            val jobs = roomJobDao.allJobsSync()
            Log.d(TAG, "Syncing ${jobs.size} jobs to cloud")
            
            jobs.forEach { job ->
                val firestoreJob = FirestoreJob.fromRoomEntity(job)
                val result = firestoreRepo.upsertJob(firestoreJob)
                if (result.isSuccess) {
                    stats.jobsSynced++
                } else {
                    Log.e(TAG, "Failed to sync job: ${job.name}", result.exceptionOrNull())
                    stats.errors++
                }
            }
            
            // Sync goals (last 2 years)
            val currentMonth = YearMonth.now()
            val goals = mutableListOf<MonthlyGoal>()
            
            for (i in 0 until SYNC_RANGE_YEARS * 12) {
                val month = currentMonth.minusMonths(i.toLong())
                val key = MonthlyGoal.key(month)
                roomGoalDao.goalForMonth(key).first()?.let { goals.add(it) }
            }
            
            Log.d(TAG, "Syncing ${goals.size} goals to cloud")
            
            goals.forEach { goal ->
                val firestoreGoal = FirestoreMonthlyGoal.fromRoomEntity(goal)
                val result = firestoreRepo.upsertGoal(firestoreGoal)
                if (result.isSuccess) {
                    stats.goalsSynced++
                } else {
                    Log.e(TAG, "Failed to sync goal: ${goal.yearMonth}", result.exceptionOrNull())
                    stats.errors++
                }
            }
            
            Log.d(TAG, "Sync to cloud completed: $stats")
            return Result.success(stats)
            
        } catch (e: Exception) {
            Log.e(TAG, "Sync to cloud failed", e)
            return Result.failure(e)
        }
    }
    
    /**
     * Sync Firestore data to local Room (download)
     */
    suspend fun syncFromCloud(): Result<SyncStats> {
        if (!networkMonitor.isOnline()) {
            Log.d(TAG, "Offline - skipping sync from cloud")
            return Result.failure(Exception("No internet connection"))
        }
        
        if (!firestoreRepo.isAuthenticated) {
            Log.w(TAG, "User not authenticated - skipping sync")
            return Result.failure(Exception("User not authenticated"))
        }
        
        val stats = SyncStats()
        
        try {
            Log.d(TAG, "Starting sync from cloud...")
            
            // Sync entries (last 2 years)
            val startDate = LocalDate.now().minusYears(SYNC_RANGE_YEARS.toLong())
            val endDate = LocalDate.now().plusDays(1)
            
            val firestoreEntries = firestoreRepo.getEntriesBetween(startDate, endDate).first()
            Log.d(TAG, "Received ${firestoreEntries.size} entries from cloud")
            
            firestoreEntries.forEach { firestoreEntry ->
                try {
                    val roomEntry = firestoreEntry.toRoomEntity()
                    roomEntryRepo.upsert(roomEntry)
                    stats.entriesSynced++
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to save entry: ${firestoreEntry.date}", e)
                    stats.errors++
                }
            }
            
            // Sync jobs
            val firestoreJobs = firestoreRepo.getAllJobs().first()
            Log.d(TAG, "Received ${firestoreJobs.size} jobs from cloud")
            
            firestoreJobs.forEach { firestoreJob ->
                try {
                    val roomJob = firestoreJob.toRoomEntity()
                    // Only insert if job doesn't exist (avoid overwriting local IDs)
                    val existingJobs = roomJobDao.allJobsSync()
                    if (!existingJobs.any { it.name == roomJob.name }) {
                        roomJobDao.insert(roomJob.copy(id = 0)) // Let Room auto-generate ID
                        stats.jobsSynced++
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to save job: ${firestoreJob.name}", e)
                    stats.errors++
                }
            }
            
            // Sync goals
            val firestoreGoals = firestoreRepo.getAllGoals().first()
            Log.d(TAG, "Received ${firestoreGoals.size} goals from cloud")
            
            firestoreGoals.forEach { firestoreGoal ->
                try {
                    val roomGoal = firestoreGoal.toRoomEntity()
                    roomGoalDao.upsert(roomGoal)
                    stats.goalsSynced++
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to save goal: ${firestoreGoal.yearMonth}", e)
                    stats.errors++
                }
            }
            
            Log.d(TAG, "Sync from cloud completed: $stats")
            return Result.success(stats)
            
        } catch (e: Exception) {
            Log.e(TAG, "Sync from cloud failed", e)
            return Result.failure(e)
        }
    }
    
    /**
     * Full bidirectional sync
     * 1. Sync from cloud (Firestore is source of truth)
     * 2. Sync to cloud (upload any local-only changes)
     */
    suspend fun fullSync(): Result<SyncStats> {
        if (!networkMonitor.isOnline()) {
            return Result.failure(Exception("No internet connection"))
        }
        
        Log.d(TAG, "Starting full bidirectional sync...")
        
        // Download from cloud first (cloud is source of truth)
        val downloadResult = syncFromCloud()
        
        // Then upload any local changes
        val uploadResult = syncToCloud()
        
        // Combine stats
        val combinedStats = SyncStats(
            entriesSynced = (downloadResult.getOrNull()?.entriesSynced ?: 0) + 
                           (uploadResult.getOrNull()?.entriesSynced ?: 0),
            jobsSynced = (downloadResult.getOrNull()?.jobsSynced ?: 0) + 
                        (uploadResult.getOrNull()?.jobsSynced ?: 0),
            goalsSynced = (downloadResult.getOrNull()?.goalsSynced ?: 0) + 
                         (uploadResult.getOrNull()?.goalsSynced ?: 0),
            errors = (downloadResult.getOrNull()?.errors ?: 0) + 
                    (uploadResult.getOrNull()?.errors ?: 0)
        )
        
        Log.d(TAG, "Full sync completed: $combinedStats")
        return Result.success(combinedStats)
    }
    
    /**
     * Sync statistics
     */
    data class SyncStats(
        var entriesSynced: Int = 0,
        var jobsSynced: Int = 0,
        var goalsSynced: Int = 0,
        var errors: Int = 0
    ) {
        override fun toString(): String {
            return "SyncStats(entries=$entriesSynced, jobs=$jobsSynced, goals=$goalsSynced, errors=$errors)"
        }
    }
}
