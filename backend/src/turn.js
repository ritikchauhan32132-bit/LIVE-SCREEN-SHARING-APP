const crypto = require('crypto');

/**
 * Generates short-lived WebRTC TURN credentials following RFC 5766 REST API standards.
 * Used by coturn and modern WebRTC turn providers.
 *
 * @param {string} username - User identifier or session ID
 * @param {string} secret - Shared secret key configured on the TURN server
 * @param {number} ttlSeconds - Time-to-live in seconds (default: 86400 / 24 hours)
 * @returns {object} { username, credential, ttl }
 */
function generateTurnCredentials(username, secret, ttlSeconds = 86400) {
  if (!username) {
    throw new Error('Username is required for TURN credential generation');
  }
  if (!secret) {
    throw new Error('TURN secret is required');
  }

  const expiryTimestamp = Math.floor(Date.now() / 1000) + ttlSeconds;
  const turnUsername = `${expiryTimestamp}:${username}`;

  const hmac = crypto.createHmac('sha1', secret);
  hmac.update(turnUsername);
  const credential = hmac.digest('base64');

  return {
    username: turnUsername,
    credential: credential,
    ttl: ttlSeconds,
    expiresAt: expiryTimestamp * 1000
  };
}

module.exports = {
  generateTurnCredentials
};
