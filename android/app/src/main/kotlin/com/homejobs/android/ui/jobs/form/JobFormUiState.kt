package com.homejobs.android.ui.jobs.form

import com.homejobs.android.domain.model.JobUpsertInput
import com.homejobs.android.domain.model.PaymentMethod

data class JobFormUiState(
    val input: JobUpsertInput = JobUpsertInput(title = ""),
    val paymentMethods: List<PaymentMethod> = emptyList(),
    val isEditing: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errors: List<String> = emptyList(),
    val saveError: String? = null,
)
