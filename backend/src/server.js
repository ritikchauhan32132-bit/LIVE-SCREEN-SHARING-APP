require('dotenv').config();
const express = require('express');
const cors = require('cors');
const http = require('http');
const { WebSocketServer } = require('ws');

const { generateSecureCode, checkRateLimit, validateCode, CODE_EXPIRY_MS } = require('./pairing');
const { generateTurnCredentials } = require('./turn');
const { authenticateToken } = require('./auth');

const app = express();
const server = http.createServer(app);
const wss = new WebSocketServer({ server });

app.use(cors());
app.use(express.json());

// Structured privacy-conscious logger middleware
app.use((req, res, next) => {
  const start = Date.now();
  res.on('finish', () => {
    const duration = Date.now() - start;
    // Do not log auth tokens or request body payload with sensitive data
    console.log(JSON.stringify({
      timestamp: new Date().toISOString(),
      method: req.method,
      path: req.path,
      status: res.statusCode,
      durationMs: duration,
      userAgent: req.headers['user-agent'] || 'unknown'
    }));
  });
  next();
});

// Health check endpoint
app.get('/health', (req, res) => {
  res.json({
    status: 'healthy',
    service: 'RITIK SC Backend',
    timestamp: new Date().toISOString(),
    version: '1.0.0'
  });
});

// Ephemeral TURN credentials generation (RFC 5766)
app.post('/api/turn/credentials', (req, res) => {
  const secret = process.env.TURN_SERVER_SECRET || 'dev_sample_turn_secret_never_use_in_prod';
  const username = req.body.username || req.body.sessionId || 'user_' + Date.now();
  const ttl = parseInt(process.env.TURN_CREDENTIAL_TTL_SECONDS || '86400', 10);

  try {
    const creds = generateTurnCredentials(username, secret, ttl);
    res.json({
      urls: [
        process.env.TURN_SERVER_URL || 'turn:turn.ritiksc.com:3478'
      ],
      username: creds.username,
      credential: creds.credential,
      ttl: creds.ttl,
      expiresAt: creds.expiresAt
    });
  } catch (err) {
    res.status(500).json({ error: 'Failed to generate TURN credentials', message: err.message });
  }
});

// Pairing code generation endpoint
app.post('/api/pairing/generate', (req, res) => {
  const { deviceId, deviceName, uid } = req.body;

  if (!deviceId || !uid) {
    return res.status(400).json({
      error: 'Bad Request',
      message: 'deviceId and uid are required'
    });
  }

  // Rate limiting per device
  const isAllowed = checkRateLimit(`pairing_${deviceId}`);
  if (!isAllowed) {
    return res.status(429).json({
      error: 'Too Many Requests',
      message: 'Rate limit exceeded for pairing code generation. Please wait 15 minutes.'
    });
  }

  const code = generateSecureCode();
  const now = Date.now();

  res.json({
    code,
    deviceId,
    deviceName: deviceName || 'Android Device',
    ownerUid: uid,
    createdAt: now,
    expiresAt: now + CODE_EXPIRY_MS,
    isUsed: false
  });
});

// WebSocket signaling sessions: sessionId -> Set of ws clients
const activeSessions = new Map();

wss.on('connection', (ws, req) => {
  let boundSessionId = null;
  let boundDeviceId = null;

  ws.on('message', (data) => {
    try {
      const msg = JSON.parse(data.toString());

      switch (msg.type) {
        case 'JOIN_SESSION': {
          boundSessionId = msg.sessionId;
          boundDeviceId = msg.deviceId;
          if (!activeSessions.has(boundSessionId)) {
            activeSessions.set(boundSessionId, new Set());
          }
          activeSessions.get(boundSessionId).add(ws);
          ws.send(JSON.stringify({ type: 'SESSION_JOINED', sessionId: boundSessionId }));
          break;
        }

        case 'SIGNALING_OFFER':
        case 'SIGNALING_ANSWER':
        case 'SIGNALING_ICE': {
          // Relay message to other peer in the same session
          if (boundSessionId && activeSessions.has(boundSessionId)) {
            activeSessions.get(boundSessionId).forEach((client) => {
              if (client !== ws && client.readyState === ws.OPEN) {
                client.send(JSON.stringify(msg));
              }
            });
          }
          break;
        }

        case 'SESSION_END': {
          if (boundSessionId && activeSessions.has(boundSessionId)) {
            activeSessions.get(boundSessionId).forEach((client) => {
              if (client !== ws && client.readyState === ws.OPEN) {
                client.send(JSON.stringify({ type: 'SESSION_END', reason: msg.reason || 'User ended session' }));
              }
            });
            activeSessions.delete(boundSessionId);
          }
          break;
        }
      }
    } catch (err) {
      console.error('Error handling WebSocket message:', err.message);
    }
  });

  ws.on('close', () => {
    if (boundSessionId && activeSessions.has(boundSessionId)) {
      const sessionClients = activeSessions.get(boundSessionId);
      sessionClients.delete(ws);
      if (sessionClients.size === 0) {
        activeSessions.delete(boundSessionId);
      }
    }
  });
});

const PORT = process.env.PORT || 8080;

if (require.main === module) {
  server.listen(PORT, () => {
    console.log(`RITIK SC Backend Server running on port ${PORT}`);
  });
}

module.exports = { app, server };
