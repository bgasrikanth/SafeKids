package com.example

import com.example.data.MockAppsCatalog
import com.example.util.SecurityUtils
import com.example.util.TimeUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun sha256_hashesPinDeterministicallyWithoutPlainText() {
        val hash1 = SecurityUtils.sha256("1234")
        val hash2 = SecurityUtils.sha256("1234")
        val hashOther = SecurityUtils.sha256("5678")

        assertEquals(64, hash1.length)
        assertEquals(hash1, hash2)
        assertNotEquals(hash1, hashOther)
        assertFalse(hash1.contains("1234"))
    }

    @Test
    fun recoveryAnswer_normalizesCaseAndWhitespace() {
        val h1 = SecurityUtils.hashRecoveryAnswer("  Buddy ")
        val h2 = SecurityUtils.hashRecoveryAnswer("buddy")
        assertEquals(h1, h2)
    }

    @Test
    fun bedtime_handlesOvernightAndSameDayWindows() {
        // Overnight: 8:00 PM (1200) to 7:00 AM (420)
        assertTrue(TimeUtils.isWithinBedtime(21 * 60, 20 * 60, 7 * 60))
        assertTrue(TimeUtils.isWithinBedtime(2 * 60, 20 * 60, 7 * 60))
        assertFalse(TimeUtils.isWithinBedtime(14 * 60, 20 * 60, 7 * 60))
        assertFalse(TimeUtils.isWithinBedtime(7 * 60, 20 * 60, 7 * 60))

        // Same-day: 1:00 PM (780) to 3:00 PM (900)
        assertTrue(TimeUtils.isWithinBedtime(14 * 60, 13 * 60, 15 * 60))
        assertFalse(TimeUtils.isWithinBedtime(16 * 60, 13 * 60, 15 * 60))
    }

    @Test
    fun mockAppsCatalog_containsAll16ExpectedApps() {
        assertEquals(16, MockAppsCatalog.allApps.size)
        assertEquals(8, MockAppsCatalog.defaultAllowedAppIds.size)
    }
}
