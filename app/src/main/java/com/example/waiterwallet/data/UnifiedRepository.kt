package com.example.waiterwallet.data

import android.util.Log
import com.example.waiterwallet.data.firebase.FirestoreDailyEntry
import com.example.waiterwallet.data.firebase.FirestoreJob
import com.example.waiterwallet.data.firebase.FirestoreMonthlyGoal
import com.example.waiterwallet.data.firebase.FirestoreRepository
import com.example.waiterwallet.data.sync.SyncManager
import com.example.waiterwallet.utils.NetworkMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

/**
 * Unified repository that provides a single interface for both Room and Firestore.
 * Implements offline-first strategy:
 * - Writes go to Room immediately, then Firestore when online
 * - Reads come from Room, with background sync from Firestore
 */
class UnifiedRepository(
    private val roomEntryRepo: DailyEntryRepository,
    private val roomJobDao: JobDao,
    private val roomGoalDao: MonthlyGoalDao,
    private val firestoreRepo: FirestoreRepository,
    private val syncManager: SyncManager,
    private val networkMonitor: NetworkMonitor
) {
    companion object {
        private const val TAG = "UnifiedRepository"
    }
    
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    init {
        // Observe connectivity changes and sync when coming back online
        scope.launch {
            networkMonitor.observeConnectivity().collect { isOnline ->
                if (isOnline && firestoreRepo.isAuthenticated) {
                    Log.d(TAG, "Device came online - triggering sync to cloud")
                    try {
                        val result = syncManager.syncToCloud()
                        if (result.isSuccess) {
                            val stats = result.getOrNull()
                            Log.d(TAG, "Auto-sync completed: $stats")
                        } else {
                            Log.e(TAG, "Auto-sync failed", result.exceptionOrNull())
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error during auto-sync", e)
                    }
                }
            }
        }
    }
    
    // ========================== Daily Entries ==========================
    
    /**
     * Get entries between dates (offline-first)
     * Returns Room data immediately, triggers background sync from cloud
     */
    fun entriesBetween(start: LocalDate, end: LocalDate): Flow<List<DailyEntry>> {
        // Trigger background sync from Firestore to Room
        scope.launch {
            if (networkMonitor.isOnline() && firestoreRepo.isAuthenticated) {
                try {
                    firestoreRepo.getEntriesBetween(start, end).collect { firestoreEntries ->
                        firestoreEntries.forEach { firestoreEntry ->
                            try {
                                val roomEntry = firestoreEntry.toRoomEntity()
                                roomEntryRepo.upsert(roomEntry)
                            } catch (e: Exception) {
                                Log.e(TAG, "Failed to sync entry from cloud: ${firestoreEntry.date}", e)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Background sync failed", e)
                }
            }
        }
        
        // Return Room data immediately (offline support)
        return roomEntryRepo.entriesBetween(start, end)
    }
    
    /**
     * Get entry for specific date
     */
    fun entryByDate(date: LocalDate): Flow<DailyEntry?> {
        // Trigger background sync for this entry
        scope.launch {
            if (networkMonitor.isOnline() && firestoreRepo.isAuthenticated) {
                try {
                    firestoreRepo.getEntryByDate(date).collect { firestoreEntry ->
                        if (firestoreEntry != null) {
                            val roomEntry = firestoreEntry.toRoomEntity()
                            roomEntryRepo.upsert(roomEntry)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to sync entry for $date", e)
                }
            }
        }
        
        return roomEntryRepo.entryByDate(date)
    }
    
    /**
     * Upsert entry (write to both databases)
     */
    suspend fun upsertEntry(entry: DailyEntry) {
        // Save to Room first (immediate, works offline)
        roomEntryRepo.upsert(entry)
        Log.d(TAG, "✓ Saved entry to Room: ${entry.date}")
        
        // Check authentication status
        if (!firestoreRepo.isAuthenticated) {
            Log.w(TAG, "⚠ Not authenticated - cannot sync to Firestore")
            return
        }
        
        // Check network status
        val isOnline = networkMonitor.isOnline()
        Log.d(TAG, "Network status: ${if (isOnline) "ONLINE" else "OFFLINE"}")
        
        // Save to Firestore (when online)
        if (isOnline) {
            try {
                Log.d(TAG, "→ Attempting to sync entry to Firestore: ${entry.date}")
                val firestoreEntry = FirestoreDailyEntry.fromRoomEntity(entry)
                val result = firestoreRepo.upsertEntry(firestoreEntry)
                
                if (result.isSuccess) {
                    Log.d(TAG, "✓ Successfully synced entry to Firestore: ${entry.date}")
                } else {
                    Log.e(TAG, "✗ Failed to sync entry to Firestore: ${entry.date}", result.exceptionOrNull())
                    // Entry is still saved in Room, will sync later
                }
            } catch (e: Exception) {
                Log.e(TAG, "✗ Error syncing entry to Firestore: ${entry.date}", e)
                // Entry is still saved in Room
            }
        } else {
            Log.i(TAG, "⏱ Offline - entry saved to Room only. Will sync when online: ${entry.date}")
        }
    }
    
    /**
     * Get total tips for a month
     */
    fun totalTipsForMonth(date: LocalDate): Flow<Double?> {
        return roomEntryRepo.totalTipsForMonth(date)
    }
    
    /**
     * Get total turnover for a month
     */
    fun totalTurnoverForMonth(date: LocalDate): Flow<Double?> {
        return roomEntryRepo.totalTurnoverForMonth(date)
    }
    
    // ========================== Job-Filtered Queries ==========================
    
    /**
     * Get entries between dates for a specific job
     */
    fun entriesBetweenForJob(start: LocalDate, end: LocalDate, jobId: Long): Flow<List<DailyEntry>> {
        // Trigger background sync
        scope.launch {
            if (networkMonitor.isOnline() && firestoreRepo.isAuthenticated) {
                try {
                    firestoreRepo.getEntriesBetween(start, end).collect { firestoreEntries ->
                        firestoreEntries.forEach { firestoreEntry ->
                            try {
                                val roomEntry = firestoreEntry.toRoomEntity()
                                roomEntryRepo.upsert(roomEntry)
                            } catch (e: Exception) {
                                Log.e(TAG, "Failed to sync entry from cloud: ${firestoreEntry.date}", e)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Background sync failed for job entries", e)
                }
            }
        }
        
        return roomEntryRepo.entriesBetweenForJob(start, end, jobId)
    }
    
    /**
     * Get total tips for a month by job
     */
    fun totalTipsForMonthByJob(date: LocalDate, jobId: Long): Flow<Double?> {
        return roomEntryRepo.totalTipsForMonthByJob(date, jobId)
    }
    
    /**
     * Get total turnover for a month by job
     */
    fun totalTurnoverForMonthByJob(date: LocalDate, jobId: Long): Flow<Double?> {
        return roomEntryRepo.totalTurnoverForMonthByJob(date, jobId)
    }
    
    /**
     * Get total hours worked for a month
     */
    fun totalHoursWorkedForMonth(date: LocalDate): Flow<Double?> {
        return roomEntryRepo.totalHoursWorkedForMonth(date)
    }
    
    /**
     * Get monthly earnings history for the last N months.
     * Returns a list of MonthlyEarnings sorted by month (oldest first).
     * 
     * @param numberOfMonths Number of months to retrieve (default 12)
     * @param hourlyRate Hourly rate for calculating hourly wages
     * @param commissionPercent Commission percentage (e.g., 0.01 for 1%)
     */
    suspend fun getMonthlyEarningsHistory(
        numberOfMonths: Int = 12,
        hourlyRate: Double,
        commissionPercent: Double
    ): List<MonthlyEarnings> {
        val currentMonth = YearMonth.now()
        val monthsList = mutableListOf<MonthlyEarnings>()
        
        // Generate list of months from oldest to newest
        for (i in (numberOfMonths - 1) downTo 0) {
            val targetMonth = currentMonth.minusMonths(i.toLong())
            val start = targetMonth.atDay(1)
            
            // Get data for this month (using first() to get single value from Flow)
            val tips = roomEntryRepo.totalTipsForMonth(start).first() ?: 0.0
            val turnover = roomEntryRepo.totalTurnoverForMonth(start).first() ?: 0.0
            val hoursWorked = roomEntryRepo.totalHoursWorkedForMonth(start).first() ?: 0.0
            
            val commission = turnover * commissionPercent
            val wages = hoursWorked * hourlyRate
            
            monthsList.add(
                MonthlyEarnings(
                    yearMonth = targetMonth,
                    tips = tips,
                    commission = commission,
                    hourlyWages = wages
                )
            )
        }
        
        return monthsList
    }

    
    /**
     * Get total hours worked for a month by job
     */
    fun totalHoursWorkedForMonthByJob(date: LocalDate, jobId: Long): Flow<Double?> {
        return roomEntryRepo.totalHoursWorkedForMonthByJob(date, jobId)
    }
    
    // ========================== Jobs ==========================
    
    /**
     * Get all jobs (offline-first)
     */
    fun observeJobs(): Flow<List<Job>> {
        // Trigger background sync
        scope.launch {
            if (networkMonitor.isOnline() && firestoreRepo.isAuthenticated) {
                try {
                    firestoreRepo.getAllJobs().collect { firestoreJobs ->
                        firestoreJobs.forEach { firestoreJob ->
                            try {
                                val roomJob = firestoreJob.toRoomEntity()
                                // Only insert if doesn't exist
                                val existing = roomJobDao.allJobsSync()
                                if (!existing.any { it.name == roomJob.name }) {
                                    roomJobDao.insert(roomJob.copy(id = 0))
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Failed to sync job: ${firestoreJob.name}", e)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to sync jobs from cloud", e)
                }
            }
        }
        
        return roomJobDao.observeJobs()
    }
    
    /**
     * Get job by ID
     */
    fun jobById(id: Long): Flow<Job?> {
        return roomJobDao.jobById(id)
    }
    
    /**
     * Upsert job (write to both databases)
     */
    suspend fun upsertJob(job: Job): Long {
        // Save to Room first
        val jobId = roomJobDao.upsert(job)
        Log.d(TAG, "Saved job to Room: ${job.name}")
        
        // Save to Firestore
        if (networkMonitor.isOnline() && firestoreRepo.isAuthenticated) {
            try {
                val firestoreJob = FirestoreJob.fromRoomEntity(job.copy(id = jobId))
                val result = firestoreRepo.upsertJob(firestoreJob)
                
                if (result.isSuccess) {
                    Log.d(TAG, "Synced job to Firestore: ${job.name}")
                } else {
                    Log.w(TAG, "Failed to sync job to Firestore", result.exceptionOrNull())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing job to Firestore", e)
            }
        }
        
        return jobId
    }
    
    /**
     * Delete job (from both databases)
     */
    suspend fun deleteJob(job: Job) {
        // Delete from Room
        roomJobDao.delete(job)
        Log.d(TAG, "Deleted job from Room: ${job.name}")
        
        // Delete from Firestore
        if (networkMonitor.isOnline() && firestoreRepo.isAuthenticated) {
            try {
                val result = firestoreRepo.deleteJob(job.id.toString())
                if (result.isSuccess) {
                    Log.d(TAG, "Deleted job from Firestore: ${job.name}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting job from Firestore", e)
            }
        }
    }
    
    // ========================== Monthly Goals ==========================
    
    /**
     * Get goal for specific month
     */
    fun goalForMonth(key: String): Flow<MonthlyGoal?> {
        // Trigger background sync
        scope.launch {
            if (networkMonitor.isOnline() && firestoreRepo.isAuthenticated) {
                try {
                    val yearMonth = YearMonth.parse(key)
                    firestoreRepo.getGoalForMonth(yearMonth).collect { firestoreGoal ->
                        if (firestoreGoal != null) {
                            val roomGoal = firestoreGoal.toRoomEntity()
                            roomGoalDao.upsert(roomGoal)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to sync goal for $key", e)
                }
            }
        }
        
        return roomGoalDao.goalForMonth(key)
    }
    
    /**
     * Upsert monthly goal (write to both databases)
     */
    suspend fun upsertGoal(goal: MonthlyGoal) {
        // Save to Room first
        roomGoalDao.upsert(goal)
        Log.d(TAG, "Saved goal to Room: ${goal.yearMonth}")
        
        // Save to Firestore
        if (networkMonitor.isOnline() && firestoreRepo.isAuthenticated) {
            try {
                val firestoreGoal = FirestoreMonthlyGoal.fromRoomEntity(goal)
                val result = firestoreRepo.upsertGoal(firestoreGoal)
                
                if (result.isSuccess) {
                    Log.d(TAG, "Synced goal to Firestore: ${goal.yearMonth}")
                } else {
                    Log.w(TAG, "Failed to sync goal to Firestore", result.exceptionOrNull())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing goal to Firestore", e)
            }
        }
    }
    
    // ========================== Sync Operations ==========================
    
    /**
     * Manually trigger full sync
     */
    suspend fun performFullSync(): Result<SyncManager.SyncStats> {
        return syncManager.fullSync()
    }
    
    /**
     * Sync local data to cloud
     */
    suspend fun syncToCloud(): Result<SyncManager.SyncStats> {
        return syncManager.syncToCloud()
    }
    
    /**
     * Sync cloud data to local
     */
    suspend fun syncFromCloud(): Result<SyncManager.SyncStats> {
        return syncManager.syncFromCloud()
    }
}
