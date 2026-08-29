package com.homejobs.android.domain.model

enum class JobStatus { QUOTED, SCHEDULED, IN_PROGRESS, DONE, CANCELLED }

enum class PaymentStatus { UNPAID, PARTIAL, PAID }

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
    val paymentMethodId: Long?,
    val createdAt: Long,
    val updatedAt: Long,
    val linkedJobJarId: Long?,
) {
    val costVariance: Double?
        get() = if (actualCost != null && quotedCost != null) actualCost - quotedCost else null

    val timeVariance: Double?
        get() = if (actualHours != null && predictedHours != null) actualHours - predictedHours else null
}

enum class JobSortField { CREATED_AT, UPDATED_AT, SCHEDULED_DATE, COMPLETED_DATE, COST_VARIANCE, TIME_VARIANCE, TITLE }

enum class SortDirection { ASC, DESC }

data class JobFilter(
    val status: JobStatus? = null,
    val category: String? = null,
    val location: String? = null,
    val sortBy: JobSortField = JobSortField.CREATED_AT,
    val sortDir: SortDirection = SortDirection.DESC,
)
