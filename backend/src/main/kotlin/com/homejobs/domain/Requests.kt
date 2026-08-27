package com.homejobs.domain

import kotlinx.serialization.Serializable

class ValidationException(val errors: List<String>) : RuntimeException(errors.joinToString("; "))

@Serializable
data class JobUpsertRequest(
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
    fun validate() {
        val errors = mutableListOf<String>()
        if (title.isBlank()) errors += "title must not be blank"
        if (title.length > 200) errors += "title must be at most 200 characters"
        quotedCost?.let { if (it < 0) errors += "quotedCost must not be negative" }
        actualCost?.let { if (it < 0) errors += "actualCost must not be negative" }
        predictedHours?.let { if (it < 0) errors += "predictedHours must not be negative" }
        actualHours?.let { if (it < 0) errors += "actualHours must not be negative" }
        listOf("scheduledDate" to scheduledDate, "completedDate" to completedDate, "warrantyExpiry" to warrantyExpiry)
            .forEach { (name, value) -> value?.let { if (!isValidIsoDate(it)) errors += "$name must be an ISO-8601 date (yyyy-MM-dd)" } }
        if (errors.isNotEmpty()) throw ValidationException(errors)
    }

    private fun isValidIsoDate(value: String): Boolean = try {
        java.time.LocalDate.parse(value)
        true
    } catch (e: java.time.format.DateTimeParseException) {
        false
    }
}

@Serializable
data class JobNoteCreateRequest(
    val body: String,
) {
    fun validate() {
        if (body.isBlank()) throw ValidationException(listOf("body must not be blank"))
        if (body.length > 5000) throw ValidationException(listOf("body must be at most 5000 characters"))
    }
}
