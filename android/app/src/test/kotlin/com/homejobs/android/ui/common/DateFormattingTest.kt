package com.homejobs.android.ui.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class DateFormattingTest {

    @Test
    fun `toDisplayDate formats an iso date without the year-first layout`() {
        assertEquals("Feb 10, 2026", "2026-02-10".toDisplayDate())
    }

    @Test
    fun `toDisplayDate falls back to the raw string on malformed input`() {
        assertEquals("not-a-date", "not-a-date".toDisplayDate())
    }

    @Test
    fun `toDisplayDateTime drops fractional seconds and the raw ISO separator`() {
        // Instant strings from the backend can carry however many fractional-second digits
        // Postgres happened to store; the display format must never leak them.
        val formatted = "2026-02-10T18:32:07.123456789Z".toDisplayDateTime()

        assertFalse(formatted.contains("."))
        assertFalse(formatted.contains("T"))
        assertFalse(formatted.contains("Z"))
    }

    @Test
    fun `toDisplayDateTime falls back to the raw string on malformed input`() {
        assertEquals("not-an-instant", "not-an-instant".toDisplayDateTime())
    }
}
