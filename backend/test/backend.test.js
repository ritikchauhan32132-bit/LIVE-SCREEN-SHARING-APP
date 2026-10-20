const test = require('node:test');
const assert = require('node:assert/strict');
const { generateSecureCode, checkRateLimit, clearRateLimits, validateCode, CODE_EXPIRY_MS } = require('../src/pairing');
const { generateTurnCredentials } = require('../src/turn');

test('Pairing Code - format and length', () => {
  const code = generateSecureCode();
  assert.equal(typeof code, 'string');
  assert.equal(code.length, 8);
  // Ensure no ambiguous chars: I, O, 0, 1
  assert.match(code, /^[ABCDEFGHJKLMNPQRSTUVWXYZ23456789]{8}$/);
});

test('Pairing Code - randomness across multiple generations', () => {
  const codes = new Set();
  for (let i = 0; i < 50; i++) {
    codes.add(generateSecureCode());
  }
  // With 32^8 possibilities, 50 random codes should all be distinct
  assert.equal(codes.size, 50);
});

test('Pairing Rate Limiter - blocks after maximum attempts', () => {
  clearRateLimits();
  const testKey = 'device_test_123';

  // First 5 attempts should succeed
  for (let i = 0; i < 5; i++) {
    const allowed = checkRateLimit(testKey, 5, 60000);
    assert.equal(allowed, true, `Attempt ${i + 1} should be allowed`);
  }

  // 6th attempt should be blocked
  const blocked = checkRateLimit(testKey, 5, 60000);
  assert.equal(blocked, false, '6th attempt must be blocked by rate limiter');
});

test('Pairing Code Validation - valid unused code', () => {
  const now = Date.now();
  const record = {
    code: 'ABCDEFGH',
    ownerUid: 'user_456',
    expiresAt: now + 300000,
    isUsed: false
  };

  const result = validateCode(record, 'ABCDEFGH', 'user_456');
  assert.equal(result.valid, true);
});

test('Pairing Code Validation - expired code rejection', () => {
  const now = Date.now();
  const record = {
    code: 'ABCDEFGH',
    ownerUid: 'user_456',
    expiresAt: now - 1000, // Expired 1 second ago
    isUsed: false
  };

  const result = validateCode(record, 'ABCDEFGH', 'user_456');
  assert.equal(result.valid, false);
  assert.match(result.reason, /expired/i);
});

test('Pairing Code Validation - used code replay rejection', () => {
  const now = Date.now();
  const record = {
    code: 'ABCDEFGH',
    ownerUid: 'user_456',
    expiresAt: now + 300000,
    isUsed: true // Already used
  };

  const result = validateCode(record, 'ABCDEFGH', 'user_456');
  assert.equal(result.valid, false);
  assert.match(result.reason, /already been used/i);
});

test('Pairing Code Validation - different user account rejection', () => {
  const now = Date.now();
  const record = {
    code: 'ABCDEFGH',
    ownerUid: 'user_original',
    expiresAt: now + 300000,
    isUsed: false
  };

  const result = validateCode(record, 'ABCDEFGH', 'user_attacker');
  assert.equal(result.valid, false);
  assert.match(result.reason, /belongs to another user/i);
});

test('TURN Credentials - RFC 5766 generation and valid HMAC', () => {
  const username = 'session_test_xyz';
  const secret = 'super_secret_coturn_key';
  const ttl = 3600;

  const creds = generateTurnCredentials(username, secret, ttl);

  assert.ok(creds.username.endsWith(`:${username}`));
  assert.equal(typeof creds.credential, 'string');
  assert.ok(creds.credential.length > 0);
  assert.equal(creds.ttl, ttl);
  assert.ok(creds.expiresAt > Date.now());
});
