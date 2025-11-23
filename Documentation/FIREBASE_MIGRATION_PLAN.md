# Firebase Migration Plan for Salary Tracker

## Overview
This document outlines the step-by-step process to migrate the Salary Tracker app from Room (local SQLite) to Firebase (cloud-based database with authentication and hosting).

---

## Current Architecture

### Data Layer
- **Database**: Room (SQLite) with 3 entities
  - `DailyEntry` - Daily work entries (date, turnover, tips, notes, jobId)
  - `Job` - Job/workplace information
  - `MonthlyGoal` - Monthly tip goals and commission settings
- **Storage**: DataStore for user preferences (commission %, reminder settings)
- **Data Flow**: Room DAOs → Repository → ViewModel → UI

### Key Features
- Offline-first architecture
- Local data storage
- CSV export functionality
- Work reminders via WorkManager

---

## Target Firebase Architecture

### Firebase Services Needed

1. **Firebase Authentication**
   - User sign-in (Email/Password, Google Sign-In)
   - Multi-device sync capability
   - User ID for data isolation

2. **Cloud Firestore** (Recommended over Realtime Database)
   - NoSQL document database
   - Offline persistence built-in
   - Real-time sync across devices
   - Better querying than Realtime Database

3. **Firebase Storage** (Optional)
   - Store CSV exports in cloud
   - Profile pictures (future feature)

4. **Firebase Cloud Functions** (Optional)
   - Automated data processing
   - Scheduled reminders
   - Data validation

5. **Firebase Hosting** (For Web Dashboard - Future)
   - Host web version of dashboard
   - Admin panel

---

## Migration Strategy: Phased Approach

### Phase 1: Setup & Authentication (Week 1)
**Goal**: Add Firebase to project and implement authentication

#### Step 1.1: Firebase Project Setup
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create new Firebase project: "Salary-Tracker"
3. Enable Google Analytics (optional)
4. Register Android app:
   - Package name: `com.example.waiterwallet`
   - Download `google-services.json`
   - Place in `app/` directory

#### Step 1.2: Update Build Files

**Project-level `build.gradle.kts`** (or `settings.gradle.kts`):
```kotlin
plugins {
    // ... existing plugins
    id("com.google.gms.google-services") version "4.4.0" apply false
}
```

**App-level `build.gradle.kts`**:
```kotlin
plugins {
    // ... existing plugins
    id("com.google.gms.google-services")
}

dependencies {
    // Firebase BOM (Bill of Materials)
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    
    // Firebase Authentication
    implementation("com.google.firebase:firebase-auth-ktx")
    
    // Cloud Firestore
    implementation("com.google.firebase:firebase-firestore-ktx")
    
    // Firebase Storage (optional)
    implementation("com.google.firebase:firebase-storage-ktx")
    
    // Play Services Auth (for Google Sign-In)
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    
    // ... existing dependencies
}
```

#### Step 1.3: Create Authentication UI
Create new screens:
- `LoginScreen.kt` - Email/password + Google Sign-In
- `RegisterScreen.kt` - New user registration
- `AuthViewModel.kt` - Handle authentication state

**Key Features**:
```kotlin
sealed class AuthState {
    object Loading : AuthState()
    data class Authenticated(val userId: String) : AuthState()
    object Unauthenticated : AuthState()
    data class Error(val message: String) : AuthState()
}
```

#### Step 1.4: Update Navigation
- Add authentication routes
- Add auth state check in `MainActivity`
- Protect main screens behind authentication

**Estimated Time**: 3-5 days

---

### Phase 2: Firestore Data Models (Week 2)
**Goal**: Create Firestore-compatible data models alongside Room

#### Step 2.1: Firestore Collection Structure
```
users/{userId}/
  ├── entries/{entryId}          // DailyEntry documents
  ├── jobs/{jobId}                // Job documents
  ├── goals/{yearMonth}           // MonthlyGoal documents
  └── settings/preferences        // User settings
```

#### Step 2.2: Create Firestore Data Classes

**`data/firebase/FirestoreDailyEntry.kt`**:
```kotlin
data class FirestoreDailyEntry(
    val id: String = "",
    val date: String = "",  // ISO-8601 format: "2025-11-23"
    val turnover: Double = 0.0,
    val tipsCash: Double? = null,
    val tipsCard: Double? = null,
    val notes: String? = null,
    val jobId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    // Convert to Room entity for local cache
    fun toRoomEntity(): DailyEntry {
        return DailyEntry(
            date = LocalDate.parse(date),
            turnover = turnover,
            tipsCash = tipsCash,
            tipsCard = tipsCard,
            notes = notes,
            jobId = jobId?.toLongOrNull()
        )
    }
    
    companion object {
        // Convert from Room entity
        fun fromRoomEntity(entry: DailyEntry): FirestoreDailyEntry {
            return FirestoreDailyEntry(
                id = entry.date.toString(),
                date = entry.date.toString(),
                turnover = entry.turnover,
                tipsCash = entry.tipsCash,
                tipsCard = entry.tipsCard,
                notes = entry.notes,
                jobId = entry.jobId?.toString()
            )
        }
    }
}
```

#### Step 2.3: Create Firestore Repository Layer

**`data/firebase/FirestoreRepository.kt`**:
```kotlin
class FirestoreRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val userId: String?
        get() = auth.currentUser?.uid
    
    private fun entriesCollection() = 
        firestore.collection("users").document(userId!!).collection("entries")
    
    // Upsert entry
    suspend fun upsertEntry(entry: FirestoreDailyEntry) = suspendCoroutine { cont ->
        entriesCollection()
            .document(entry.id)
            .set(entry)
            .addOnSuccessListener { cont.resume(Unit) }
            .addOnFailureListener { cont.resumeWithException(it) }
    }
    
    // Get entry by date
    fun getEntryByDate(date: LocalDate): Flow<FirestoreDailyEntry?> = callbackFlow {
        val listener = entriesCollection()
            .document(date.toString())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObject(FirestoreDailyEntry::class.java))
            }
        awaitClose { listener.remove() }
    }
    
    // Get entries in date range
    fun getEntriesBetween(start: LocalDate, end: LocalDate): Flow<List<FirestoreDailyEntry>> = 
        callbackFlow {
            val listener = entriesCollection()
                .whereGreaterThanOrEqualTo("date", start.toString())
                .whereLessThanOrEqualTo("date", end.toString())
                .orderBy("date")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    val entries = snapshot?.toObjects(FirestoreDailyEntry::class.java) ?: emptyList()
                    trySend(entries)
                }
            awaitClose { listener.remove() }
        }
    
    // Similar methods for Jobs and MonthlyGoals...
}
```

**Estimated Time**: 4-6 days

---

### Phase 3: Hybrid Architecture (Week 3)
**Goal**: Run Room and Firestore in parallel with sync mechanism

#### Step 3.1: Create Sync Manager

**`data/sync/SyncManager.kt`**:
```kotlin
class SyncManager(
    private val roomRepo: DailyEntryRepository,
    private val firestoreRepo: FirestoreRepository,
    private val connectivityManager: ConnectivityManager
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    // Sync local changes to cloud
    suspend fun syncToCloud() {
        if (!isOnline()) return
        
        // Get all entries from Room
        roomRepo.entriesBetween(/* all time */)
            .first()
            .forEach { entry ->
                try {
                    val firestoreEntry = FirestoreDailyEntry.fromRoomEntity(entry)
                    firestoreRepo.upsertEntry(firestoreEntry)
                } catch (e: Exception) {
                    Log.e("SyncManager", "Failed to sync entry: ${entry.date}", e)
                }
            }
    }
    
    // Pull cloud changes to local
    suspend fun syncFromCloud() {
        if (!isOnline()) return
        
        firestoreRepo.getEntriesBetween(/* all time */)
            .first()
            .forEach { firestoreEntry ->
                try {
                    val roomEntry = firestoreEntry.toRoomEntity()
                    roomRepo.upsert(roomEntry)
                } catch (e: Exception) {
                    Log.e("SyncManager", "Failed to sync from cloud", e)
                }
            }
    }
    
    // Bi-directional sync
    suspend fun fullSync() {
        syncFromCloud()  // Cloud is source of truth
        syncToCloud()    // Upload any local-only changes
    }
    
    private fun isOnline(): Boolean {
        // Check network connectivity
    }
}
```

#### Step 3.2: Update Repository Pattern

**`data/UnifiedRepository.kt`** (Wraps both Room and Firestore):
```kotlin
class UnifiedRepository(
    private val roomRepo: DailyEntryRepository,
    private val firestoreRepo: FirestoreRepository,
    private val syncManager: SyncManager,
    private val isOnline: () -> Boolean
) {
    // Read from Room (offline-first), sync in background
    fun getEntriesBetween(start: LocalDate, end: LocalDate): Flow<List<DailyEntry>> {
        // Trigger background sync
        CoroutineScope(Dispatchers.IO).launch {
            if (isOnline()) syncManager.syncFromCloud()
        }
        
        // Return Room data immediately (offline support)
        return roomRepo.entriesBetween(start, end)
    }
    
    // Write to both Room and Firestore
    suspend fun upsertEntry(entry: DailyEntry) {
        // Save to Room first (immediate)
        roomRepo.upsert(entry)
        
        // Save to Firestore (when online)
        if (isOnline()) {
            try {
                val firestoreEntry = FirestoreDailyEntry.fromRoomEntity(entry)
                firestoreRepo.upsertEntry(firestoreEntry)
            } catch (e: Exception) {
                // Queue for later sync
                Log.e("UnifiedRepository", "Failed to sync to cloud", e)
            }
        }
    }
}
```

#### Step 3.3: Update ViewModels
Replace direct Room repository usage with `UnifiedRepository`

**Estimated Time**: 5-7 days

---

### Phase 4: Migration & Testing (Week 4)
**Goal**: Test sync, handle edge cases, migrate existing users

#### Step 4.1: Data Migration Tool
Create one-time migration function:
```kotlin
suspend fun migrateExistingDataToFirebase(
    context: Context,
    userId: String
) {
    val roomDb = AppDatabase.getInstance(context)
    val firestore = FirebaseFirestore.getInstance()
    
    // Migrate entries
    val entries = roomDb.dailyEntryDao().getAllEntries() // Create this method
    entries.forEach { entry ->
        val firestoreEntry = FirestoreDailyEntry.fromRoomEntity(entry)
        firestore.collection("users/$userId/entries")
            .document(firestoreEntry.id)
            .set(firestoreEntry)
            .await()
    }
    
    // Migrate jobs, goals, settings...
}
```

#### Step 4.2: Testing Checklist
- [ ] Offline functionality (airplane mode)
- [ ] Sync after regaining connectivity
- [ ] Multi-device sync (install on 2 devices)
- [ ] Conflict resolution (edit same entry on 2 devices)
- [ ] Large dataset performance
- [ ] Auth flow (login, logout, re-login)
- [ ] CSV export still works
- [ ] Reminders still work

#### Step 4.3: Rollout Strategy
1. **Beta testing**: Release to small group
2. **Gradual rollout**: Percentage-based deployment
3. **Monitoring**: Firebase Analytics + Crashlytics

**Estimated Time**: 5-7 days

---

### Phase 5: Firebase Hosting (Optional - Future)
**Goal**: Create web dashboard for desktop access

#### Components
1. **React/Next.js Web App**
   - Read-only dashboard initially
   - View stats, charts, export CSV
   - Uses Firebase JS SDK

2. **Firebase Hosting Setup**
```bash
npm install -g firebase-tools
firebase login
firebase init hosting
firebase deploy
```

3. **Authentication Web SDK**
```javascript
import { getAuth, signInWithEmailAndPassword } from "firebase/auth";

const auth = getAuth();
signInWithEmailAndPassword(auth, email, password)
  .then((userCredential) => {
    // Access Firestore data
  });
```

**Estimated Time**: 2-3 weeks (separate project)

---

## Detailed Code Changes Required

### 1. Authentication Implementation

**New Files to Create**:
```
ui/
  auth/
    LoginScreen.kt
    RegisterScreen.kt
    ForgotPasswordScreen.kt
    AuthViewModel.kt
  
data/
  firebase/
    FirestoreRepository.kt
    FirestoreDailyEntry.kt
    FirestoreJob.kt
    FirestoreGoal.kt
  sync/
    SyncManager.kt
    SyncWorker.kt  // WorkManager for periodic sync
  
utils/
  AuthManager.kt
  NetworkMonitor.kt
```

**Modified Files**:
```
MainActivity.kt           // Add auth check
MainScreen.kt            // Add logout button
Navigation.kt            // Add auth routes
build.gradle.kts         // Add Firebase dependencies
WaiterWalletApp.kt       // Initialize Firebase
```

### 2. Gradle Configuration

**`build.gradle.kts` (Project level)**:
```kotlin
buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.4.0")
    }
}
```

**`build.gradle.kts` (App level)** - Add after existing dependencies:
```kotlin
// Firebase
implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
implementation("com.google.firebase:firebase-auth-ktx")
implementation("com.google.firebase:firebase-firestore-ktx")
implementation("com.google.firebase:firebase-analytics-ktx")
implementation("com.google.firebase:firebase-crashlytics-ktx")

// Coroutines for Firebase
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
```

### 3. AndroidManifest.xml Updates

Add permissions:
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

---

## Security Considerations

### 1. Firestore Security Rules

**`firestore.rules`** (Deploy via Firebase Console):
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Users can only access their own data
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
      
      match /entries/{entryId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
      
      match /jobs/{jobId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
      
      match /goals/{goalId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
      
      match /settings/{settingId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
    }
  }
}
```

### 2. Data Validation
Use Cloud Functions for server-side validation:
```javascript
exports.validateEntry = functions.firestore
  .document('users/{userId}/entries/{entryId}')
  .onWrite((change, context) => {
    const newValue = change.after.data();
    
    // Validate turnover is positive
    if (newValue.turnover < 0) {
      throw new Error('Turnover must be positive');
    }
    
    // Validate date format
    // etc.
  });
```

---

## Cost Estimation (Firebase Free Tier)

### Spark Plan (Free)
- **Authentication**: Unlimited users
- **Firestore**: 
  - 1 GB storage
  - 50K reads/day
  - 20K writes/day
  - 20K deletes/day
- **Storage**: 5 GB
- **Hosting**: 10 GB bandwidth/month

### Typical Usage (Single User)
- **Writes**: ~30-50/month (1-2 entries/day)
- **Reads**: ~1000/month (viewing data)
- **Storage**: <10 MB/year

**Conclusion**: Free tier is MORE than sufficient for personal use and small user base (<100 users).

---

## Rollback Plan

If Firebase migration encounters issues:

1. **Keep Room database**: Don't remove Room code during Phase 3
2. **Feature flag**: Add `useFirebase` boolean in SettingsStore
3. **Toggle between backends**: 
   ```kotlin
   val repository = if (useFirebase) {
       unifiedRepository
   } else {
       roomOnlyRepository
   }
   ```
4. **Gradual transition**: Let users opt-in to cloud sync

---

## Immediate Next Steps (This Week)

### Priority 1: Firebase Console Setup (1 hour)
1. ✅ Create Firebase project
2. ✅ Register Android app
3. ✅ Download `google-services.json`
4. ✅ Enable Authentication (Email/Password + Google)
5. ✅ Create Firestore database

### Priority 2: Update Build Configuration (30 mins)
1. ✅ Add Firebase dependencies to `build.gradle.kts`
2. ✅ Add Google Services plugin
3. ✅ Place `google-services.json` in `app/` folder
4. ✅ Sync Gradle

### Priority 3: Create Authentication UI (2-3 days)
1. ✅ Create `LoginScreen.kt`
2. ✅ Create `RegisterScreen.kt`
3. ✅ Create `AuthViewModel.kt`
4. ✅ Update navigation flow
5. ✅ Test basic email/password auth

### Priority 4: Firestore Data Models (2-3 days)
1. ✅ Create Firebase data classes
2. ✅ Implement FirestoreRepository
3. ✅ Test basic CRUD operations
4. ✅ Verify data structure in Firebase Console

**Status**: ✅ **COMPLETE** - See [PHASE_2_IMPLEMENTATION.md](PHASE_2_IMPLEMENTATION.md) for details

---

## Resources & Documentation

### Official Documentation
- [Firebase Android Setup](https://firebase.google.com/docs/android/setup)
- [Firebase Authentication](https://firebase.google.com/docs/auth/android/start)
- [Cloud Firestore](https://firebase.google.com/docs/firestore/quickstart)
- [Firestore Security Rules](https://firebase.google.com/docs/firestore/security/get-started)

### Code Samples
- [Firebase Android Samples](https://github.com/firebase/quickstart-android)
- [Firestore + Room Example](https://github.com/firebase/firebase-android-sdk/tree/master/firebase-firestore)

### Best Practices
- [Offline Data](https://firebase.google.com/docs/firestore/manage-data/enable-offline)
- [Structure Data](https://firebase.google.com/docs/firestore/manage-data/structure-data)
- [Optimize Performance](https://firebase.google.com/docs/firestore/best-practices)

---

## Questions to Consider

Before starting migration:

1. **Multi-user**: Will multiple users work at same workplace? (Shared jobs?)
2. **Data ownership**: Should users own their data exclusively?
3. **Export**: Keep CSV export or add PDF/Excel?
4. **Backup**: Manual backup feature or auto-backup to Drive?
5. **Analytics**: Track usage patterns? (Firebase Analytics)
6. **Push notifications**: Goal reminders via FCM instead of WorkManager?

---

## Timeline Summary

| Phase | Duration | Key Deliverable |
|-------|----------|----------------|
| 1. Auth Setup | 3-5 days | Working login/register |
| 2. Firestore Models | 4-6 days | Data models + repository |
| 3. Hybrid Architecture | 5-7 days | Room + Firestore sync |
| 4. Migration & Testing | 5-7 days | Production-ready |
| **Total** | **3-4 weeks** | Firebase-powered app |

---

## Success Metrics

Post-migration validation:
- [ ] All existing features work identically
- [ ] Data syncs across devices within 5 seconds
- [ ] App works offline (no crashes)
- [ ] No data loss during sync
- [ ] Authentication is secure
- [ ] Performance is acceptable (<2s screen load)

---

## Need Help?

If you encounter issues during migration:
1. Check Firebase Console logs
2. Review Logcat for errors
3. Test Firestore rules in Firebase Console simulator
4. Join Firebase Discord community
5. Stack Overflow tag: `firebase` + `android` + `kotlin`

---

**Last Updated**: November 23, 2025
**Version**: 1.0
