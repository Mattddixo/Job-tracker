package com.homejobs.android.viewmodel

import app.cash.turbine.test
import com.homejobs.android.MainDispatcherRule
import com.homejobs.android.domain.model.Job
import com.homejobs.android.domain.model.JobFilter
import com.homejobs.android.domain.model.JobStatus
import com.homejobs.android.domain.model.PaymentStatus
import com.homejobs.android.fakes.FakeJobRepository
import com.homejobs.android.ui.jobs.list.JobListTab
import com.homejobs.android.ui.jobs.list.JobListViewModel
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
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
        createdAt = 0L,
        updatedAt = 0L,
    )

    @Test
    fun `exposes cached jobs from the repository`() = runTest {
        val repository = FakeJobRepository()
        repository.jobsState.value = listOf(job(1, "Fix roof", status = JobStatus.SCHEDULED))

        val viewModel = JobListViewModel(repository)

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(1, state.jobs.size)
        }
    }

    @Test
    fun `updateFilter narrows the observed jobs by status within a tab`() = runTest {
        val repository = FakeJobRepository()
        repository.jobsState.value = listOf(
            job(1, "Fix roof", status = JobStatus.DONE),
            job(2, "Paint fence", status = JobStatus.QUOTED),
        )
        val viewModel = JobListViewModel(repository)
        viewModel.selectTab(JobListTab.ALL)

        viewModel.updateFilter(JobFilter(status = JobStatus.DONE))

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(1, state.jobs.size)
            assertEquals("Fix roof", state.jobs.single().title)
        }
    }

    @Test
    fun `active tab hides completed and cancelled jobs by default`() = runTest {
        val repository = FakeJobRepository()
        repository.jobsState.value = listOf(
            job(1, "Fix roof", status = JobStatus.DONE),
            job(2, "Paint fence", status = JobStatus.QUOTED),
            job(3, "Old estimate", status = JobStatus.CANCELLED),
        )
        val viewModel = JobListViewModel(repository)

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(JobListTab.ACTIVE, state.selectedTab)
            assertEquals(1, state.jobs.size)
            assertEquals("Paint fence", state.jobs.single().title)
        }
    }

    @Test
    fun `completed tab shows only done and cancelled jobs`() = runTest {
        val repository = FakeJobRepository()
        repository.jobsState.value = listOf(
            job(1, "Fix roof", status = JobStatus.DONE),
            job(2, "Paint fence", status = JobStatus.QUOTED),
            job(3, "Old estimate", status = JobStatus.CANCELLED),
        )
        val viewModel = JobListViewModel(repository)

        viewModel.selectTab(JobListTab.COMPLETED)

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(2, state.jobs.size)
            assertTrue(state.jobs.none { it.title == "Paint fence" })
        }
    }

    @Test
    fun `all tab shows every job regardless of status`() = runTest {
        val repository = FakeJobRepository()
        repository.jobsState.value = listOf(
            job(1, "Fix roof", status = JobStatus.DONE),
            job(2, "Paint fence", status = JobStatus.QUOTED),
        )
        val viewModel = JobListViewModel(repository)

        viewModel.selectTab(JobListTab.ALL)

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(2, state.jobs.size)
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
