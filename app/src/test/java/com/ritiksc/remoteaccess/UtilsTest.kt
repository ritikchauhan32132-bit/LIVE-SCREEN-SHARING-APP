package com.ritiksc.remoteaccess

import com.ritiksc.remoteaccess.common.CodeGenerator
import com.ritiksc.remoteaccess.common.TimeUtils
import org.junit.Assert.*
import org.junit.Test

class UtilsTest {

    @Test
    fun testPairingCodeGenerationLength() {
        val code = CodeGenerator.generatePairingCode()
        assertEquals(8, code.length)
    }

    @Test
    fun testPairingCodeCharactersExcludeAmbiguous() {
        for (i in 0 until 100) {
            val code = CodeGenerator.generatePairingCode()
            assertFalse("Code should not contain '0': $code", code.contains('0'))
            assertFalse("Code should not contain '1': $code", code.contains('1'))
            assertFalse("Code should not contain 'I': $code", code.contains('I'))
            assertFalse("Code should not contain 'O': $code", code.contains('O'))
        }
    }

    @Test
    fun testDeviceIdGeneration() {
        val id1 = CodeGenerator.generateDeviceId()
        val id2 = CodeGenerator.generateDeviceId()
        assertNotNull(id1)
        assertNotNull(id2)
        assertNotEquals(id1, id2)
    }

    @Test
    fun testFormatDuration() {
        val zeroMs = TimeUtils.formatDuration(0)
        assertEquals("0s", zeroMs)

        val thirtySec = TimeUtils.formatDuration(30_000)
        assertEquals("30s", thirtySec)

        val fiveMin = TimeUtils.formatDuration(5 * 60 * 1000)
        assertEquals("5m 0s", fiveMin)

        val oneHourTenMin = TimeUtils.formatDuration((1 * 3600 + 10 * 60) * 1000)
        assertEquals("1h 10m", oneHourTenMin)
    }

    @Test
    fun testRelativeTime() {
        val now = System.currentTimeMillis()
        val justNow = TimeUtils.formatRelativeTime(now - 10_000)
        assertEquals("Just now", justNow)

        val fiveMinAgo = TimeUtils.formatRelativeTime(now - 5 * 60_000)
        assertEquals("5m ago", fiveMinAgo)

        val twoHoursAgo = TimeUtils.formatRelativeTime(now - 2 * 3600_000)
        assertEquals("2h ago", twoHoursAgo)
    }
}
