# Security Architecture & Threat Model — RITIK SC

## 1. Ethical & Platform Compliance Mandate

RITIK SC is built strictly as an authorized, transparent remote support tool. Under no circumstances does the application implement:
- Covert recording or surveillance
- Background screen capture without user consent
- Hidden installation or concealment from the app drawer
- Keylogging, credential theft, or password harvesting
- Silent activation of cameras or microphones
- Bypass of Android system consent dialogues

---

## 2. Authentication & Authorization Model

```
[ Controller Phone ]                     [ Backend Server / Firestore ]                     [ Remote Phone ]
         |                                             |                                           |
         | --- 1. Sign in with Firebase Auth --------> | <--- 1. Sign in with Firebase Auth ------ |
         |                                             |                                           |
         |                                             | <--- 2. Request 5-min Pairing Code ------ |
         |                                             |      (Returns 8-char code & QR)           |
         |                                             |                                           |
         | --- 3. Validate & Claim Pairing Code -----> |                                           |
         |     (Atomic verify ownerUid & isUsed=false) |                                           |
         |                                             |                                           |
         | --- 4. Request Support Session -----------> | ----------------------------------------> |
         |                                             |      Prompt: "Grant Consent & Share?"     |
         |                                             |                                           |
         |                                             | <--- 5. User Grants Android Consent ----- |
         |                                             |      (Starts Foreground Service)          |
         |                                             |                                           |
         | <================= 6. WebRTC DTLS-SRTP Live Video Stream =============================> |
```

---

## 3. Screen Sharing Protection Model

1. **System Permission**: Uses Android's official `MediaProjectionManager.createScreenCaptureIntent()`. The system dialogue cannot be automated, bypassed, or dismissed by code.
2. **Foreground Service Requirement**: In Android 10+ and 14+, screen capture is strictly restricted to services declared with `android:foregroundServiceType="mediaProjection"`.
3. **User-Visible Notification**: While active, Android displays a persistent, non-dismissible notification indicating screen sharing is ongoing.
4. **Hardware Flag Protection**: Android automatically renders black frames if the user opens apps with `FLAG_SECURE` (e.g., banking apps, password managers, DRM content).

---

## 4. Cryptographic Pairing Security

- **Character Set**: 32 unambiguous characters (`ABCDEFGHJKLMNPQRSTUVWXYZ23456789`), excluding visually ambiguous `0`, `1`, `I`, `O`.
- **Entropy**: $32^8 \approx 1.099 \times 10^{12}$ combinations.
- **Short TTL**: Pairing codes expire after exactly 5 minutes (300 seconds).
- **Single Use**: Once a pairing code is consumed, Firestore atomically updates `isUsed = true`. Subsequent attempts are rejected immediately.
- **Sliding-Window Rate Limiting**: Maximum 5 pairing attempts per 15-minute window per device.
