package com.homejobs.android.ui.jobs.picker

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homejobs.android.domain.model.Job
import com.homejobs.android.domain.model.JobFilter
import com.homejobs.android.domain.repository.JobRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Backs the "Link to existing Job Tracker job" flow, reached via a `hometracker://pickjob`
 * deep link. [returnJobId] is the *Job Jar* job that wants a Tracker counterpart — picking a
 * job here sets that job's own [Job.linkedJobJarId] to [returnJobId], establishing the link
 * from this side; the screen itself fires the `jobjar://linked` callback so Job Jar learns
 * which Tracker job it's now linked to.
 */
@HiltViewModel
class JobPickerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: JobRepository,
) : ViewModel() {

    val returnJobId: Long = checkNotNull(savedStateHandle["returnJobId"])

    /** Only jobs not already linked to something — picking an already-linked job would silently
     * orphan its existing link. */
    val pickableJobs: StateFlow<List<Job>> = repository.observeJobs(JobFilter())
        .map { jobs -> jobs.filter { it.linkedJobJarId == null }.sortedBy { it.title.lowercase() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun linkTo(job: Job, onLinked: (Job) -> Unit) {
        viewModelScope.launch {
            repository.setLinkedJobJarId(job.id, returnJobId)
            onLinked(job)
        }
    }
}
