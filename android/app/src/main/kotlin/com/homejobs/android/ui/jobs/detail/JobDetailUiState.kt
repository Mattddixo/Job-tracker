package com.homejobs.android.ui.jobs.detail

import com.homejobs.android.domain.model.Job
import com.homejobs.android.domain.model.JobNote
import com.homejobs.android.ui.common.UiState

data class JobDetailUiState(
    val job: UiState<Job> = UiState.Loading,
    val notes: List<JobNote> = emptyList(),
    val noteDraft: String = "",
    val isSubmittingNote: Boolean = false,
    val noteError: String? = null,
)
