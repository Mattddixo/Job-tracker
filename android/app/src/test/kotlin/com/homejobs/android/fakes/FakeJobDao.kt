package com.homejobs.android.fakes

import com.homejobs.android.data.local.db.JobDao
import com.homejobs.android.data.local.db.JobEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.util.concurrent.atomic.AtomicLong

class FakeJobDao : JobDao {
    private val state = MutableStateFlow<List<JobEntity>>(emptyList())
    private val idSeq = AtomicLong(1)

    override fun observeJobs(status: String?, category: String?, location: String?) =
        state.map { jobs ->
            jobs.filter { job ->
                (status == null || job.status == status) &&
                    (category == null || job.category == category) &&
                    (location == null || job.location == location)
            }
        }

    override fun observeJob(id: Long) = state.map { jobs -> jobs.firstOrNull { it.id == id } }

    override suspend fun insert(job: JobEntity): Long {
        val id = idSeq.getAndIncrement()
        state.value = state.value + job.copy(id = id)
        return id
    }

    override suspend fun update(job: JobEntity) {
        state.value = state.value.map { if (it.id == job.id) job else it }
    }

    override suspend fun delete(id: Long) {
        state.value = state.value.filterNot { it.id == id }
    }

    fun currentJobs(): List<JobEntity> = state.value
}
