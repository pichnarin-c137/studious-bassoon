package com.gymapp.util

import kotlinx.datetime.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

/** UTC is stored; everything renders in Asia/Phnom_Penh (UTC+7). */
class DateTimeUtilTest {

    private fun millis(iso: String): Long = Instant.parse(iso).toEpochMilliseconds()

    @Test
    fun formatsDateAndTimeInPhnomPenh() {
        // Spec example: 03:00 UTC -> 10:00 in Cambodia.
        val t = millis("2025-06-08T03:00:00Z")
        assertEquals("08 Jun 2025", DateTimeUtil.formatDate(t))
        assertEquals("10:00 AM", DateTimeUtil.formatTime(t))
        assertEquals("08 Jun 2025, 10:00 AM", DateTimeUtil.formatDateTime(t))
        assertEquals("Expires 08 Jun 2025", DateTimeUtil.formatExpiry(t))
    }

    @Test
    fun rollsOverToNextLocalDayAfterMidnight() {
        // 17:30 UTC -> 00:30 the next day in Cambodia.
        val t = millis("2025-01-01T17:30:00Z")
        assertEquals("02 Jan 2025", DateTimeUtil.formatDate(t))
        assertEquals("12:30 AM", DateTimeUtil.formatTime(t))
    }

    @Test
    fun rendersNoonAsTwelvePm() {
        // 05:00 UTC -> 12:00 local.
        val t = millis("2025-01-01T05:00:00Z")
        assertEquals("12:00 PM", DateTimeUtil.formatTime(t))
    }
}
