package com.example.waiterwallet.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.waiterwallet.ui.screens.MainScreen
import com.example.waiterwallet.WelcomeScreen

object Routes {
    const val Welcome = "welcome"
    const val Dashboard = "dashboard"
    const val Entry = "entry"
    const val Settings = "settings"
    const val Calendar = "calendar"
    const val Jobs = "jobs"
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String = Routes.Welcome,
    onSaveEntry: () -> Unit
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.Welcome) {
            WelcomeScreen(onContinue = { navController.navigate(Routes.Dashboard) })
        }
        composable(Routes.Dashboard) {
            MainScreen(onSaveEntry = onSaveEntry)
        }
    }
}
