package com.example.waiterwallet.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.waiterwallet.ui.navigation.Routes

data class BottomNavItem(
    val route: String,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Routes.Dashboard, "Dashboard", Icons.Default.Home),
    BottomNavItem(Routes.Calendar, "Calendar", Icons.Default.DateRange),
    BottomNavItem(Routes.Jobs, "Jobs", Icons.Default.Person),
    BottomNavItem(Routes.Settings, "Settings", Icons.Default.Settings)
)

@Composable
fun MainScreen(onSaveEntry: () -> Unit) {
    val navController = rememberNavController()
    
    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.Dashboard,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.Dashboard) {
                DashboardScreen(
                    onAddEntry = { navController.navigate(Routes.Entry) }
                )
            }
            composable(Routes.Calendar) {
                CalendarScreen(onNavigateBack = {})
            }
            composable(Routes.Jobs) {
                JobsScreen(onNavigateBack = {})
            }
            composable(Routes.Settings) {
                SettingsScreen(onSaved = {})
            }
            composable(Routes.Entry) {
                DataEntryScreen(onSaved = {
                    onSaveEntry()
                    navController.popBackStack()
                })
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    NavigationBar {
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                onClick = {
                    navController.navigate(item.route) {
                        // Pop up to the start destination
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // Avoid multiple copies
                        launchSingleTop = true
                        // Restore state when reselecting
                        restoreState = true
                    }
                }
            )
        }
    }
}
