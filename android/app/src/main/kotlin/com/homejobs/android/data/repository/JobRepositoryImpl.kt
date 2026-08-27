package com.homejobs.android.data.repository

import com.homejobs.android.data.local.db.JobDao
import com.homejobs.android.data.local.db.JobNoteDao
import com.homejobs.android.data.remote.HomeJobsApiService
import com.homejobs.android.domain.model.Job
import com.homejobs.android.domain.model.JobFilter
import com.homejobs.android.domain.model.JobNote
import com.homejobs.android.domain.model.JobSortField
import com.homejobs.android.domain.model.JobUpsertInput
import com.homejobs.android.domain.model.SortDirection
import com.homejobs.android.domain.repository.JobRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JobRepositoryImpl @Inject constructor(
    private val api: HomeJobsApiService,
    private val jobDao: JobDao,
    private val jobNoteDao: JobNoteDao,
) : JobRepository {

    override fun observeJobs(filter: JobFilter): Flow<List<Job>> =
        jobDao.observeJobs(
            status = filter.status?.name,
            category = filter.category,
            location = filter.location,
        ).map { entities -> entities.map { it.toDomain() }.sortedWith(comparator(filter)) }

    override fun observeJob(id: Long): Flow<Job?> = jobDao.observeJob(id).map { it?.toDomain() }

    override fun observeNotes(jobId: Long): Flow<List<JobNote>> =
        jobNoteDao.observeNotes(jobId).map { entities -> entities.map { it.toDomain() } }

    override suspend fun refreshJobs(): Result<Unit> = runCatching {
        val jobs = api.listJobs()
        jobDao.replaceAll(jobs.map { it.toEntity() })
    }

    override suspend fun refreshJob(id: Long): Result<Unit> = runCatching {
        val job = api.getJob(id)
        jobDao.upsert(job.toEntity())
    }

    override suspend fun refreshNotes(jobId: Long): Result<Unit> = runCatching {
        val notes = api.listNotes(jobId)
        jobNoteDao.replaceForJob(jobId, notes.map { it.toEntity() })
    }

    override suspend fun createJob(input: JobUpsertInput): Result<Job> = runCatching {
        val created = api.createJob(input.toRequestDto())
        jobDao.upsert(created.toEntity())
        created.toEntity().toDomain()
    }

    override suspend fun updateJob(id: Long, input: JobUpsertInput): Result<Job> = runCatching {
        val updated = api.updateJob(id, input.toRequestDto())
        jobDao.upsert(updated.toEntity())
        updated.toEntity().toDomain()
    }

    override suspend fun deleteJob(id: Long): Result<Unit> = runCatching {
        api.deleteJob(id)
        jobDao.delete(id)
    }

    override suspend fun addNote(jobId: Long, body: String): Result<JobNote> = runCatching {
        val created = api.addNote(jobId, body.toNoteRequestDto())
        jobNoteDao.upsert(created.toEntity())
        created.toEntity().toDomain()
    }

    override suspend fun deleteNote(jobId: Long, noteId: Long): Result<Unit> = runCatching {
        api.deleteNote(jobId, noteId)
        jobNoteDao.delete(noteId)
    }

    private fun comparator(filter: JobFilter): Comparator<Job> {
        val base: Comparator<Job> = when (filter.sortBy) {
            JobSortField.CREATED_AT -> compareBy { it.createdAt }
            JobSortField.UPDATED_AT -> compareBy { it.updatedAt }
            JobSortField.SCHEDULED_DATE -> compareBy(nullsFirst()) { it.scheduledDate }
            JobSortField.COMPLETED_DATE -> compareBy(nullsFirst()) { it.completedDate }
            JobSortField.TITLE -> compareBy { it.title.lowercase() }
            JobSortField.COST_VARIANCE -> compareBy(nullsFirst()) { it.costVariance }
            JobSortField.TIME_VARIANCE -> compareBy(nullsFirst()) { it.timeVariance }
        }
        return if (filter.sortDir == SortDirection.DESC) base.reversed() else base
    }
}
