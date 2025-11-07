package com.example.waiterwallet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
    Column(modifier = Modifier.padding(24.dp)) {
        Text(text = "Waiter Wallet", style = MaterialTheme.typography.headlineMedium)
        Text(
            text = "Track your turnover, tips & commission.",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 12.dp)
        )
        Text(
            text = "Scaffold ready. Next: add navigation, data entry screens, calendar, and overview dashboard.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 24.dp)
        )
        Button(onClick = { onContinue?.invoke() }, modifier = Modifier.padding(top = 24.dp)) {
            Text("Continue")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WelcomePreview() {
    WaiterWalletTheme { WelcomeScreen() }
}
