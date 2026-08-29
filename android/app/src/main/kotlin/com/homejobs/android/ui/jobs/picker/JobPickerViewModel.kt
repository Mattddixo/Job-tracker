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

    /**
     * Every job, sorted by title — deliberately NOT filtered down to only-unlinked. The two
     * sides of a link can end up desynced (e.g. one side's own copy of the link got cleared by
     * a bug, or a job on either side was deleted and its id later reused) with no way for either
     * app to detect that on its own — the only reliable fix is to just let the user re-pick and
     * re-establish the correct pairing directly, rather than hard-blocking a job because ITS OWN
     * possibly-stale record claims it's "already" linked to something. Picking any job here always
     * overwrites its previous linkedJobJarId, whatever that was.
     */
    val pickableJobs: StateFlow<List<Job>> = repository.observeJobs(JobFilter())
        .map { jobs -> jobs.sortedBy { it.title.lowercase() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun linkTo(job: Job, onLinked: (Job) -> Unit) {
        viewModelScope.launch {
            repository.setLinkedJobJarId(job.id, returnJobId)
            onLinked(job)
        }
    }
}
