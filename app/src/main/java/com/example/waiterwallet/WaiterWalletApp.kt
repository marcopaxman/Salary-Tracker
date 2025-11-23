package com.example.waiterwallet

import android.app.Application
import android.util.Log
import com.example.waiterwallet.data.AppDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class WaiterWalletApp : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob())
    val database by lazy { AppDatabase.getInstance(this) }

    override fun onCreate() {
        super.onCreate()
        
        // Expose app for simple ViewModel factories
        com.example.waiterwallet.ui.viewmodel.WaiterWalletAppHolder.app = this
        
        // Enable Firestore offline persistence
        initializeFirestore()
    }
    
    private fun initializeFirestore() {
        try {
            val firestore = FirebaseFirestore.getInstance()
            val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true) // Enable offline persistence
                .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED) // Unlimited cache
                .build()
            
            firestore.firestoreSettings = settings
            Log.d("WaiterWalletApp", "Firestore offline persistence enabled")
        } catch (e: Exception) {
            Log.e("WaiterWalletApp", "Failed to initialize Firestore", e)
        }
    }
}

