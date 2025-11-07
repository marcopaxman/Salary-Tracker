package com.example.waiterwallet.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.waiterwallet.ui.screens.DashboardScreen
import com.example.waiterwallet.ui.screens.DataEntryScreen
import com.example.waiterwallet.ui.screens.SettingsScreen
import com.example.waiterwallet.WelcomeScreen

object Routes {
    const val Welcome = "welcome"
    const val Dashboard = "dashboard"
    const val Entry = "entry"
    const val Settings = "settings"
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
            DashboardScreen(onAddEntry = { navController.navigate(Routes.Entry) }, onOpenSettings = { navController.navigate(Routes.Settings) })
        }
        composable(Routes.Entry) {
            DataEntryScreen(onSaved = {
                onSaveEntry()
                navController.popBackStack(Routes.Dashboard, false)
            })
        }
        composable(Routes.Settings) {
            SettingsScreen(onSaved = { navController.popBackStack() })
        }
    }
}
