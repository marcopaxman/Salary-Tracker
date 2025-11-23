package com.example.waiterwallet.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.waiterwallet.data.AppDatabase
import com.example.waiterwallet.data.UnifiedRepositoryFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.LocalDate

sealed class AuthState {
    object Loading : AuthState()
    data class Authenticated(val userId: String, val email: String?) : AuthState()
    object Unauthenticated : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private const val TAG = "AuthViewModel"
    }
    
    private val auth: FirebaseAuth = Firebase.auth
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState
    
    init {
        checkAuthStatus()
    }
    
    private fun checkAuthStatus() {
        val currentUser = auth.currentUser
        _authState.value = if (currentUser != null) {
            AuthState.Authenticated(currentUser.uid, currentUser.email)
        } else {
            AuthState.Unauthenticated
        }
    }
    
    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                val result = auth.signInWithEmailAndPassword(email, password).await()
                _authState.value = AuthState.Authenticated(
                    result.user?.uid ?: "",
                    result.user?.email
                )
            } catch (e: Exception) {
                _authState.value = AuthState.Error(
                    e.message?.let { formatErrorMessage(it) } ?: "Sign in failed"
                )
            }
        }
    }
    
    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                _authState.value = AuthState.Authenticated(
                    result.user?.uid ?: "",
                    result.user?.email
                )
            } catch (e: Exception) {
                _authState.value = AuthState.Error(
                    e.message?.let { formatErrorMessage(it) } ?: "Sign up failed"
                )
            }
        }
    }
    
    fun signOut() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Starting sign out process...")
                
                // First, clear UnifiedRepository singleton to stop any ongoing syncs
                UnifiedRepositoryFactory.clearInstance()
                Log.d(TAG, "Cleared UnifiedRepository singleton")
                
                // Clear Room database on IO thread
                withContext(Dispatchers.IO) {
                    val database = AppDatabase.getInstance(getApplication())
                    database.clearAllTables()
                    Log.d(TAG, "Cleared Room database tables")
                    
                    // Verify tables are actually empty
                    val entryCount = database.dailyEntryDao().entriesBetween(
                        LocalDate.MIN,
                        LocalDate.MAX
                    ).first().size
                    Log.d(TAG, "Verification: Room has $entryCount entries after clearing")
                }
                
                // Sign out from Firebase
                auth.signOut()
                Log.d(TAG, "Signed out from Firebase")
                
                _authState.value = AuthState.Unauthenticated
                
            } catch (e: Exception) {
                Log.e(TAG, "Error during sign out", e)
                // Still sign out even if clearing fails
                UnifiedRepositoryFactory.clearInstance()
                auth.signOut()
                _authState.value = AuthState.Unauthenticated
            }
        }
    }
    
    fun clearError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.Unauthenticated
        }
    }
    
    private fun formatErrorMessage(message: String): String {
        return when {
            message.contains("no user record", ignoreCase = true) -> 
                "No account found with this email"
            message.contains("password is invalid", ignoreCase = true) -> 
                "Incorrect password"
            message.contains("email address is badly formatted", ignoreCase = true) -> 
                "Invalid email format"
            message.contains("email address is already in use", ignoreCase = true) -> 
                "This email is already registered"
            message.contains("password should be at least 6 characters", ignoreCase = true) -> 
                "Password must be at least 6 characters"
            message.contains("network error", ignoreCase = true) -> 
                "Network error. Check your internet connection"
            else -> message
        }
    }
}
