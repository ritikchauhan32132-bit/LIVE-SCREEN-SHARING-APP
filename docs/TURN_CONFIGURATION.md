# TURN Server Configuration Guide — RITIK SC

## Why TURN is Required

In WebRTC, **STUN** (Session Traversal Utilities for NAT) allows peers to discover their public IP and port mappings. When both devices are behind symmetric NATs (standard in cellular mobile networks like 4G/5G or restrictive corporate Wi-Fi), direct P2P packet exchange is blocked.

A **TURN** (Traversal Using Relays around NAT) server acts as an encrypted media relay. WebRTC automatically falls back to TURN when direct peer-to-peer connectivity fails.

---

## Deploying Coturn on Linux (Ubuntu/Debian)

### 1. Install Coturn
```bash
sudo apt update
sudo apt install -y coturn
```

### 2. Configure Coturn (`/etc/turnserver.conf`)
Edit the configuration file:
```ini
# Listening ports
listening-port=3478
tls-listening-port=5349

# Public IP address of the server
external-ip=YOUR_SERVER_PUBLIC_IP

# Realm and static auth secret for RFC 5766 REST API
realm=ritiksc.com
use-auth-secret
static-auth-secret=YOUR_STRONG_RANDOM_SECRET_KEY

# Certificate configuration for TLS (recommended)
cert=/etc/letsencrypt/live/turn.ritiksc.com/fullchain.pem
pkey=/etc/letsencrypt/live/turn.ritiksc.com/privkey.pem

# Security restrictions
no-tcp-relay
no-multicast
no-loopback-peers
```

### 3. Enable and Start the Service
In `/etc/default/coturn`, set:
```
TURNSERVER_ENABLED=1
```
Start the service:
```bash
sudo systemctl restart coturn
sudo systemctl enable coturn
```

---

## 3. Integrating with RITIK SC Backend

In `backend/.env`, set:
```env
TURN_SERVER_URL=turn:turn.ritiksc.com:3478
TURN_SERVER_SECRET=YOUR_STRONG_RANDOM_SECRET_KEY
```

The backend server will dynamically calculate short-lived HMAC-SHA1 tokens according to RFC 5766:
- Username: `<timestamp>:<deviceId>`
- Password: `base64(hmac_sha1(static_auth_secret, username))`

This prevents leaking long-term credentials inside client APKs.
