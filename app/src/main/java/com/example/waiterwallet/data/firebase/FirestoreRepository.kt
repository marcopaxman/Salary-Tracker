package com.example.waiterwallet.data.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.YearMonth

/**
 * Repository for Firestore operations.
 * Provides CRUD operations for all Firebase collections.
 */
class FirestoreRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    companion object {
        private const val TAG = "FirestoreRepository"
        private const val COLLECTION_USERS = "users"
        private const val COLLECTION_ENTRIES = "entries"
        private const val COLLECTION_JOBS = "jobs"
        private const val COLLECTION_GOALS = "goals"
        private const val COLLECTION_SETTINGS = "settings"
        private const val DOCUMENT_PREFERENCES = "preferences"
    }
    
    /**
     * Get the current user ID. Throws exception if user is not authenticated.
     */
    private val userId: String
        get() = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
    
    /**
     * Check if user is authenticated
     */
    val isAuthenticated: Boolean
        get() = auth.currentUser != null
    
    // ========================== Collections ==========================
    
    private fun entriesCollection() = 
        firestore.collection(COLLECTION_USERS)
            .document(userId)
            .collection(COLLECTION_ENTRIES)
    
    private fun jobsCollection() = 
        firestore.collection(COLLECTION_USERS)
            .document(userId)
            .collection(COLLECTION_JOBS)
    
    private fun goalsCollection() = 
        firestore.collection(COLLECTION_USERS)
            .document(userId)
            .collection(COLLECTION_GOALS)
    
    private fun settingsDocument() = 
        firestore.collection(COLLECTION_USERS)
            .document(userId)
            .collection(COLLECTION_SETTINGS)
            .document(DOCUMENT_PREFERENCES)
    
    // ========================== Daily Entries ==========================
    
    /**
     * Upsert a daily entry (create or update)
     */
    suspend fun upsertEntry(entry: FirestoreDailyEntry): Result<Unit> {
        return try {
            if (!isAuthenticated) {
                return Result.failure(IllegalStateException("User not authenticated"))
            }
            
            val updatedEntry = entry.copy(updatedAt = System.currentTimeMillis())
            entriesCollection()
                .document(entry.docId)
                .set(updatedEntry)
                .await()
            
            Log.d(TAG, "Successfully upserted entry: ${entry.date}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to upsert entry: ${entry.date}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get a single entry by date as a Flow (real-time updates)
     */
    fun getEntryByDate(date: LocalDate): Flow<FirestoreDailyEntry?> = callbackFlow {
        if (!isAuthenticated) {
            close(IllegalStateException("User not authenticated"))
            return@callbackFlow
        }
        
        val dateString = date.toString()
        val listener = entriesCollection()
            .document(dateString)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to entry: $dateString", error)
                    close(error)
                    return@addSnapshotListener
                }
                
                val entry = snapshot?.toObject(FirestoreDailyEntry::class.java)
                trySend(entry).isSuccess
            }
        
        awaitClose { listener.remove() }
    }
    
    /**
     * Get entries between two dates as a Flow (real-time updates)
     */
    fun getEntriesBetween(start: LocalDate, end: LocalDate): Flow<List<FirestoreDailyEntry>> = 
        callbackFlow {
            if (!isAuthenticated) {
                close(IllegalStateException("User not authenticated"))
                return@callbackFlow
            }
            
            val listener = entriesCollection()
                .whereGreaterThanOrEqualTo("date", start.toString())
                .whereLessThanOrEqualTo("date", end.toString())
                .orderBy("date", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Error listening to entries", error)
                        close(error)
                        return@addSnapshotListener
                    }
                    
                    val entries = snapshot?.toObjects(FirestoreDailyEntry::class.java) ?: emptyList()
                    trySend(entries).isSuccess
                }
            
            awaitClose { listener.remove() }
        }
    
    /**
     * Get all entries for a specific month
     */
    fun getEntriesForMonth(yearMonth: YearMonth): Flow<List<FirestoreDailyEntry>> {
        val start = yearMonth.atDay(1)
        val end = yearMonth.atEndOfMonth()
        return getEntriesBetween(start, end)
    }
    
    /**
     * Delete an entry by date
     */
    suspend fun deleteEntry(date: LocalDate): Result<Unit> {
        return try {
            if (!isAuthenticated) {
                return Result.failure(IllegalStateException("User not authenticated"))
            }
            
            entriesCollection()
                .document(date.toString())
                .delete()
                .await()
            
            Log.d(TAG, "Successfully deleted entry: $date")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete entry: $date", e)
            Result.failure(e)
        }
    }
    
    // ========================== Jobs ==========================
    
    /**
     * Upsert a job (create or update)
     */
    suspend fun upsertJob(job: FirestoreJob): Result<String> {
        return try {
            if (!isAuthenticated) {
                return Result.failure(IllegalStateException("User not authenticated"))
            }
            
            val updatedJob = job.copy(updatedAt = System.currentTimeMillis())
            
            val docRef = if (job.docId.isEmpty()) {
                // Create new job with auto-generated ID
                jobsCollection().document()
            } else {
                // Update existing job
                jobsCollection().document(job.docId)
            }
            
            docRef.set(updatedJob.copy(docId = docRef.id)).await()
            
            Log.d(TAG, "Successfully upserted job: ${job.name}")
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to upsert job: ${job.name}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get all jobs as a Flow (real-time updates)
     */
    fun getAllJobs(): Flow<List<FirestoreJob>> = callbackFlow {
        if (!isAuthenticated) {
            close(IllegalStateException("User not authenticated"))
            return@callbackFlow
        }
        
        val listener = jobsCollection()
            .orderBy("name")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to jobs", error)
                    close(error)
                    return@addSnapshotListener
                }
                
                val jobs = snapshot?.toObjects(FirestoreJob::class.java) ?: emptyList()
                trySend(jobs).isSuccess
            }
        
        awaitClose { listener.remove() }
    }
    
    /**
     * Get a job by ID
     */
    suspend fun getJobById(jobId: String): Result<FirestoreJob?> {
        return try {
            if (!isAuthenticated) {
                return Result.failure(IllegalStateException("User not authenticated"))
            }
            
            val snapshot = jobsCollection()
                .document(jobId)
                .get()
                .await()
            
            val job = snapshot.toObject(FirestoreJob::class.java)
            Result.success(job)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get job: $jobId", e)
            Result.failure(e)
        }
    }
    
    /**
     * Delete a job
     */
    suspend fun deleteJob(jobId: String): Result<Unit> {
        return try {
            if (!isAuthenticated) {
                return Result.failure(IllegalStateException("User not authenticated"))
            }
            
            jobsCollection()
                .document(jobId)
                .delete()
                .await()
            
            Log.d(TAG, "Successfully deleted job: $jobId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete job: $jobId", e)
            Result.failure(e)
        }
    }
    
    // ========================== Monthly Goals ==========================
    
    /**
     * Upsert a monthly goal
     */
    suspend fun upsertGoal(goal: FirestoreMonthlyGoal): Result<Unit> {
        return try {
            if (!isAuthenticated) {
                return Result.failure(IllegalStateException("User not authenticated"))
            }
            
            val updatedGoal = goal.copy(updatedAt = System.currentTimeMillis())
            goalsCollection()
                .document(goal.docId)
                .set(updatedGoal)
                .await()
            
            Log.d(TAG, "Successfully upserted goal: ${goal.yearMonth}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to upsert goal: ${goal.yearMonth}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get goal for a specific month as a Flow
     */
    fun getGoalForMonth(yearMonth: YearMonth): Flow<FirestoreMonthlyGoal?> = callbackFlow {
        if (!isAuthenticated) {
            close(IllegalStateException("User not authenticated"))
            return@callbackFlow
        }
        
        val monthKey = String.format("%04d-%02d", yearMonth.year, yearMonth.monthValue)
        val listener = goalsCollection()
            .document(monthKey)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to goal: $monthKey", error)
                    close(error)
                    return@addSnapshotListener
                }
                
                val goal = snapshot?.toObject(FirestoreMonthlyGoal::class.java)
                trySend(goal).isSuccess
            }
        
        awaitClose { listener.remove() }
    }
    
    /**
     * Get all goals as a Flow
     */
    fun getAllGoals(): Flow<List<FirestoreMonthlyGoal>> = callbackFlow {
        if (!isAuthenticated) {
            close(IllegalStateException("User not authenticated"))
            return@callbackFlow
        }
        
        val listener = goalsCollection()
            .orderBy("yearMonth", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to goals", error)
                    close(error)
                    return@addSnapshotListener
                }
                
                val goals = snapshot?.toObjects(FirestoreMonthlyGoal::class.java) ?: emptyList()
                trySend(goals).isSuccess
            }
        
        awaitClose { listener.remove() }
    }
    
    // ========================== User Settings ==========================
    
    /**
     * Save user settings
     */
    suspend fun saveSettings(settings: FirestoreUserSettings): Result<Unit> {
        return try {
            if (!isAuthenticated) {
                return Result.failure(IllegalStateException("User not authenticated"))
            }
            
            val updatedSettings = settings.copy(updatedAt = System.currentTimeMillis())
            settingsDocument()
                .set(updatedSettings)
                .await()
            
            Log.d(TAG, "Successfully saved settings")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save settings", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get user settings as a Flow
     */
    fun getSettings(): Flow<FirestoreUserSettings?> = callbackFlow {
        if (!isAuthenticated) {
            close(IllegalStateException("User not authenticated"))
            return@callbackFlow
        }
        
        val listener = settingsDocument()
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to settings", error)
                    close(error)
                    return@addSnapshotListener
                }
                
                val settings = snapshot?.toObject(FirestoreUserSettings::class.java)
                trySend(settings).isSuccess
            }
        
        awaitClose { listener.remove() }
    }
    
    // ========================== Batch Operations ==========================
    
    /**
     * Batch write multiple entries (useful for initial sync)
     */
    suspend fun batchUpsertEntries(entries: List<FirestoreDailyEntry>): Result<Unit> {
        return try {
            if (!isAuthenticated) {
                return Result.failure(IllegalStateException("User not authenticated"))
            }
            
            // Firestore batch limit is 500 operations
            entries.chunked(500).forEach { chunk ->
                val batch = firestore.batch()
                
                chunk.forEach { entry ->
                    val docRef = entriesCollection().document(entry.docId)
                    val updatedEntry = entry.copy(updatedAt = System.currentTimeMillis())
                    batch.set(docRef, updatedEntry)
                }
                
                batch.commit().await()
            }
            
            Log.d(TAG, "Successfully batch upserted ${entries.size} entries")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to batch upsert entries", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get count of all entries (for statistics)
     */
    suspend fun getEntriesCount(): Result<Int> {
        return try {
            if (!isAuthenticated) {
                return Result.failure(IllegalStateException("User not authenticated"))
            }
            
            val snapshot = entriesCollection().get().await()
            Result.success(snapshot.size())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get entries count", e)
            Result.failure(e)
        }
    }
}
