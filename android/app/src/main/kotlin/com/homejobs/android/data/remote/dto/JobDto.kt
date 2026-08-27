package com.homejobs.android.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class JobDto(
    val id: Long,
    val title: String,
    val category: String? = null,
    val location: String? = null,
    val vendorName: String? = null,
    val vendorContact: String? = null,
    val status: String,
    val quotedCost: Double? = null,
    val actualCost: Double? = null,
    val predictedHours: Double? = null,
    val actualHours: Double? = null,
    val scheduledDate: String? = null,
    val completedDate: String? = null,
    val warrantyExpiry: String? = null,
    val paymentStatus: String,
    val paymentMethod: String? = null,
    val costVariance: Double? = null,
    val timeVariance: Double? = null,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class JobUpsertRequestDto(
    val title: String,
    val category: String? = null,
    val location: String? = null,
    val vendorName: String? = null,
    val vendorContact: String? = null,
    val status: String,
    val quotedCost: Double? = null,
    val actualCost: Double? = null,
    val predictedHours: Double? = null,
    val actualHours: Double? = null,
    val scheduledDate: String? = null,
    val completedDate: String? = null,
    val warrantyExpiry: String? = null,
    val paymentStatus: String,
    val paymentMethod: String? = null,
)

@Serializable
data class JobNoteDto(
    val id: Long,
    val jobId: Long,
    val timestamp: String,
    val body: String,
)

@Serializable
data class JobNoteCreateRequestDto(
    val body: String,
)

@Serializable
data class ErrorResponseDto(
    val error: String,
    val message: String,
    val details: List<String> = emptyList(),
)
