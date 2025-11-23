# Firebase Quick Start Guide
**Get started with Firebase in 1 hour**

## Step 1: Firebase Console Setup (15 minutes)

### 1.1 Create Firebase Project
1. Go to https://console.firebase.google.com/
2. Click "Add project"
3. Project name: `Salary-Tracker`
4. Enable Google Analytics (optional)
5. Click "Create project"

### 1.2 Register Your Android App
1. Click the Android icon to add Android app
2. Enter package name: `com.example.waiterwallet`
3. App nickname: `Salary Tracker` (optional)
4. Click "Register app"
5. **Download `google-services.json`**
6. Place the file in: `app/google-services.json` (same level as `build.gradle.kts`)

### 1.3 Enable Authentication
1. In Firebase Console, go to **Build → Authentication**
2. Click "Get started"
3. Click "Sign-in method" tab
4. Enable **Email/Password** → Save
5. Enable **Google** → Configure → Add support email → Save

### 1.4 Create Firestore Database
1. Go to **Build → Firestore Database**
2. Click "Create database"
3. Choose **Start in test mode** (for development)
4. Select location: `us-central` or closest to you
5. Click "Enable"

---

## Step 2: Update Project Files (10 minutes)

### 2.1 Update `settings.gradle.kts` (Project Root)
Add this to the existing `pluginManagement` and `dependencyResolutionManagement` sections if not already present:

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
```

### 2.2 Update `build.gradle.kts` (Project Level)
Add Google Services plugin:

```kotlin
// Top-level build file
plugins {
    id("com.android.application") version "8.1.4" apply false
    id("org.jetbrains.kotlin.android") version "1.9.25" apply false
    id("com.google.devtools.ksp") version "1.9.25-1.0.20" apply false
    id("com.google.gms.google-services") version "4.4.0" apply false  // ADD THIS
}
```

### 2.3 Update `app/build.gradle.kts` (App Level)
Add Firebase dependencies at the end of the plugins block:

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")  // ADD THIS LINE
}
```

Then add Firebase dependencies at the end of the `dependencies` block:

```kotlin
dependencies {
    // ... existing dependencies ...

    // Firebase BOM (Bill of Materials) - Manages versions
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    
    // Firebase Authentication
    implementation("com.google.firebase:firebase-auth-ktx")
    
    // Cloud Firestore
    implementation("com.google.firebase:firebase-firestore-ktx")
    
    // Firebase Analytics (optional but recommended)
    implementation("com.google.firebase:firebase-analytics-ktx")
    
    // Play Services for Google Sign-In
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    
    // Coroutines support for Firebase
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
}
```

### 2.4 Sync Gradle
In Android Studio:
- Click **File → Sync Project with Gradle Files**
- Wait for sync to complete (1-2 minutes)

---

## Step 3: Test Firebase Connection (5 minutes)

### 3.1 Add Test Code to MainActivity

Open `MainActivity.kt` and add this to the `onCreate` method:

```kotlin
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import android.util.Log

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Test Firebase connection
        testFirebaseConnection()
        
        // ... rest of your existing code ...
    }
    
    private fun testFirebaseConnection() {
        val auth = Firebase.auth
        val firestore = Firebase.firestore
        
        Log.d("Firebase", "Auth initialized: ${auth != null}")
        Log.d("Firebase", "Firestore initialized: ${firestore != null}")
        
        // Test Firestore write
        val testDoc = hashMapOf("test" to "Hello Firebase!")
        firestore.collection("test")
            .add(testDoc)
            .addOnSuccessListener { 
                Log.d("Firebase", "✅ Firestore write successful!") 
            }
            .addOnFailureListener { e -> 
                Log.e("Firebase", "❌ Firestore write failed", e) 
            }
    }
}
```

### 3.2 Run the App
1. Run the app on emulator or device
2. Open **Logcat** in Android Studio
3. Filter for "Firebase"
4. You should see:
   ```
   D/Firebase: Auth initialized: true
   D/Firebase: Firestore initialized: true
   D/Firebase: ✅ Firestore write successful!
   ```

### 3.3 Verify in Firebase Console
1. Go to Firestore Database in Firebase Console
2. You should see a `test` collection with a document
3. **Success!** Firebase is connected

---

## Step 4: Create Basic Authentication Screen (30 minutes)

### 4.1 Create AuthViewModel

Create: `app/src/main/java/com/example/waiterwallet/ui/viewmodel/AuthViewModel.kt`

```kotlin
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
    data class Authenticated(val userId: String) : AuthState()
    object Unauthenticated : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = Firebase.auth
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState
    
    init {
        checkAuthStatus()
    }
    
    private fun checkAuthStatus() {
        val currentUser = auth.currentUser
        _authState.value = if (currentUser != null) {
            AuthState.Authenticated(currentUser.uid)
        } else {
            AuthState.Unauthenticated
        }
    }
    
    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                val result = auth.signInWithEmailAndPassword(email, password).await()
                _authState.value = AuthState.Authenticated(result.user?.uid ?: "")
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Sign in failed")
            }
        }
    }
    
    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                _authState.value = AuthState.Authenticated(result.user?.uid ?: "")
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Sign up failed")
            }
        }
    }
    
    fun signOut() {
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
    }
}
```

### 4.2 Create Login Screen

Create: `app/src/main/java/com/example/waiterwallet/ui/screens/LoginScreen.kt`

```kotlin
package com.example.waiterwallet.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.waiterwallet.ui.viewmodel.AuthState
import com.example.waiterwallet.ui.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) }
    
    val authState by viewModel.authState.collectAsState()
    
    // Navigate on successful authentication
    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            onLoginSuccess()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Salary Tracker",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Show error if present
        if (authState is AuthState.Error) {
            Text(
                text = (authState as AuthState.Error).message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Login/Sign Up Button
        Button(
            onClick = {
                if (isSignUp) {
                    viewModel.signUp(email, password)
                } else {
                    viewModel.signIn(email, password)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = authState !is AuthState.Loading && 
                     email.isNotBlank() && 
                     password.isNotBlank()
        ) {
            if (authState is AuthState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(if (isSignUp) "Sign Up" else "Login")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Toggle between Login and Sign Up
        TextButton(onClick = { isSignUp = !isSignUp }) {
            Text(
                if (isSignUp) "Already have an account? Login" 
                else "Don't have an account? Sign Up"
            )
        }
    }
}
```

### 4.3 Update MainActivity to Show Login

Modify `MainActivity.kt` to check auth state:

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            WaiterWalletTheme {
                val authViewModel: AuthViewModel = viewModel()
                val authState by authViewModel.authState.collectAsState()
                
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (authState) {
                        is AuthState.Authenticated -> {
                            // Show main app
                            MainScreen()
                        }
                        else -> {
                            // Show login screen
                            LoginScreen(
                                onLoginSuccess = { /* Will auto-navigate */ }
                            )
                        }
                    }
                }
            }
        }
    }
}
```

---

## Step 5: Test Authentication (10 minutes)

### 5.1 Run the App
1. Build and run the app
2. You should see the Login screen

### 5.2 Create Test Account
1. Click "Sign Up"
2. Enter email: `test@example.com`
3. Enter password: `password123`
4. Click "Sign Up"
5. Should navigate to main app

### 5.3 Verify in Firebase Console
1. Go to **Authentication → Users** in Firebase Console
2. You should see your test account listed
3. **Success!** Authentication is working

---

## What's Next?

You've successfully:
✅ Connected your app to Firebase  
✅ Set up Authentication  
✅ Created a login screen  
✅ Tested with a real account  

### Next Phase: Firestore Integration

Refer to **FIREBASE_MIGRATION_PLAN.md** for:
- Phase 2: Creating Firestore data models
- Phase 3: Syncing Room + Firestore
- Phase 4: Full migration

### Useful Commands

```bash
# Check if google-services.json exists
ls app/google-services.json

# View Firebase debug logs
adb logcat | grep Firebase

# Clear app data (reset auth state)
adb shell pm clear com.example.waiterwallet
```

---

## Troubleshooting

### Build Error: "google-services.json not found"
- Make sure file is in `app/` directory (not `app/src/`)
- Sync Gradle again

### Authentication Error: "Network error"
- Check internet connection
- Check Firebase Console → Authentication is enabled

### Firestore Error: "Permission denied"
- In Firestore Console → Rules
- Make sure you're in **test mode** (allows all reads/writes)

---

**You're ready to build!** 🚀

Continue with the full migration plan in `FIREBASE_MIGRATION_PLAN.md`.
