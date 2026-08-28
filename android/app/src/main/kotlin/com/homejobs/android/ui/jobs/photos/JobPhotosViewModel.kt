package com.homejobs.android.ui.jobs.photos

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homejobs.android.domain.model.Photo
import com.homejobs.android.domain.repository.JobRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class JobPhotosViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    repository: JobRepository,
) : ViewModel() {

    private val jobId: Long = checkNotNull(savedStateHandle["jobId"])

    /** Set when this screen was opened from a specific photo (e.g. "view all" from the viewer). */
    val focusPhotoId: Long? = savedStateHandle.get<String>("photoId")?.toLongOrNull()

    /** Every photo across all of this job's notes, newest first. */
    val photos: StateFlow<List<Photo>> = repository.observeNotes(jobId)
        .map { notes -> notes.flatMap { it.photos }.sortedByDescending { it.createdAt } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
