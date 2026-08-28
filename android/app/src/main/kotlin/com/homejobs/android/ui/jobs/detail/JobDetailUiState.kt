package com.homejobs.android.ui.jobs.detail

import com.homejobs.android.domain.model.Job
import com.homejobs.android.domain.model.JobNote
import com.homejobs.android.domain.model.PaymentMethod
import com.homejobs.android.ui.common.UiState

data class JobDetailUiState(
    val job: UiState<Job> = UiState.Loading,
    val notes: List<JobNote> = emptyList(),
    val paymentMethods: List<PaymentMethod> = emptyList(),
    val noteDraft: String = "",
    val pendingPhotoPaths: List<String> = emptyList(),
    val isSubmittingNote: Boolean = false,
    val noteError: String? = null,
    val editingNoteId: Long? = null,
    val editingNoteDraft: String = "",
    val isSavingEditedNote: Boolean = false,
    val editNoteError: String? = null,
)

internal data class NoteComposerState(
    val draft: String = "",
    val pendingPhotoPaths: List<String> = emptyList(),
    val isSubmitting: Boolean = false,
    val error: String? = null,
)

internal data class NoteEditState(
    val noteId: Long,
    val draft: String,
    val isSubmitting: Boolean = false,
    val error: String? = null,
)
