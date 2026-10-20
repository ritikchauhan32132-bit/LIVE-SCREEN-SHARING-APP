# Android Setup Guide — RITIK SC

This guide covers building and running RITIK SC on Windows using Android Studio or command-line tools.

## Prerequisites

1. **Operating System**: Windows 10/11 (64-bit).
2. **Java Development Kit (JDK)**: JDK 17 or JDK 21 installed and configured on your `PATH`.
   - Set `JAVA_HOME=C:\Program Files\Java\jdk-17` (or your preferred JDK 17 path).
3. **Android Studio**: Android Studio (Ladybug, Meerkat, or Hedgehog).
4. **Android SDK**:
   - `compileSdk`: 35 (Android 15)
   - `targetSdk`: 35
   - `minSdk`: 26 (Android 8.0 Oreo)
   - Build Tools 35.0.0+
5. **Physical Android Devices**:
   - Two real Android phones running Android 8.0 or newer.
   - Developer Options and USB Debugging enabled.

---

## Step 1: Open the Project

1. Launch Android Studio.
2. Select **Open** and navigate to:
   ```
   C:\Users\PC\Downloads\SS APP CREATION
   ```
3. Allow Gradle to sync dependencies.

---

## Step 2: Add Firebase Credentials

1. Follow [FIREBASE_SETUP.md](FIREBASE_SETUP.md) to obtain your `google-services.json`.
2. Copy `google-services.json` to the `app/` folder:
   ```powershell
   Copy-Item "path\to\google-services.json" -Destination "C:\Users\PC\Downloads\SS APP CREATION\app\google-services.json"
   ```

---

## Step 3: Build Debug APK via Command Line

Run the Gradle wrapper on Windows:
```cmd
cd "C:\Users\PC\Downloads\SS APP CREATION"
gradlew.bat assembleDebug
```

Output APK will be located at:
```
app\build\outputs\apk\debug\app-debug.apk
```

---

## Step 4: Run Automated Tests

To run local Android unit tests:
```cmd
gradlew.bat testDebugUnitTest
```

---

## Step 5: Install APK on Connected Devices

Using `adb`:
```cmd
# List connected devices
adb devices

# Install APK on specific device
adb -s <DEVICE_ID_1> install -r app\build\outputs\apk\debug\app-debug.apk
adb -s <DEVICE_ID_2> install -r app\build\outputs\apk\debug\app-debug.apk
```
