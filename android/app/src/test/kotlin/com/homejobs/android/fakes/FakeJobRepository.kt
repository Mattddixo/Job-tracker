package com.homejobs.android.fakes

import com.homejobs.android.domain.model.Job
import com.homejobs.android.domain.model.JobFilter
import com.homejobs.android.domain.model.JobNote
import com.homejobs.android.domain.model.JobUpsertInput
import com.homejobs.android.domain.model.PaymentMethod
import com.homejobs.android.domain.model.Photo
import com.homejobs.android.domain.repository.JobRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeJobRepository : JobRepository {
    val jobsState = MutableStateFlow<List<Job>>(emptyList())
    val notesState = MutableStateFlow<List<JobNote>>(emptyList())
    val paymentMethodsState = MutableStateFlow<List<PaymentMethod>>(emptyList())

    var createJobResult: ((JobUpsertInput) -> Job)? = null
    var deleteJobCalledWith: Long? = null

    override fun observeJobs(filter: JobFilter) = jobsState.map { jobs ->
        jobs.filter { job -> filter.status == null || job.status == filter.status }
    }

    override fun observeJob(id: Long) = jobsState.map { jobs -> jobs.firstOrNull { it.id == id } }

    override fun observeNotes(jobId: Long) = notesState.map { notes -> notes.filter { it.jobId == jobId } }

    override suspend fun createJob(input: JobUpsertInput): Job =
        createJobResult?.invoke(input) ?: Job(
            id = (jobsState.value.maxOfOrNull { it.id } ?: 0) + 1,
            title = input.title,
            category = input.category,
            location = input.location,
            vendorName = input.vendorName,
            vendorContact = input.vendorContact,
            status = input.status,
            quotedCost = input.quotedCost,
            actualCost = input.actualCost,
            predictedHours = input.predictedHours,
            actualHours = input.actualHours,
            scheduledDate = input.scheduledDate,
            completedDate = input.completedDate,
            warrantyExpiry = input.warrantyExpiry,
            paymentStatus = input.paymentStatus,
            paymentMethodId = input.paymentMethodId,
            createdAt = 0L,
            updatedAt = 0L,
        ).also { jobsState.value = jobsState.value + it }

    override suspend fun updateJob(id: Long, input: JobUpsertInput): Job = createJob(input)

    override suspend fun deleteJob(id: Long) {
        deleteJobCalledWith = id
        jobsState.value = jobsState.value.filterNot { it.id == id }
    }

    override suspend fun addNote(jobId: Long, body: String, photoPaths: List<String>): JobNote {
        val photos = photoPaths.mapIndexed { index, path ->
            Photo(id = (index + 1).toLong(), noteId = 0, filePath = path, createdAt = 0L)
        }
        val note = JobNote(id = (notesState.value.maxOfOrNull { it.id } ?: 0) + 1, jobId = jobId, timestamp = 0L, body = body, photos = photos)
        notesState.value = notesState.value + note
        return note
    }

    override suspend fun updateNote(noteId: Long, body: String) {
        notesState.value = notesState.value.map { if (it.id == noteId) it.copy(body = body) else it }
    }

    override suspend fun deleteNote(jobId: Long, noteId: Long) {
        notesState.value = notesState.value.filterNot { it.id == noteId }
    }

    override suspend fun addPhotoToNote(noteId: Long, filePath: String) {
        notesState.value = notesState.value.map { note ->
            if (note.id == noteId) {
                note.copy(photos = note.photos + Photo(id = (note.photos.maxOfOrNull { it.id } ?: 0) + 1, noteId = noteId, filePath = filePath, createdAt = 0L))
            } else {
                note
            }
        }
    }

    override suspend fun deletePhoto(photoId: Long) {
        notesState.value = notesState.value.map { note -> note.copy(photos = note.photos.filterNot { it.id == photoId }) }
    }

    override fun observePaymentMethods() = paymentMethodsState.map { it }

    override suspend fun addPaymentMethod(name: String, maxCredit: Double?): PaymentMethod {
        val method = PaymentMethod(id = (paymentMethodsState.value.maxOfOrNull { it.id } ?: 0) + 1, name = name, maxCredit = maxCredit)
        paymentMethodsState.value = paymentMethodsState.value + method
        return method
    }

    override suspend fun updatePaymentMethod(id: Long, name: String, maxCredit: Double?) {
        paymentMethodsState.value = paymentMethodsState.value.map {
            if (it.id == id) it.copy(name = name, maxCredit = maxCredit) else it
        }
    }

    override suspend fun deletePaymentMethod(id: Long) {
        paymentMethodsState.value = paymentMethodsState.value.filterNot { it.id == id }
        jobsState.value = jobsState.value.map { if (it.paymentMethodId == id) it.copy(paymentMethodId = null) else it }
    }
}
