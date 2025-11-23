package com.example.waiterwallet.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.waiterwallet.ui.screens.MainScreen
import com.example.waiterwallet.ui.viewmodel.AuthViewModel

object Routes {
    const val Dashboard = "dashboard"
    const val Entry = "entry"
    const val Settings = "settings"
    const val Calendar = "calendar"
    const val Jobs = "jobs"
    const val FirebaseTest = "firebase_test" // Test screen for Firebase operations
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    onSaveEntry: () -> Unit,
    authViewModel: AuthViewModel
) {
    // MainScreen now handles all the bottom navigation
    MainScreen(
        onSaveEntry = onSaveEntry,
        authViewModel = authViewModel
    )
}
