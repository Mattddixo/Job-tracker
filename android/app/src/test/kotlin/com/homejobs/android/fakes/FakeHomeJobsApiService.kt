package com.homejobs.android.fakes

import com.homejobs.android.data.remote.HomeJobsApiService
import com.homejobs.android.data.remote.dto.JobDto
import com.homejobs.android.data.remote.dto.JobNoteCreateRequestDto
import com.homejobs.android.data.remote.dto.JobNoteDto
import com.homejobs.android.data.remote.dto.JobUpsertRequestDto
import java.io.IOException
import java.util.concurrent.atomic.AtomicLong

class FakeHomeJobsApiService : HomeJobsApiService {
    private val jobs = mutableMapOf<Long, JobDto>()
    private val notes = mutableMapOf<Long, MutableList<JobNoteDto>>()
    private val jobIdSeq = AtomicLong(1)
    private val noteIdSeq = AtomicLong(1)

    var shouldFail: Boolean = false

    fun seedJob(job: JobDto) {
        jobs[job.id] = job
    }

    override suspend fun listJobs(status: String?, category: String?, location: String?): List<JobDto> {
        maybeFail()
        return jobs.values.filter { j ->
            (status == null || j.status == status) &&
                (category == null || j.category == category) &&
                (location == null || j.location == location)
        }
    }

    override suspend fun getJob(id: Long): JobDto {
        maybeFail()
        return jobs[id] ?: throw IOException("404 job $id not found")
    }

    override suspend fun createJob(request: JobUpsertRequestDto): JobDto {
        maybeFail()
        val id = jobIdSeq.getAndIncrement()
        val dto = request.toDto(id)
        jobs[id] = dto
        return dto
    }

    override suspend fun updateJob(id: Long, request: JobUpsertRequestDto): JobDto {
        maybeFail()
        val dto = request.toDto(id)
        jobs[id] = dto
        return dto
    }

    override suspend fun deleteJob(id: Long) {
        maybeFail()
        jobs.remove(id)
        notes.remove(id)
    }

    override suspend fun listNotes(jobId: Long): List<JobNoteDto> {
        maybeFail()
        return notes[jobId].orEmpty()
    }

    override suspend fun addNote(jobId: Long, request: JobNoteCreateRequestDto): JobNoteDto {
        maybeFail()
        val note = JobNoteDto(id = noteIdSeq.getAndIncrement(), jobId = jobId, timestamp = "2026-01-01T00:00:00Z", body = request.body)
        notes.getOrPut(jobId) { mutableListOf() }.add(note)
        return note
    }

    override suspend fun deleteNote(jobId: Long, noteId: Long) {
        maybeFail()
        notes[jobId]?.removeAll { it.id == noteId }
    }

    private fun maybeFail() {
        if (shouldFail) throw IOException("simulated network failure")
    }

    private fun JobUpsertRequestDto.toDto(id: Long) = JobDto(
        id = id,
        title = title,
        category = category,
        location = location,
        vendorName = vendorName,
        vendorContact = vendorContact,
        status = status,
        quotedCost = quotedCost,
        actualCost = actualCost,
        predictedHours = predictedHours,
        actualHours = actualHours,
        scheduledDate = scheduledDate,
        completedDate = completedDate,
        warrantyExpiry = warrantyExpiry,
        paymentStatus = paymentStatus,
        paymentMethod = paymentMethod,
        createdAt = "2026-01-01T00:00:00Z",
        updatedAt = "2026-01-01T00:00:00Z",
    )
}
