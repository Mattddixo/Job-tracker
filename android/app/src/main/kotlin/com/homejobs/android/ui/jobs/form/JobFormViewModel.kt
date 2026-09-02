package com.homejobs.android.ui.jobs.form

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homejobs.android.domain.model.Job
import com.homejobs.android.domain.model.JobStatus
import com.homejobs.android.domain.model.JobUpsertInput
import com.homejobs.android.domain.repository.JobRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JobFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: JobRepository,
) : ViewModel() {

    private val jobId: Long? = (savedStateHandle.get<String>("jobId"))?.toLongOrNull()

    // The status this job had when it was loaded for editing, before any changes made in this
    // session - used by save() to tell whether *this* save is what just moved it into DONE,
    // rather than it having already been DONE going in (see justCompletedLinkedJobJarId).
    private var originalStatus: JobStatus? = null

    // Populated only when this form was opened via a hometracker://newjob deep link (e.g. Job
    // Jar's "Send to Job Tracker" button) — see DeepLink.kt / Routes.jobFormFromDeepLink.
    private val deepLinkTitle: String? = savedStateHandle.get<String>("title")
    private val deepLinkCategory: String? = savedStateHandle.get<String>("category")
    private val deepLinkSourceJobJarId: Long? = savedStateHandle.get<String>("sourceJobJarId")?.toLongOrNull()

    // Job Jar's estimatedMinutes converted to this app's hours unit, and its scheduledDate
    // (already an ISO "yyyy-MM-dd" string on that side) passed straight through — both optional,
    // present only when Job Jar had them set on the job being sent.
    private val deepLinkPredictedHours: Double? =
        savedStateHandle.get<String>("estimatedMinutes")?.toIntOrNull()?.let { it / 60.0 }
    private val deepLinkScheduledDate: String? = savedStateHandle.get<String>("scheduledDate")

    private val _uiState = MutableStateFlow(
        JobFormUiState(
            isEditing = jobId != null,
            isLoading = jobId != null,
            input = if (jobId == null && deepLinkTitle != null) {
                JobUpsertInput(
                    title = deepLinkTitle,
                    category = deepLinkCategory,
                    linkedJobJarId = deepLinkSourceJobJarId,
                    predictedHours = deepLinkPredictedHours,
                    scheduledDate = deepLinkScheduledDate,
                )
            } else {
                JobUpsertInput(title = "")
            },
        ),
    )
    val uiState: StateFlow<JobFormUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observePaymentMethods().collect { methods ->
                _uiState.value = _uiState.value.copy(paymentMethods = methods)
            }
        }

        val id = jobId
        if (id != null) {
            viewModelScope.launch {
                val job = repository.observeJob(id).filterNotNull().first()
                originalStatus = job.status
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    input = JobUpsertInput(
                        title = job.title,
                        category = job.category,
                        location = job.location,
                        vendorName = job.vendorName,
                        vendorContact = job.vendorContact,
                        status = job.status,
                        quotedCost = job.quotedCost,
                        actualCost = job.actualCost,
                        predictedHours = job.predictedHours,
                        actualHours = job.actualHours,
                        scheduledDate = job.scheduledDate,
                        completedDate = job.completedDate,
                        warrantyExpiry = job.warrantyExpiry,
                        paymentStatus = job.paymentStatus,
                        paymentMethodId = job.paymentMethodId,
                        linkedJobJarId = job.linkedJobJarId,
                    ),
                )
            }
        }
    }

    fun updateInput(transform: (JobUpsertInput) -> JobUpsertInput) {
        _uiState.value = _uiState.value.copy(input = transform(_uiState.value.input), errors = emptyList())
    }

    /** Creates a new payment method and immediately selects it on the job being edited. */
    fun createPaymentMethod(name: String, maxCredit: Double?) {
        viewModelScope.launch {
            val method = repository.addPaymentMethod(name, maxCredit)
            updateInput { it.copy(paymentMethodId = method.id) }
        }
    }

    /**
     * [onResult] gets the saved job, whether this was a fresh create (vs. editing an existing
     * one), and — only when this exact save is what just moved a *linked* job into
     * [JobStatus.DONE] — that Job Jar id, so the screen can nudge "want to update the linked Job
     * Jar task?" without also firing on a save that leaves an already-done job alone or that
     * changes some other field on it. [isNewlyCreated] separately gates the `jobjar://linked`
     * return callback (only ever on the create that just established a link).
     */
    fun save(onResult: (job: Job, isNewlyCreated: Boolean, justCompletedLinkedJobJarId: Long?) -> Unit) {
        val input = _uiState.value.input
        val errors = input.validate()
        if (errors.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(errors = errors)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, saveError = null)
            try {
                val id = jobId
                val wasDone = originalStatus == JobStatus.DONE
                val savedJob = if (id != null) repository.updateJob(id, input) else repository.createJob(input)
                val justCompletedLinkedJobJarId = if (!wasDone && savedJob.status == JobStatus.DONE) {
                    savedJob.linkedJobJarId
                } else {
                    null
                }
                onResult(savedJob, id == null, justCompletedLinkedJobJarId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(saveError = e.message ?: "Failed to save job")
            }
            _uiState.value = _uiState.value.copy(isSaving = false)
        }
    }
}
