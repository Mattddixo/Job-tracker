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
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JobDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: JobRepository,
) : ViewModel() {

    private val jobId: Long = checkNotNull(savedStateHandle["jobId"])

    private val noteDraft = MutableStateFlow("")
    private val isSubmittingNote = MutableStateFlow(false)
    private val noteError = MutableStateFlow<String?>(null)
    private val jobState = MutableStateFlow<UiState<com.homejobs.android.domain.model.Job>>(UiState.Loading)

    val uiState: StateFlow<JobDetailUiState> = combine(
        jobState,
        repository.observeNotes(jobId),
        noteDraft,
        isSubmittingNote,
        noteError,
    ) { job, notes, draft, submitting, error ->
        JobDetailUiState(job = job, notes = notes, noteDraft = draft, isSubmittingNote = submitting, noteError = error)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), JobDetailUiState())

    init {
        viewModelScope.launch {
            repository.observeJob(jobId).collect { job ->
                jobState.value = job?.let { UiState.Success(it) } ?: UiState.Empty
            }
        }
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            repository.refreshJob(jobId).onFailure { error ->
                if (jobState.value !is UiState.Success) {
                    jobState.value = UiState.Error(error.message ?: "Failed to load job")
                }
            }
            repository.refreshNotes(jobId)
        }
    }

    fun updateNoteDraft(text: String) {
        noteDraft.value = text
    }

    fun submitNote() {
        val body = noteDraft.value.trim()
        if (body.isBlank()) return
        viewModelScope.launch {
            isSubmittingNote.value = true
            repository.addNote(jobId, body)
                .onSuccess {
                    noteDraft.value = ""
                    noteError.value = null
                }
                .onFailure { noteError.value = it.message ?: "Failed to add note" }
            isSubmittingNote.value = false
        }
    }

    fun deleteNote(noteId: Long) {
        viewModelScope.launch {
            repository.deleteNote(jobId, noteId).onFailure { noteError.value = it.message ?: "Failed to delete note" }
        }
    }

    fun deleteJob(onDeleted: () -> Unit) {
        viewModelScope.launch {
            repository.deleteJob(jobId).onSuccess { onDeleted() }
        }
    }
}
