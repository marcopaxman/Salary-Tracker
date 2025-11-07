# Waiter Wallet (Salary Tracker)

Android Kotlin application to track daily turnover, tips (cash vs card), and monthly commissions for service staff.

## Quick Start

1. Install JDK 17 (Microsoft Build of OpenJDK or Eclipse Adoptium). Set `JAVA_HOME`.
2. Install Android SDK Command Line Tools. Set `ANDROID_HOME` (or `ANDROID_SDK_ROOT`). Add to `PATH`:
   - `%ANDROID_HOME%\cmdline-tools\latest\bin`
   - `%ANDROID_HOME%\platform-tools`
   - `%ANDROID_HOME%\emulator`
3. Install required SDK packages (PowerShell):
   ```powershell
   sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0" "system-images;android-34;google_apis;x86_64"
   ```
4. Create an emulator (AVD):
   ```powershell
   avdmanager create avd -n waiterWalletApi34 -k "system-images;android-34;google_apis;x86_64" --device "pixel"
   ```
5. Launch emulator:
   ```powershell
   emulator -avd waiterWalletApi34
   ```
6. Generate Gradle wrapper (first time) from project root:
   ```powershell
   gradle wrapper
   ```
7. Build & install debug APK:
   ```powershell
   .\gradlew assembleDebug
   .\gradlew installDebug
   ```
8. If using VS Code, install extensions:
   - Kotlin
   - Android for VS Code (ADB integration)
   - Gradle Extension Pack / Java Extension Pack

## Run on an Emulator

1. Start your AVD (or create one per Quick Start):
   ```powershell
   emulator -avd waiterWalletApi34
   ```
2. Confirm the emulator is visible to ADB:
   ```powershell
   adb devices
   ```
   You should see a device listed as `emulator-5554` (or similar) in the `device` state.
3. Build and install the debug build to the emulator:
   ```powershell
   .\gradlew installDebug
   ```
4. Launch the app:
   ```powershell
   adb shell am start -n com.example.waiterwallet/.MainActivity
   ```
   Alternatively, open the app from the emulator’s app drawer (look for "Waiter Wallet").

## Troubleshooting

- No devices/emulators found:
  - Ensure `%ANDROID_HOME%\platform-tools` and `%ANDROID_HOME%\emulator` are in PATH.
  - Run `adb kill-server; adb start-server` (two commands) and retry `adb devices`.
- License errors (first setup):
  ```powershell
  sdkmanager --licenses
  ```
- Build errors after changes:
  ```powershell
  .\gradlew clean assembleDebug
  ```

## Architecture Overview (Initial)

- UI: Jetpack Compose + Material3
- Data: Room (DailyEntry entity) with java.time (desugaring)
- Charts: MPAndroidChart (to be integrated in overview screen)
- Future modules: Authentication, Calendar view, Notifications, Goals

## Testing

Run unit tests:
```powershell
./gradlew test
```
Run instrumentation tests (emulator/device required):
```powershell
./gradlew connectedDebugAndroidTest
```
