# Run the Android App on an Emulator (Windows PowerShell)

Use these commands from the project root unless noted otherwise.

## One-time (if not already done)
```powershell
# Accept licenses
sdkmanager --licenses

# Create an emulator (adjust name or image as desired)
avdmanager create avd -n waiterWalletApi34 -k "system-images;android-34;google_apis;x86_64" --device "pixel"
```

## Start the emulator and verify ADB
```powershell
# Start AVD
emulator -avd waiterWalletApi34

# Verify device is connected
adb devices
```
You should see an entry like `emulator-5554    device`.

If it shows `offline`, restart adb:
```powershell
adb kill-server; adb start-server
adb devices
```

## Build, install, and launch the app
```powershell
# Build (optional if you go straight to installDebug)
.\gradlew assembleDebug

# Install to the running emulator
.\gradlew installDebug

# Launch MainActivity
adb shell am start -n com.example.waiterwallet/.MainActivity
```

## Useful extras
```powershell
# Clean rebuild if you hit errors
.\gradlew clean assembleDebug

# Run UI tests (emulator must be running)
.\gradlew connectedDebugAndroidTest

# Run unit tests
.\gradlew test

# Uninstall the app
adb uninstall com.example.waiterwallet
```

## Troubleshooting
```powershell
emulator -avd waiterWalletApi34 -wipe-data
```
```powershell
adb kill-server
Remove-Item "$env:USERPROFILE\.android\adbkey*" -Force
adb start-server
adb devices
```
