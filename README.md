# RITIK SC — Complete Android Remote Access & Remote Support Application

[![Android](https://img.shields.io/badge/Platform-Android_8.0+_(API_26)-3DDC84.svg?logo=android&logoColor=white)](https://android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF.svg?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack_Compose-Material_3-4285F4.svg?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![WebRTC](https://img.shields.io/badge/WebRTC-Live_Screen_Stream-FF5722.svg)](https://webrtc.org)
[![Firebase](https://img.shields.io/badge/Firebase-Auth_&_Firestore-FFCA28.svg?logo=firebase&logoColor=black)](https://firebase.google.com)

**RITIK SC** is a complete, production-grade Android remote support and screen sharing system built with Kotlin, Jetpack Compose, WebRTC, and Firebase. It allows two Android devices across any network (different Wi-Fi, 4G/5G mobile data, or separate locations) to securely connect and stream real-time screen video with end-to-end authorization.

---

## 📱 Two Device Roles

### 1. Controller Device
* Registers securely with the user account.
* Authorizes remote phones via **QR code scanning** or **one-time 8-character pairing codes**.
* Initiates remote support connection sessions.
* Receives and renders **genuine live 30 FPS screen frames** inside a hardware-accelerated WebRTC SurfaceView.
* Displays live connection latency, session timer, and stream diagnostics.
* Terminates sessions and revokes authorizations at any time.

### 2. Remote Support Device
* Registers securely with the user account.
* Displays a **5-minute self-expiring pairing code & dynamic QR code**.
* Receives connection requests with explicit prompt: **"Grant Consent & Start Sharing"**.
* Captures high-definition screen video using Android's official **MediaProjection API** and a **Foreground Service** (`foregroundServiceType="mediaProjection"`).
* Displays a persistent notification with a one-tap **"Stop Sharing"** button.
* Stops capture immediately when the user or controller ends the session.

---

## 🛡️ Security Architecture

* **Zero Covert Surveillance**: No hidden recording, keystroke logging, microphone spying, or secret installations.
* **Explicit User Consent**: Android MediaProjection consent required for every capture session.
* **Ephemeral Pairing Codes**: Cryptographically random 8-character codes expiring in 5 minutes with sliding-window rate limiting.
* **Account-Bound Pairing**: Devices can only be paired if authenticated to the same authorized user account.
* **Hardware Video Codecs**: Hardware-accelerated H.264/VP8 video encoding/decoding via WebRTC.
* **RFC 5766 Ephemeral TURN Tokens**: Coturn HMAC-SHA1 short-lived credentials prevent leaking master secrets.

---

## 📂 Project Architecture

```
SS APP CREATION/
├── app/
│   ├── build.gradle.kts                   # Compose BOM, WebRTC, Firebase, CameraX, ZXing
│   ├── proguard-rules.pro                 # ProGuard keep rules for WebRTC & Firebase
│   ├── src/main/
│   │   ├── AndroidManifest.xml            # MediaProjection foreground service & permissions
│   │   ├── java/com/ritiksc/remoteaccess/
│   │   │   ├── MainActivity.kt            # Edge-to-edge Compose container
│   │   │   ├── RitikScApplication.kt      # Notification channels & Firebase init
│   │   │   ├── auth/                      # Firebase Auth repository & ViewModel
│   │   │   ├── common/                    # Models, Result wrapper, formatting utils
│   │   │   ├── connection/                # WebRTC signaling, FCM messaging service
│   │   │   ├── devices/                   # Device registry, status & history
│   │   │   ├── pairing/                   # QR code generator, pairing codes & timer
│   │   │   ├── permissions/               # Centralized runtime permission manager
│   │   │   ├── settings/                  # DataStore preferences
│   │   │   ├── streaming/                 # WebRtcManager & ScreenCaptureService
│   │   │   └── ui/
│   │   │       ├── components/            # Gradient buttons, badges, error banners
│   │   │       ├── navigation/            # 20-screen Compose navigation graph
│   │   │       ├── screens/               # Splash, Welcome, Login, Dashboard, Viewer, etc.
│   │   │       └── theme/                 # Dark theme, modern blue accents, typography
│   │   └── res/                           # Vectors, colors, strings, adaptive launcher icons
│   └── src/test/java/                     # Android unit tests (UtilsTest.kt)
├── backend/
│   ├── package.json                       # Node.js backend configuration
│   ├── .env.example                       # Environment variables template
│   ├── src/
│   │   ├── server.js                      # Express API + WebSocket signaling server
│   │   ├── auth.js                        # Firebase token verification middleware
│   │   ├── pairing.js                     # Rate-limited secure pairing logic
│   │   └── turn.js                        # RFC 5766 TURN credential generator
│   └── test/
│       └── backend.test.js                # 8 passing automated unit tests
├── docs/                                  # Full setup & testing documentation
│   ├── ANDROID_SETUP.md
│   ├── FIREBASE_SETUP.md
│   ├── BACKEND_SETUP.md
│   ├── TURN_CONFIGURATION.md
│   ├── SECURITY_ARCHITECTURE.md
│   ├── TWO_DEVICES_TESTING.md
│   ├── LIMITATIONS_AND_TROUBLESHOOTING.md
│   └── COMPLETION_REPORT.md
├── firestore.rules                        # Least-privilege Firestore database rules
├── settings.gradle.kts
└── build.gradle.kts
```

---

## 🚀 Quick Start (Windows)

### 1. Backend Server
```powershell
cd backend
npm install
npm test
npm start
```

### 2. Android Studio
1. Open `c:\Users\PC\Downloads\SS APP CREATION` in **Android Studio Meerkat / Ladybug** or newer.
2. Place your `google-services.json` inside the `app/` directory (see [docs/FIREBASE_SETUP.md](docs/FIREBASE_SETUP.md)).
3. Connect your Android device or start an emulator.
4. Run `app` or build debug APK:
   ```cmd
   gradlew.bat assembleDebug
   ```

For detailed guides, refer to the documentation in [docs/](docs/).
