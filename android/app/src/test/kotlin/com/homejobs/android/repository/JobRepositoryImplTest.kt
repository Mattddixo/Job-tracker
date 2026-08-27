package com.homejobs.android.repository

import app.cash.turbine.test
import com.homejobs.android.data.remote.dto.JobDto
import com.homejobs.android.data.repository.JobRepositoryImpl
import com.homejobs.android.domain.model.JobFilter
import com.homejobs.android.domain.model.JobStatus
import com.homejobs.android.domain.model.JobUpsertInput
import com.homejobs.android.fakes.FakeHomeJobsApiService
import com.homejobs.android.fakes.FakeJobDao
import com.homejobs.android.fakes.FakeJobNoteDao
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class JobRepositoryImplTest {

    private lateinit var api: FakeHomeJobsApiService
    private lateinit var jobDao: FakeJobDao
    private lateinit var jobNoteDao: FakeJobNoteDao
    private lateinit var repository: JobRepositoryImpl

    @Before
    fun setUp() {
        api = FakeHomeJobsApiService()
        jobDao = FakeJobDao()
        jobNoteDao = FakeJobNoteDao()
        repository = JobRepositoryImpl(api, jobDao, jobNoteDao)
    }

    private fun sampleDto(id: Long, title: String, status: String = "QUOTED", quoted: Double? = 100.0, actual: Double? = null) = JobDto(
        id = id,
        title = title,
        category = "Plumbing",
        location = null,
        vendorName = null,
        vendorContact = null,
        status = status,
        quotedCost = quoted,
        actualCost = actual,
        predictedHours = null,
        actualHours = null,
        scheduledDate = null,
        completedDate = null,
        warrantyExpiry = null,
        paymentStatus = "UNPAID",
        paymentMethod = null,
        createdAt = "2026-01-01T00:00:00Z",
        updatedAt = "2026-01-01T00:00:00Z",
    )

    @Test
    fun `refreshJobs populates the local cache from the network`() = runTest {
        api.seedJob(sampleDto(1, "Fix roof"))
        api.seedJob(sampleDto(2, "Paint fence"))

        val result = repository.refreshJobs()

        assertTrue(result.isSuccess)
        repository.observeJobs(JobFilter()).test {
            assertEquals(2, awaitItem().size)
        }
    }

    @Test
    fun `refreshJobs failure surfaces as Result failure and leaves cache untouched`() = runTest {
        api.seedJob(sampleDto(1, "Fix roof"))
        repository.refreshJobs()
        api.shouldFail = true

        val result = repository.refreshJobs()

        assertTrue(result.isFailure)
        repository.observeJobs(JobFilter()).test {
            assertEquals(1, awaitItem().size) // stale cache preserved
        }
    }

    @Test
    fun `refreshJobs removes jobs that no longer exist server-side`() = runTest {
        api.seedJob(sampleDto(1, "Fix roof"))
        api.seedJob(sampleDto(2, "Paint fence"))
        repository.refreshJobs()

        // Simulate job 2 having been deleted on the server between syncs.
        val onlyJobOne = FakeHomeJobsApiService().apply { seedJob(sampleDto(1, "Fix roof")) }
        val repoWithFreshApi = JobRepositoryImpl(onlyJobOne, jobDao, jobNoteDao)
        repoWithFreshApi.refreshJobs()

        repository.observeJobs(JobFilter()).test {
            assertEquals(1, awaitItem().size)
        }
    }

    @Test
    fun `createJob sends request and caches the created job`() = runTest {
        val result = repository.createJob(JobUpsertInput(title = "New job", status = JobStatus.QUOTED))

        assertTrue(result.isSuccess)
        val job = result.getOrThrow()
        assertEquals("New job", job.title)
        repository.observeJob(job.id).test {
            assertEquals("New job", awaitItem()?.title)
        }
    }

    @Test
    fun `deleteJob removes from cache and network`() = runTest {
        val created = repository.createJob(JobUpsertInput(title = "To delete")).getOrThrow()

        val result = repository.deleteJob(created.id)

        assertTrue(result.isSuccess)
        repository.observeJob(created.id).test {
            assertNull(awaitItem())
        }
    }

    @Test
    fun `addNote and observeNotes round-trip`() = runTest {
        val job = repository.createJob(JobUpsertInput(title = "Job with notes")).getOrThrow()

        repository.addNote(job.id, "Contractor called").getOrThrow()

        repository.observeNotes(job.id).test {
            val notes = awaitItem()
            assertEquals(1, notes.size)
            assertEquals("Contractor called", notes.single().body)
        }
    }

    @Test
    fun `observeJobs filters by status`() = runTest {
        api.seedJob(sampleDto(1, "Fix roof", status = "DONE"))
        api.seedJob(sampleDto(2, "Paint fence", status = "QUOTED"))
        repository.refreshJobs()

        repository.observeJobs(JobFilter(status = JobStatus.DONE)).test {
            val jobs = awaitItem()
            assertEquals(1, jobs.size)
            assertEquals("Fix roof", jobs.single().title)
        }
    }
}
