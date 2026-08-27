package com.homejobs.domain

import kotlinx.serialization.Serializable

@Serializable
data class JobNote(
    val id: Long,
    val jobId: Long,
    val timestamp: String,
    val body: String,
)

@Serializable
data class Attachment(
    val id: Long,
    val jobId: Long,
    val fileName: String,
    val label: String?,
    val takenAt: String?,
    val createdAt: String,
)
