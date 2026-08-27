package com.homejobs.domain

import kotlinx.serialization.Serializable

@Serializable
enum class JobStatus {
    QUOTED, SCHEDULED, IN_PROGRESS, DONE, CANCELLED;

    companion object {
        fun fromDb(value: String): JobStatus = valueOf(value.uppercase())
        fun JobStatus.toDb(): String = name.lowercase()
    }
}

@Serializable
enum class PaymentStatus {
    UNPAID, PARTIAL, PAID;

    companion object {
        fun fromDb(value: String): PaymentStatus = valueOf(value.uppercase())
        fun PaymentStatus.toDb(): String = name.lowercase()
    }
}

/**
 * A household job/project. costVariance and timeVariance are always derived
 * (actual - quoted/predicted), never stored, so they can never drift out of sync.
 */
@Serializable
data class Job(
    val id: Long,
    val title: String,
    val category: String?,
    val location: String?,
    val vendorName: String?,
    val vendorContact: String?,
    val status: JobStatus,
    val quotedCost: Double?,
    val actualCost: Double?,
    val predictedHours: Double?,
    val actualHours: Double?,
    val scheduledDate: String?,
    val completedDate: String?,
    val warrantyExpiry: String?,
    val paymentStatus: PaymentStatus,
    val paymentMethod: String?,
    val createdAt: String,
    val updatedAt: String,
) {
    val costVariance: Double?
        get() = if (actualCost != null && quotedCost != null) actualCost - quotedCost else null

    val timeVariance: Double?
        get() = if (actualHours != null && predictedHours != null) actualHours - predictedHours else null
}
