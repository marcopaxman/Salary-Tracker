package com.example.waiterwallet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.example.waiterwallet.data.DailyEntryRepository
import com.example.waiterwallet.ui.theme.WaiterWalletTheme
import com.example.waiterwallet.ui.navigation.AppNavHost

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WaiterWalletTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    val app = application as WaiterWalletApp
                    val repository = DailyEntryRepository(app.database.dailyEntryDao())
                    AppNavHost(
                        navController = navController,
                        onSaveEntry = { /* could trigger a snackbar/update in future */ }
                    )
                }
            }
        }
    }
}

@Composable
fun WelcomeScreen(onContinue: (() -> Unit)? = null) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
    ) {
        // Simple logo shape
        androidx.compose.foundation.Canvas(
            modifier = Modifier
                .size(120.dp)
                .padding(bottom = 24.dp)
        ) {
            // Circle background
            drawCircle(
                color = androidx.compose.ui.graphics.Color(0xFF009688), // Teal
                radius = size.minDimension / 2
            )
            // Dollar sign representation
            drawCircle(
                color = androidx.compose.ui.graphics.Color.White,
                radius = size.minDimension / 3,
                center = center
            )
        }
        
        Text(
            text = "Waiter Wallet",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Track Your Tips & Earnings",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 8.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = { onContinue?.invoke() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Login", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WelcomePreview() {
    WaiterWalletTheme { WelcomeScreen() }
}
