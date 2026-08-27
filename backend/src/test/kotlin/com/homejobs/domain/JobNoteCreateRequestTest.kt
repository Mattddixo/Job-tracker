package com.homejobs.domain

import kotlin.test.Test
import kotlin.test.assertFailsWith

class JobNoteCreateRequestTest {

    @Test
    fun `valid body does not throw`() {
        JobNoteCreateRequest("Contractor arrived on time.").validate()
    }

    @Test
    fun `blank body is rejected`() {
        assertFailsWith<ValidationException> { JobNoteCreateRequest("   ").validate() }
    }

    @Test
    fun `body over 5000 chars is rejected`() {
        assertFailsWith<ValidationException> { JobNoteCreateRequest("x".repeat(5001)).validate() }
    }
}
