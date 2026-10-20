const admin = require('firebase-admin');

// Lazy initialization of Firebase Admin
let isInitialized = false;

function initFirebase() {
  if (isInitialized) return;

  const serviceAccountPath = process.env.FIREBASE_SERVICE_ACCOUNT_PATH;
  if (serviceAccountPath && require('fs').existsSync(serviceAccountPath)) {
    const serviceAccount = require(serviceAccountPath);
    admin.initializeApp({
      credential: admin.credential.cert(serviceAccount)
    });
    isInitialized = true;
  } else if (process.env.FIREBASE_PROJECT_ID) {
    admin.initializeApp({
      projectId: process.env.FIREBASE_PROJECT_ID
    });
    isInitialized = true;
  }
}

/**
 * Express middleware to verify Firebase Authentication ID Token in Authorization header
 */
async function authenticateToken(req, res, next) {
  const authHeader = req.headers['authorization'];
  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return res.status(401).json({
      error: 'Unauthorized',
      message: 'Missing or malformed Authorization header with Bearer token'
    });
  }

  const token = authHeader.split('Bearer ')[1].trim();

  try {
    initFirebase();
    if (!isInitialized) {
      // In development mode without credentials, reject unless mock flag is set
      return res.status(503).json({
        error: 'Service Unavailable',
        message: 'Firebase Admin is not configured with service account credentials'
      });
    }

    const decodedToken = await admin.auth().verifyIdToken(token);
    req.user = decodedToken;
    next();
  } catch (error) {
    return res.status(401).json({
      error: 'Unauthorized',
      message: 'Invalid or expired authentication token',
      details: error.message
    });
  }
}

module.exports = {
  authenticateToken,
  initFirebase
};
