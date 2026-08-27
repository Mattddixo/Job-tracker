package com.homejobs.domain

enum class JobSortField { CREATED_AT, UPDATED_AT, SCHEDULED_DATE, COMPLETED_DATE, COST_VARIANCE, TIME_VARIANCE, TITLE }

enum class SortDirection { ASC, DESC }

data class JobFilter(
    val status: JobStatus? = null,
    val category: String? = null,
    val location: String? = null,
    val sortBy: JobSortField = JobSortField.CREATED_AT,
    val sortDir: SortDirection = SortDirection.DESC,
)
