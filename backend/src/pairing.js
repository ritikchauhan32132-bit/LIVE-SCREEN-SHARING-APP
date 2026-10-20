const crypto = require('crypto');

const PAIRING_CHARS = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789'; // Unambiguous chars
const CODE_LENGTH = 8;
const CODE_EXPIRY_MS = 5 * 60 * 1000; // 5 minutes

// In-memory rate limiting store: key -> array of timestamps
const rateLimitStore = new Map();

/**
 * Generate a cryptographically secure 8-character code
 */
function generateSecureCode(length = CODE_LENGTH) {
  const bytes = crypto.randomBytes(length);
  let code = '';
  for (let i = 0; i < length; i++) {
    code += PAIRING_CHARS[bytes[i] % PAIRING_CHARS.length];
  }
  return code;
}

/**
 * Checks and records rate limit for a key (e.g., deviceId or IP)
 * @returns {boolean} true if allowed, false if rate limited
 */
function checkRateLimit(key, maxAttempts = 5, windowMs = 15 * 60 * 1000) {
  const now = Date.now();
  const timestamps = rateLimitStore.get(key) || [];
  
  // Filter out timestamps outside window
  const activeTimestamps = timestamps.filter(ts => now - ts < windowMs);

  if (activeTimestamps.length >= maxAttempts) {
    rateLimitStore.set(key, activeTimestamps);
    return false;
  }

  activeTimestamps.push(now);
  rateLimitStore.set(key, activeTimestamps);
  return true;
}

/**
 * Clears rate limit store (used in testing)
 */
function clearRateLimits() {
  rateLimitStore.clear();
}

/**
 * Validates pairing code format and expiry
 */
function validateCode(codeRecord, inputCode, currentUid) {
  if (!codeRecord) {
    return { valid: false, reason: 'Pairing code not found' };
  }

  const now = Date.now();
  if (codeRecord.isUsed) {
    return { valid: false, reason: 'This pairing code has already been used' };
  }

  if (codeRecord.expiresAt < now) {
    return { valid: false, reason: 'This pairing code has expired' };
  }

  if (codeRecord.code.toUpperCase() !== inputCode.toUpperCase().trim()) {
    return { valid: false, reason: 'Invalid code' };
  }

  if (codeRecord.ownerUid !== currentUid) {
    return { valid: false, reason: 'Pairing code belongs to another user account' };
  }

  return { valid: true };
}

module.exports = {
  generateSecureCode,
  checkRateLimit,
  clearRateLimits,
  validateCode,
  CODE_EXPIRY_MS
};
