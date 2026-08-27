package com.homejobs.android.fakes

import com.homejobs.android.domain.model.Job
import com.homejobs.android.domain.model.JobFilter
import com.homejobs.android.domain.model.JobNote
import com.homejobs.android.domain.model.JobUpsertInput
import com.homejobs.android.domain.repository.JobRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeJobRepository : JobRepository {
    val jobsState = MutableStateFlow<List<Job>>(emptyList())
    val notesState = MutableStateFlow<List<JobNote>>(emptyList())

    var refreshJobsResult: Result<Unit> = Result.success(Unit)
    var refreshJobsCallCount = 0

    var createJobResult: ((JobUpsertInput) -> Result<Job>)? = null
    var deleteJobResult: Result<Unit> = Result.success(Unit)
    var deleteJobCalledWith: Long? = null

    override fun observeJobs(filter: JobFilter) = jobsState.map { jobs ->
        jobs.filter { job -> filter.status == null || job.status == filter.status }
    }

    override fun observeJob(id: Long) = jobsState.map { jobs -> jobs.firstOrNull { it.id == id } }

    override fun observeNotes(jobId: Long) = notesState.map { notes -> notes.filter { it.jobId == jobId } }

    override suspend fun refreshJobs(): Result<Unit> {
        refreshJobsCallCount++
        return refreshJobsResult
    }

    override suspend fun refreshJob(id: Long): Result<Unit> = Result.success(Unit)

    override suspend fun refreshNotes(jobId: Long): Result<Unit> = Result.success(Unit)

    override suspend fun createJob(input: JobUpsertInput): Result<Job> =
        createJobResult?.invoke(input) ?: Result.success(
            Job(
                id = 1,
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
                paymentMethod = input.paymentMethod,
                createdAt = "2026-01-01T00:00:00Z",
                updatedAt = "2026-01-01T00:00:00Z",
            ),
        )

    override suspend fun updateJob(id: Long, input: JobUpsertInput): Result<Job> = createJob(input)

    override suspend fun deleteJob(id: Long): Result<Unit> {
        deleteJobCalledWith = id
        if (deleteJobResult.isSuccess) jobsState.value = jobsState.value.filterNot { it.id == id }
        return deleteJobResult
    }

    override suspend fun addNote(jobId: Long, body: String): Result<JobNote> {
        val note = JobNote(id = (notesState.value.maxOfOrNull { it.id } ?: 0) + 1, jobId = jobId, timestamp = "2026-01-01T00:00:00Z", body = body)
        notesState.value = notesState.value + note
        return Result.success(note)
    }

    override suspend fun deleteNote(jobId: Long, noteId: Long): Result<Unit> {
        notesState.value = notesState.value.filterNot { it.id == noteId }
        return Result.success(Unit)
    }
}
