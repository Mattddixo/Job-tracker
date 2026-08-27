package com.homejobs.android.viewmodel

import app.cash.turbine.test
import com.homejobs.android.MainDispatcherRule
import com.homejobs.android.domain.model.Job
import com.homejobs.android.domain.model.JobFilter
import com.homejobs.android.domain.model.JobStatus
import com.homejobs.android.domain.model.PaymentStatus
import com.homejobs.android.fakes.FakeJobRepository
import com.homejobs.android.ui.jobs.list.JobListViewModel
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class JobListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun job(id: Long, title: String, status: JobStatus = JobStatus.QUOTED) = Job(
        id = id,
        title = title,
        category = null,
        location = null,
        vendorName = null,
        vendorContact = null,
        status = status,
        quotedCost = null,
        actualCost = null,
        predictedHours = null,
        actualHours = null,
        scheduledDate = null,
        completedDate = null,
        warrantyExpiry = null,
        paymentStatus = PaymentStatus.UNPAID,
        paymentMethod = null,
        createdAt = "2026-01-01T00:00:00Z",
        updatedAt = "2026-01-01T00:00:00Z",
    )

    @Test
    fun `refreshes on init and exposes cached jobs`() = runTest {
        val repository = FakeJobRepository()
        repository.jobsState.value = listOf(job(1, "Fix roof"))

        val viewModel = JobListViewModel(repository)

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(1, state.jobs.size)
            assertNull(state.errorMessage)
        }
        assertEquals(1, repository.refreshJobsCallCount)
    }

    @Test
    fun `refresh failure surfaces an error message`() = runTest {
        val repository = FakeJobRepository()
        repository.refreshJobsResult = Result.failure(RuntimeException("offline"))

        val viewModel = JobListViewModel(repository)

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("offline", state.errorMessage)
        }
    }

    @Test
    fun `updateFilter narrows the observed jobs by status`() = runTest {
        val repository = FakeJobRepository()
        repository.jobsState.value = listOf(
            job(1, "Fix roof", status = JobStatus.DONE),
            job(2, "Paint fence", status = JobStatus.QUOTED),
        )
        val viewModel = JobListViewModel(repository)

        viewModel.updateFilter(JobFilter(status = JobStatus.DONE))

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(1, state.jobs.size)
            assertEquals("Fix roof", state.jobs.single().title)
        }
    }

    @Test
    fun `deleteJob delegates to the repository`() = runTest {
        val repository = FakeJobRepository()
        repository.jobsState.value = listOf(job(1, "Fix roof"))
        val viewModel = JobListViewModel(repository)

        viewModel.deleteJob(1)

        assertEquals(1L, repository.deleteJobCalledWith)
        assertTrue(repository.jobsState.value.isEmpty())
    }
}
