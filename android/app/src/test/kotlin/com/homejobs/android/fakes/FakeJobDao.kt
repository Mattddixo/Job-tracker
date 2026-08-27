package com.homejobs.android.fakes

import com.homejobs.android.data.local.db.JobDao
import com.homejobs.android.data.local.db.JobEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeJobDao : JobDao {
    private val state = MutableStateFlow<List<JobEntity>>(emptyList())

    override fun observeJobs(status: String?, category: String?, location: String?) =
        state.map { jobs ->
            jobs.filter { job ->
                (status == null || job.status == status) &&
                    (category == null || job.category == category) &&
                    (location == null || job.location == location)
            }
        }

    override fun observeJob(id: Long) = state.map { jobs -> jobs.firstOrNull { it.id == id } }

    override suspend fun upsert(job: JobEntity) {
        state.value = state.value.filterNot { it.id == job.id } + job
    }

    override suspend fun upsertAll(jobs: List<JobEntity>) {
        val ids = jobs.map { it.id }.toSet()
        state.value = state.value.filterNot { it.id in ids } + jobs
    }

    override suspend fun delete(id: Long) {
        state.value = state.value.filterNot { it.id == id }
    }

    override suspend fun clearAll() {
        state.value = emptyList()
    }

    override suspend fun deleteNotIn(keepIds: List<Long>) {
        state.value = state.value.filter { it.id in keepIds }
    }

    override suspend fun replaceAll(jobs: List<JobEntity>) {
        deleteNotIn(jobs.map { it.id })
        upsertAll(jobs)
    }

    fun currentJobs(): List<JobEntity> = state.value
}
