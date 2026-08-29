package com.homejobs.android.ui.jobs.form

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homejobs.android.data.parsing.QuotePdfParser
import com.homejobs.android.domain.model.JobUpsertInput
import com.homejobs.android.domain.repository.JobRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class JobFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: JobRepository,
    private val quotePdfParser: QuotePdfParser,
) : ViewModel() {

    private val jobId: Long? = (savedStateHandle.get<String>("jobId"))?.toLongOrNull()

    private val _uiState = MutableStateFlow(JobFormUiState(isEditing = jobId != null, isLoading = jobId != null))
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

    /**
     * Pre-fills whichever of vendor name / vendor contact / quoted cost the PDF's text actually
     * contained, leaving every other field (and any field the parser didn't find) exactly as it
     * was — see [QuotePdfParser] and [com.homejobs.android.domain.parsing.QuoteTextParser] for why
     * those three and only those three. Reports back what it did (or that it found nothing) so a
     * confident-looking blank field isn't mistaken for "correctly empty."
     */
    fun importFromPdf(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isImportingPdf = true, pdfImportMessage = null)
            val parsed = try {
                withContext(Dispatchers.IO) { quotePdfParser.parse(uri) }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isImportingPdf = false,
                    pdfImportMessage = "Couldn't read that file as a PDF.",
                )
                return@launch
            }

            val foundFields = buildList {
                if (parsed.vendorName != null) add("vendor")
                if (parsed.vendorContact != null) add("vendor contact")
                if (parsed.quotedCost != null) add("quoted cost")
            }
            if (foundFields.isNotEmpty()) {
                updateInput { input ->
                    input.copy(
                        vendorName = parsed.vendorName ?: input.vendorName,
                        vendorContact = parsed.vendorContact ?: input.vendorContact,
                        quotedCost = parsed.quotedCost ?: input.quotedCost,
                    )
                }
            }
            _uiState.value = _uiState.value.copy(
                isImportingPdf = false,
                pdfImportMessage = if (foundFields.isEmpty()) {
                    "Couldn't find a vendor, contact, or total in that PDF — enter details manually."
                } else {
                    "Imported from PDF: ${foundFields.joinToString(", ")}."
                },
            )
        }
    }

    fun dismissPdfImportMessage() {
        _uiState.value = _uiState.value.copy(pdfImportMessage = null)
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
