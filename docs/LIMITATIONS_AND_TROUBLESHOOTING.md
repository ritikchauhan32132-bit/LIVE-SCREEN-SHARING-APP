# Known Limitations & Troubleshooting Guide — RITIK SC

## 1. Known Android Platform Limitations

### A. Remote Touch Injection Restrictions
* **View-Only Mode by Default**: Under standard Android security architecture, an unprivileged user-space application cannot inject simulated touch events (`MotionEvent`) into other applications.
* **Accessibility Services**: Automated gesture dispatch requires enabling an Android `AccessibilityService`. Many device manufacturers and Google Play policies restrict this for security reasons.
* **OEM/MDM APIs**: Full universal touch control across all manufacturer devices requires platform system signing keys (KNOX, OEM system privileges) or MDM device owner management.
* **RITIK SC Design**: The system provides view-only real-time screen streaming with diagnostic inspection tools by default.

### B. DRM & Protected Content (`FLAG_SECURE`)
* When the user opens banking applications, password managers, or DRM video (Netflix, Prime Video), Android's hardware compositor displays a black rectangular region instead of video frames. This is a non-bypassable operating system security feature.

### C. Battery Optimization & Doze Mode
* If the remote device goes into deep sleep / Android Doze mode, background socket connections may be throttled. The application requests Foreground Service permissions to maintain continuous active streaming while the user is sharing.

---

## 2. Troubleshooting Guide

### Issue 1: "ICE Connection Failed" or "Waiting for remote screen frames"
- **Cause**: Both devices are behind strict symmetric NATs and direct STUN P2P packet exchange was rejected by firewalls.
- **Solution**: Configure a TURN relay server (see [TURN_CONFIGURATION.md](TURN_CONFIGURATION.md)) and verify that ports 3478 (UDP/TCP) or 5349 (TLS) are accessible.

### Issue 2: "Pairing code expired"
- **Cause**: More than 5 minutes elapsed since the code was generated on the remote device.
- **Solution**: Tap "Generate New Code" on the remote phone to obtain a fresh 5-minute code.

### Issue 3: "Rate limit exceeded"
- **Cause**: More than 5 pairing requests were generated within 15 minutes.
- **Solution**: Wait 15 minutes for the sliding window to reset.

### Issue 4: "MediaProjection Permission Denied"
- **Cause**: The remote device user tapped "Cancel" on the Android screen capture dialog.
- **Solution**: The user must explicitly tap "Start now" on the system dialog to authorize screen streaming.
