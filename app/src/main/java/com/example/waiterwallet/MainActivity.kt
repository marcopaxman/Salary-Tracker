package com.example.waiterwallet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.waiterwallet.ui.navigation.AppNavHost
import com.example.waiterwallet.ui.screens.LoginScreen
import com.example.waiterwallet.ui.theme.WaiterWalletTheme
import com.example.waiterwallet.ui.viewmodel.AuthState
import com.example.waiterwallet.ui.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            WaiterWalletTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AuthenticationWrapper()
                }
            }
        }
    }
}

@Composable
fun AuthenticationWrapper(
    authViewModel: AuthViewModel = viewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    
    when (authState) {
        is AuthState.Loading -> {
            // Show loading indicator while checking auth status
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is AuthState.Authenticated -> {
            // User is logged in, show main app
            val navController = rememberNavController()
            AppNavHost(
                navController = navController,
                onSaveEntry = { /* could trigger a snackbar/update in future */ },
                authViewModel = authViewModel
            )
        }
        is AuthState.Unauthenticated, is AuthState.Error -> {
            // User is not logged in, show login screen
            LoginScreen(
                onLoginSuccess = { /* AuthViewModel will handle state change */ },
                viewModel = authViewModel
            )
        }
    }
}
