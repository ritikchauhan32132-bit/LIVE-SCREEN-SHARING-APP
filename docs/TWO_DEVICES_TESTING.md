# Two Real Android Phones Testing Guide — RITIK SC

This guide walks you through verifying genuine live remote support and screen sharing between two physical Android phones on separate networks.

---

## Equipment & Network Requirements

1. **Phone A (Controller Device)**: Connected to Home/Office Wi-Fi.
2. **Phone B (Remote Device)**: Connected to 4G/5G Cellular Mobile Data (NOT connected to the same Wi-Fi).
3. **Application APK**: Installed on both phones.

---

## Step-by-Step Test Procedure

### Phase 1: Authentication & Device Registration
1. Open **RITIK SC** on **Phone A**.
2. Tap **Log In** and enter your account credentials (or register a new user).
3. Open **RITIK SC** on **Phone B**.
4. Log into the **exact same account**.
5. Both phones will automatically register their unique device IDs into Firestore.

---

### Phase 2: Pairing Devices
1. On **Phone B (Remote Phone)**:
   - Tap **Pair Device** on the Dashboard.
   - Select **Display Pairing Code & QR** (Remote Device role).
   - An 8-character code (e.g. `K9W2-7MPX`) and dynamic QR code appear with a 5-minute countdown timer.
2. On **Phone A (Controller Phone)**:
   - Tap **Pair Device** on the Dashboard.
   - Select **Enter Code or Scan QR** (Controller Device role).
   - Type the 8-character code displayed on Phone B (or scan Phone B's QR code).
   - Tap **Verify & Pair Device**.
3. **Verification**:
   - Phone A returns to the Dashboard and shows Phone B under **Authorized Devices**.
   - Phone B acknowledges the pairing approval.

---

### Phase 3: Live Screen Sharing Session
1. On **Phone A (Controller)**:
   - Tap on **Phone B** under **Authorized Devices**.
   - Tap **Connect & View Screen**.
   - Status updates to: *"Awaiting approval…"*.
2. On **Phone B (Remote Phone)**:
   - A screen pops up: **"Screen Sharing Request"** showing Phone A's name.
   - Tap **Grant Consent & Start Sharing**.
   - The Android system prompt appears: *"RITIK SC will start capturing everything that's displayed on your screen"*.
   - Tap **Start now**.
3. **Verification**:
   - Phone B starts its Foreground Service and displays a persistent notification with a *"Stop Sharing"* action button.
   - Phone A opens **Live Screen Viewer**, connects WebRTC, and renders Phone B's actual screen in real-time at 30 FPS.
   - Navigate to different apps on Phone B (home screen, settings, browser); verify that Phone A shows the live movements with low latency.

---

### Phase 4: Session Termination
1. On **Phone B**: Pull down the Android notification shade and tap **Stop Sharing**.
2. **Verification**:
   - Phone B's screen capture stops immediately.
   - Phone A detects session termination, closes the WebRTC peer connection, and returns to the Dashboard.
   - Both devices record the session duration and timestamp in **Connection History**.
