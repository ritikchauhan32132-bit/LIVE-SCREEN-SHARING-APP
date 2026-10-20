# Backend Setup Guide — RITIK SC

The RITIK SC backend server provides:
- Secure, rate-limited device pairing code generation
- Token verification and account ownership verification
- RFC 5766 short-lived TURN credential generation
- WebSocket signaling fallback
- Structured, privacy-conscious logging

---

## 1. Local Environment Requirements

- **Node.js**: v18.0.0 or higher (Current detected version: v24.19.0)
- **npm**: v9.0.0 or higher (Current detected version: 11.17.0)

---

## 2. Installation & Configuration

1. Navigate to the backend directory:
   ```powershell
   cd "C:\Users\PC\Downloads\SS APP CREATION\backend"
   ```

2. Install dependencies:
   ```powershell
   npm install
   ```

3. Configure environment variables:
   ```powershell
   Copy-Item .env.example .env
   ```
   Edit `.env` to supply your TURN server secret and Firebase service account path.

---

## 3. Running Automated Tests

Run the backend unit tests using Node.js's built-in test runner:
```powershell
npm test
```
This runs 8 automated test suites verifying:
- Pairing code format (8 characters, unambiguous characters)
- Randomness and entropy
- Rate-limiting enforcement (blocked after 5 attempts)
- Code expiration validation
- Replay prevention (used codes rejected)
- User ownership check
- RFC 5766 TURN HMAC-SHA1 credential calculation

---

## 4. Starting the Server

```powershell
npm start
```
The server will bind to `http://localhost:8080`.

Verify health endpoint:
```powershell
curl http://localhost:8080/health
```

Expected JSON response:
```json
{
  "status": "healthy",
  "service": "RITIK SC Backend",
  "version": "1.0.0"
}
```
