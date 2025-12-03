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
    const val Entries = "entries"
    const val Settings = "settings"
    const val Calendar = "calendar"
    const val Jobs = "jobs"
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
