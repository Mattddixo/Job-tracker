package com.homejobs.android.ui.jobs.list

import com.homejobs.android.domain.model.Job
import com.homejobs.android.domain.model.JobFilter
import com.homejobs.android.domain.model.JobStatus

/**
 * Active/Completed/All is a view-level grouping, not a server-side query: a household's job list
 * is small enough that filtering it in memory (same reasoning as the in-memory sort in
 * JobRepositoryImpl) is simpler than adding multi-status filtering to the Room query.
 */
enum class JobListTab {
    ACTIVE,
    COMPLETED,
    ALL,
    ;

    fun matches(status: JobStatus): Boolean = when (this) {
        ACTIVE -> status == JobStatus.QUOTED || status == JobStatus.SCHEDULED || status == JobStatus.IN_PROGRESS
        COMPLETED -> status == JobStatus.DONE || status == JobStatus.CANCELLED
        ALL -> true
    }

    val label: String
        get() = when (this) {
            ACTIVE -> "Active"
            COMPLETED -> "Completed"
            ALL -> "All"
        }
}

data class JobListUiState(
    val jobs: List<Job> = emptyList(),
    val filter: JobFilter = JobFilter(),
    val selectedTab: JobListTab = JobListTab.ACTIVE,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)
