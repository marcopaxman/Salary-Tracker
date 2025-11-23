package com.example.waiterwallet.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class AuthState {
    object Loading : AuthState()
    data class Authenticated(val userId: String, val email: String?) : AuthState()
    object Unauthenticated : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {
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
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
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
