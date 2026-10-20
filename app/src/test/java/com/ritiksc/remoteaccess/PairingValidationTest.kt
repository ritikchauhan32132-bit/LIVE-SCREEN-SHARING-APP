package com.ritiksc.remoteaccess

import com.ritiksc.remoteaccess.common.CodeGenerator
import com.ritiksc.remoteaccess.common.Constants
import com.ritiksc.remoteaccess.common.model.*
import org.junit.Assert.*
import org.junit.Test

class PairingValidationTest {

    @Test
    fun testPairingCodeFormat() {
        val code = CodeGenerator.generatePairingCode()
        assertEquals(8, code.length)
        assertTrue(code.matches(Regex("^[ABCDEFGHJKLMNPQRSTUVWXYZ23456789]{8}$")))
    }

    @Test
    fun testPairingCodeModelDefaults() {
        val now = System.currentTimeMillis()
        val pairing = PairingCode(
            code = "H7K9MNP2",
            deviceId = "dev_123",
            deviceName = "Pixel 8",
            ownerUid = "uid_abc",
            createdAt = now,
            expiresAt = now + Constants.PAIRING_CODE_EXPIRY_MS,
            isUsed = false
        )

        assertEquals("H7K9MNP2", pairing.code)
        assertFalse(pairing.isUsed)
        assertTrue(pairing.expiresAt > pairing.createdAt)
    }

    @Test
    fun testSignalingMessageSerializationModel() {
        val msg = SignalingMessage(
            type = SignalingType.OFFER,
            sessionId = "sess_001",
            senderDeviceId = "dev_sender",
            targetDeviceId = "dev_receiver",
            payload = "v=0\r\no=- 123 2 IN IP4 127.0.0.1",
            timestamp = System.currentTimeMillis()
        )

        assertEquals(SignalingType.OFFER, msg.type)
        assertEquals("sess_001", msg.sessionId)
        assertTrue(msg.payload.isNotEmpty())
    }

    @Test
    fun testSessionStatusTransitions() {
        val session = ConnectionSession(
            sessionId = "sess_123",
            controllerDeviceId = "ctrl_1",
            remoteDeviceId = "rem_1",
            ownerUid = "uid_1",
            startedAt = System.currentTimeMillis(),
            status = SessionStatus.AWAITING_APPROVAL,
            isViewOnly = true
        )

        assertEquals(SessionStatus.AWAITING_APPROVAL, session.status)
        assertTrue(session.isViewOnly)
        assertTrue(session.endedAt == 0L)
    }

    @Test
    fun testConnectionHistoryDurationCalculation() {
        val start = 1700000000000L
        val end = start + (25 * 60 * 1000L) // 25 minutes
        val history = ConnectionHistory(
            sessionId = "sess_hist",
            controllerDeviceName = "Pixel 8",
            remoteDeviceName = "Galaxy S24",
            startedAt = start,
            endedAt = end,
            duration = end - start,
            wasViewOnly = true,
            endReason = "Completed by user"
        )

        assertEquals(1500000L, history.duration)
        assertEquals("Completed by user", history.endReason)
    }
}
