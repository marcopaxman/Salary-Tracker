package com.example.waiterwallet.data

import android.content.Context
import com.example.waiterwallet.data.firebase.FirestoreRepository
import com.example.waiterwallet.data.sync.SyncManager
import com.example.waiterwallet.utils.NetworkMonitor
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Factory to create UnifiedRepository instances with all dependencies
 */
object UnifiedRepositoryFactory {
    
    @Volatile
    private var instance: UnifiedRepository? = null
    
    fun getInstance(context: Context): UnifiedRepository {
        return instance ?: synchronized(this) {
            instance ?: createRepository(context).also { instance = it }
        }
    }
    
    private fun createRepository(context: Context): UnifiedRepository {
        val appContext = context.applicationContext
        val database = AppDatabase.getInstance(appContext)
        
        // Room components
        val roomEntryRepo = DailyEntryRepository(database.dailyEntryDao())
        val roomJobDao = database.jobDao()
        val roomGoalDao = database.goalDao()
        
        // Firebase components
        val firestore = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val firestoreRepo = FirestoreRepository(firestore, auth)
        
        // Utilities
        val networkMonitor = NetworkMonitor.getInstance(appContext)
        
        // Sync manager
        val syncManager = SyncManager(
            roomEntryRepo = roomEntryRepo,
            roomJobDao = roomJobDao,
            roomGoalDao = roomGoalDao,
            firestoreRepo = firestoreRepo,
            networkMonitor = networkMonitor
        )
        
        // Unified repository
        return UnifiedRepository(
            roomEntryRepo = roomEntryRepo,
            roomJobDao = roomJobDao,
            roomGoalDao = roomGoalDao,
            firestoreRepo = firestoreRepo,
            syncManager = syncManager,
            networkMonitor = networkMonitor
        )
    }
    
    /**
     * Clear the singleton instance (for testing or logout)
     */
    fun clearInstance() {
        synchronized(this) {
            instance = null
        }
    }
}
