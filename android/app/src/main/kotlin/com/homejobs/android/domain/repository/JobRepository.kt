package com.homejobs.android.domain.repository

import com.homejobs.android.domain.model.Job
import com.homejobs.android.domain.model.JobFilter
import com.homejobs.android.domain.model.JobNote
import com.homejobs.android.domain.model.JobUpsertInput
import kotlinx.coroutines.flow.Flow

/**
 * Room is the single source of truth for reads (offline-friendly): [observeJobs]/[observeJob]/
 * [observeNotes] always return what's on-device, while the `refresh*`/write operations talk to
 * the backend and, on success, update the local cache so observers pick up the change.
 */
interface JobRepository {
    fun observeJobs(filter: JobFilter): Flow<List<Job>>
    fun observeJob(id: Long): Flow<Job?>
    fun observeNotes(jobId: Long): Flow<List<JobNote>>

    suspend fun refreshJobs(): Result<Unit>
    suspend fun refreshJob(id: Long): Result<Unit>
    suspend fun refreshNotes(jobId: Long): Result<Unit>

    suspend fun createJob(input: JobUpsertInput): Result<Job>
    suspend fun updateJob(id: Long, input: JobUpsertInput): Result<Job>
    suspend fun deleteJob(id: Long): Result<Unit>

    suspend fun addNote(jobId: Long, body: String): Result<JobNote>
    suspend fun deleteNote(jobId: Long, noteId: Long): Result<Unit>
}
