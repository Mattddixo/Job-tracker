package com.homejobs.android.ui.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

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
    fun `toDisplayDateTime never shows sub-minute precision`() {
        val millis = LocalDate.of(2026, 2, 10)
            .atTime(18, 32, 7, 123_456_789)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val formatted = millis.toDisplayDateTime()

        assertFalse(formatted.contains("."))
        assertFalse(formatted.contains(":07"))
    }
}
