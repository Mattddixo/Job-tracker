package com.homejobs.android.ui.jobs.form

import com.homejobs.android.domain.model.JobUpsertInput

data class JobFormUiState(
    val input: JobUpsertInput = JobUpsertInput(title = ""),
    val isEditing: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errors: List<String> = emptyList(),
    val saveError: String? = null,
)
