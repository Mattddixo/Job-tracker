package com.homejobs.android.ui.jobs.form

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    // Populated only when this form was opened via a hometracker://newjob deep link (e.g. Job
    // Jar's "Send to Job Tracker" button) — see DeepLink.kt / Routes.jobFormFromDeepLink.
    private val deepLinkTitle: String? = savedStateHandle.get<String>("title")
    private val deepLinkCategory: String? = savedStateHandle.get<String>("category")
    private val deepLinkSourceJobJarId: Long? = savedStateHandle.get<String>("sourceJobJarId")?.toLongOrNull()

    private val _uiState = MutableStateFlow(
        JobFormUiState(
            isEditing = jobId != null,
            isLoading = jobId != null,
            input = if (jobId == null && deepLinkTitle != null) {
                JobUpsertInput(
                    title = deepLinkTitle,
                    category = deepLinkCategory,
                    spawnedFromJobJarId = deepLinkSourceJobJarId,
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

    fun save(onSaved: () -> Unit) {
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
                if (id != null) repository.updateJob(id, input) else repository.createJob(input)
                onSaved()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(saveError = e.message ?: "Failed to save job")
            }
            _uiState.value = _uiState.value.copy(isSaving = false)
        }
    }
}
