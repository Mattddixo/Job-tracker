package com.homejobs.android.domain.model

data class JobNote(
    val id: Long,
    val jobId: Long,
    val timestamp: String,
    val body: String,
)

data class JobUpsertInput(
    val title: String,
    val category: String? = null,
    val location: String? = null,
    val vendorName: String? = null,
    val vendorContact: String? = null,
    val status: JobStatus = JobStatus.QUOTED,
    val quotedCost: Double? = null,
    val actualCost: Double? = null,
    val predictedHours: Double? = null,
    val actualHours: Double? = null,
    val scheduledDate: String? = null,
    val completedDate: String? = null,
    val warrantyExpiry: String? = null,
    val paymentStatus: PaymentStatus = PaymentStatus.UNPAID,
    val paymentMethod: String? = null,
) {
    /** Returns field-level validation errors; empty means the input is valid. */
    fun validate(): List<String> = buildList {
        if (title.isBlank()) add("Title is required")
        if (title.length > 200) add("Title must be at most 200 characters")
        if ((quotedCost ?: 0.0) < 0) add("Quoted cost must not be negative")
        if ((actualCost ?: 0.0) < 0) add("Actual cost must not be negative")
        if ((predictedHours ?: 0.0) < 0) add("Predicted hours must not be negative")
        if ((actualHours ?: 0.0) < 0) add("Actual hours must not be negative")
    }
}
