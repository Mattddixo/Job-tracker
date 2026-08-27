package com.homejobs.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class JobVarianceTest {

    private fun job(
        quotedCost: Double? = null,
        actualCost: Double? = null,
        predictedHours: Double? = null,
        actualHours: Double? = null,
    ) = Job(
        id = 1,
        title = "Test job",
        category = null,
        location = null,
        vendorName = null,
        vendorContact = null,
        status = JobStatus.QUOTED,
        quotedCost = quotedCost,
        actualCost = actualCost,
        predictedHours = predictedHours,
        actualHours = actualHours,
        scheduledDate = null,
        completedDate = null,
        warrantyExpiry = null,
        paymentStatus = PaymentStatus.UNPAID,
        paymentMethod = null,
        createdAt = "2026-01-01T00:00:00Z",
        updatedAt = "2026-01-01T00:00:00Z",
    )

    @Test
    fun `cost variance is actual minus quoted`() {
        assertEquals(150.0, job(quotedCost = 500.0, actualCost = 650.0).costVariance)
    }

    @Test
    fun `cost variance is negative when actual comes in under quote`() {
        assertEquals(-50.0, job(quotedCost = 500.0, actualCost = 450.0).costVariance)
    }

    @Test
    fun `cost variance is null when either side is missing`() {
        assertNull(job(quotedCost = 500.0, actualCost = null).costVariance)
        assertNull(job(quotedCost = null, actualCost = 500.0).costVariance)
    }

    @Test
    fun `time variance is actual minus predicted hours`() {
        assertEquals(2.5, job(predictedHours = 4.0, actualHours = 6.5).timeVariance)
    }

    @Test
    fun `time variance is null when either side is missing`() {
        assertNull(job(predictedHours = 4.0, actualHours = null).timeVariance)
    }
}
