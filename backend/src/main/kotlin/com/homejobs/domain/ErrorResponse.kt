package com.homejobs.domain

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val error: String,
    val message: String,
    val details: List<String> = emptyList(),
)
