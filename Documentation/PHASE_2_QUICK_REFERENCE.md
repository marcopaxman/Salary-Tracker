# Firebase Phase 2 Quick Reference

## What Was Implemented

✅ **Firestore Data Models**
- `FirestoreDailyEntry` - Cloud version of daily work entries
- `FirestoreJob` - Cloud version of jobs
- `FirestoreMonthlyGoal` - Cloud version of monthly goals
- `FirestoreUserSettings` - New cloud-synced settings

✅ **Repository Layer**
- `FirestoreRepository` - Complete CRUD operations for all models
- 18 methods covering all database operations
- Real-time Flow-based updates
- Batch write support (500 items per batch)

✅ **Test Interface**
- `FirebaseTestScreen` - Interactive testing UI
- 10 test buttons for all operations
- Color-coded status feedback
- Accessible via "Test" tab (Build icon)

## Quick Test Guide

### 1. Launch App
```bash
# Already installed and running on emulator
```

### 2. Navigate to Test Screen
- Look for "Test" tab in bottom navigation (wrench/build icon)
- Tap to open Firebase test interface

### 3. Test Operations (in order)

**Create Data**:
1. Tap "Create Test Entry" → Should see green success message
2. Tap "Create Test Job" → Note the generated job ID
3. Tap "Create Test Goal" → Confirms goal for current month
4. Tap "Save Test Settings" → Settings saved

**Read Data**:
1. Tap "Read Today's Entry" → Shows turnover and tips
2. Tap "Read This Month's Entries" → Shows count of entries
3. Tap "Read All Jobs" → Lists all jobs
4. Tap "Read This Month's Goal" → Shows goal amount
5. Tap "Read Settings" → Shows commission and reminder settings

**Verify in Firebase Console**:
1. Open https://console.firebase.google.com/
2. Select "Salary-Tracker" project
3. Go to Firestore Database
4. Navigate: `users → [your-user-id] → entries/jobs/goals/settings`
5. Verify all test data is present

## Collection Structure

```
users/{userId}/
  ├── entries/
  │   └── {date}              (e.g., "2025-11-23")
  │       ├── date: "2025-11-23"
  │       ├── turnover: 1500.0
  │       ├── tipsCash: 75.0
  │       ├── tipsCard: 45.0
  │       ├── notes: "..."
  │       ├── jobId: "abc123"
  │       ├── createdAt: 1700000000000
  │       └── updatedAt: 1700000000000
  │
  ├── jobs/
  │   └── {auto-id}           (e.g., "xYz789")
  │       ├── id: "xYz789"
  │       ├── name: "Test Restaurant"
  │       ├── createdAt: 1700000000000
  │       └── updatedAt: 1700000000000
  │
  ├── goals/
  │   └── {yearMonth}         (e.g., "2025-11")
  │       ├── yearMonth: "2025-11"
  │       ├── goalTips: 2000.0
  │       ├── commissionPercent: 0.01
  │       ├── createdAt: 1700000000000
  │       └── updatedAt: 1700000000000
  │
  └── settings/
      └── preferences
          ├── commissionPercent: 0.015
          ├── reminderEnabled: true
          ├── reminderHour: 20
          ├── reminderMinute: 30
          └── updatedAt: 1700000000000
```

## Key Repository Methods

### Daily Entries
```kotlin
suspend fun upsertEntry(entry: FirestoreDailyEntry): Result<Unit>
fun getEntryByDate(date: LocalDate): Flow<FirestoreDailyEntry?>
fun getEntriesBetween(start: LocalDate, end: LocalDate): Flow<List<FirestoreDailyEntry>>
fun getEntriesForMonth(yearMonth: YearMonth): Flow<List<FirestoreDailyEntry>>
suspend fun deleteEntry(date: LocalDate): Result<Unit>
```

### Jobs
```kotlin
suspend fun upsertJob(job: FirestoreJob): Result<String>  // Returns job ID
fun getAllJobs(): Flow<List<FirestoreJob>>
suspend fun getJobById(jobId: String): Result<FirestoreJob?>
suspend fun deleteJob(jobId: String): Result<Unit>
```

### Monthly Goals
```kotlin
suspend fun upsertGoal(goal: FirestoreMonthlyGoal): Result<Unit>
fun getGoalForMonth(yearMonth: YearMonth): Flow<FirestoreMonthlyGoal?>
fun getAllGoals(): Flow<List<FirestoreMonthlyGoal>>
```

### Settings
```kotlin
suspend fun saveSettings(settings: FirestoreUserSettings): Result<Unit>
fun getSettings(): Flow<FirestoreUserSettings?>
```

### Utilities
```kotlin
suspend fun batchUpsertEntries(entries: List<FirestoreDailyEntry>): Result<Unit>
suspend fun getEntriesCount(): Result<Int>
val isAuthenticated: Boolean
```

## Usage Example

```kotlin
// In a ViewModel or Composable
val repository = FirestoreRepository()

// Create entry
scope.launch {
    val entry = FirestoreDailyEntry(
        id = LocalDate.now().toString(),
        date = LocalDate.now().toString(),
        turnover = 1500.0,
        tipsCash = 75.0,
        tipsCard = 45.0
    )
    val result = repository.upsertEntry(entry)
    if (result.isSuccess) {
        println("Entry created!")
    }
}

// Read entries (real-time updates)
repository.getEntriesBetween(startDate, endDate)
    .collect { entries ->
        // UI updates automatically when Firestore data changes
        println("Found ${entries.size} entries")
    }
```

## Security Rules

Current rules (deployed to Firebase Console):

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
      match /{document=**} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
    }
  }
}
```

**Key Points**:
- ✅ Users can only access their own data
- ✅ Authentication required for all operations
- ✅ No cross-user data leaks possible

## File Locations

```
app/src/main/java/com/example/waiterwallet/
├── data/
│   └── firebase/
│       ├── FirestoreDailyEntry.kt      (Daily entry model)
│       ├── FirestoreJob.kt             (Job model)
│       ├── FirestoreMonthlyGoal.kt     (Goal model)
│       ├── FirestoreUserSettings.kt    (Settings model)
│       └── FirestoreRepository.kt      (Repository with CRUD)
└── ui/
    └── screens/
        └── FirebaseTestScreen.kt       (Test interface)
```

## What's Next: Phase 3

**Goal**: Hybrid Room + Firestore Architecture

**Key Components**:
1. `SyncManager` - Bi-directional sync between Room and Firestore
2. `UnifiedRepository` - Single interface for both databases
3. Offline persistence - Firestore offline mode
4. Conflict resolution - Handle simultaneous edits
5. Background sync worker - Periodic sync when online

**Estimated Time**: 5-7 days

## Troubleshooting

**Issue**: "User not authenticated"  
**Fix**: Ensure you're logged in. Restart app if needed.

**Issue**: Data not in Firebase Console  
**Fix**: Refresh Console, wait a few seconds for sync.

**Issue**: Test button does nothing  
**Fix**: Check Logcat for errors. Verify internet connection.

**Issue**: Real-time updates not working  
**Fix**: Ensure Flow is being collected, coroutine scope is active.

## Remove Test Screen Before Production

```kotlin
// MainScreen.kt - Remove this line:
BottomNavItem(Routes.FirebaseTest, "Test", Icons.Default.Build),

// MainScreen.kt - Remove this composable:
composable(Routes.FirebaseTest) {
    FirebaseTestScreen()
}

// NavGraph.kt - Remove this constant:
const val FirebaseTest = "firebase_test"
```

## Performance Metrics

**Free Tier Limits**:
- Reads: 50,000/day
- Writes: 20,000/day
- Storage: 1 GB

**Our Usage (Single User)**:
- Reads: ~500/day (1% of limit)
- Writes: ~60/day (0.3% of limit)
- Storage: <1 MB (0.1% of limit)

**Conclusion**: Well within free tier! ✅

---

**Phase 2 Status**: ✅ COMPLETE  
**Date**: November 23, 2025  
**Next**: Phase 3 - Hybrid Architecture
