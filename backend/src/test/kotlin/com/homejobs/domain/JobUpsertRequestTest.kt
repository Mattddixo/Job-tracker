package com.homejobs.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class JobUpsertRequestTest {

    private fun validRequest(overrides: JobUpsertRequest.() -> JobUpsertRequest = { this }) =
        JobUpsertRequest(title = "Fix leaky faucet").let(overrides)

    @Test
    fun `valid request does not throw`() {
        validRequest().validate()
    }

    @Test
    fun `blank title is rejected`() {
        val ex = assertFailsWith<ValidationException> {
            validRequest { copy(title = "   ") }.validate()
        }
        assert(ex.errors.any { it.contains("title") })
    }

    @Test
    fun `title over 200 chars is rejected`() {
        val ex = assertFailsWith<ValidationException> {
            validRequest { copy(title = "x".repeat(201)) }.validate()
        }
        assert(ex.errors.any { it.contains("200") })
    }

    @Test
    fun `negative quoted cost is rejected`() {
        assertFailsWith<ValidationException> {
            validRequest { copy(quotedCost = -1.0) }.validate()
        }
    }

    @Test
    fun `negative actual hours is rejected`() {
        assertFailsWith<ValidationException> {
            validRequest { copy(actualHours = -0.5) }.validate()
        }
    }

    @Test
    fun `malformed date is rejected`() {
        val ex = assertFailsWith<ValidationException> {
            validRequest { copy(scheduledDate = "not-a-date") }.validate()
        }
        assert(ex.errors.any { it.contains("scheduledDate") })
    }

    @Test
    fun `valid iso date passes`() {
        validRequest { copy(scheduledDate = "2026-01-15") }.validate()
    }

    @Test
    fun `multiple errors are all reported`() {
        val ex = assertFailsWith<ValidationException> {
            validRequest { copy(title = "", quotedCost = -5.0, actualHours = -1.0) }.validate()
        }
        assertEquals(3, ex.errors.size)
    }
}
