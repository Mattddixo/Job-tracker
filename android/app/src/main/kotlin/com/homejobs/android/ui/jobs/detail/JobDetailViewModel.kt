package com.homejobs.android.ui.jobs.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homejobs.android.domain.repository.JobRepository
import com.homejobs.android.ui.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JobDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: JobRepository,
) : ViewModel() {

    private val jobId: Long = checkNotNull(savedStateHandle["jobId"])
    private val composer = MutableStateFlow(NoteComposerState())

    val uiState: StateFlow<JobDetailUiState> = combine(
        repository.observeJob(jobId),
        repository.observeNotes(jobId),
        composer,
    ) { job, notes, composerState ->
        JobDetailUiState(
            job = job?.let { UiState.Success(it) } ?: UiState.Empty,
            notes = notes,
            noteDraft = composerState.draft,
            pendingPhotoPaths = composerState.pendingPhotoPaths,
            isSubmittingNote = composerState.isSubmitting,
            noteError = composerState.error,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), JobDetailUiState())

    fun updateNoteDraft(text: String) {
        composer.update { it.copy(draft = text) }
    }

    fun addPendingPhoto(filePath: String) {
        composer.update { it.copy(pendingPhotoPaths = it.pendingPhotoPaths + filePath) }
    }

    fun removePendingPhoto(filePath: String) {
        composer.update { it.copy(pendingPhotoPaths = it.pendingPhotoPaths - filePath) }
    }

    fun submitNote() {
        val state = composer.value
        val body = state.draft.trim()
        if (body.isBlank() && state.pendingPhotoPaths.isEmpty()) return

        viewModelScope.launch {
            composer.update { it.copy(isSubmitting = true) }
            try {
                repository.addNote(jobId, body, state.pendingPhotoPaths)
                composer.value = NoteComposerState()
            } catch (e: Exception) {
                composer.update { it.copy(isSubmitting = false, error = e.message ?: "Failed to add note") }
            }
        }
    }

    fun deleteNote(noteId: Long) {
        viewModelScope.launch { repository.deleteNote(jobId, noteId) }
    }

    fun deletePhoto(photoId: Long) {
        viewModelScope.launch { repository.deletePhoto(photoId) }
    }

    fun deleteJob(onDeleted: () -> Unit) {
        viewModelScope.launch {
            repository.deleteJob(jobId)
            onDeleted()
        }
    }
}
