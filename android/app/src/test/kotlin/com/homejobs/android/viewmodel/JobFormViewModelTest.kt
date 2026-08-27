package com.homejobs.android.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.homejobs.android.MainDispatcherRule
import com.homejobs.android.domain.model.Job
import com.homejobs.android.fakes.FakeJobRepository
import com.homejobs.android.ui.jobs.form.JobFormViewModel
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class JobFormViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `blank title fails validation and does not call the repository`() = runTest {
        val repository = FakeJobRepository()
        val viewModel = JobFormViewModel(SavedStateHandle(), repository)
        var savedCalled = false

        viewModel.save { savedCalled = true }

        assertFalse(savedCalled)
        assertTrue(viewModel.uiState.value.errors.isNotEmpty())
    }

    @Test
    fun `valid input creates a job and invokes onSaved`() = runTest {
        val repository = FakeJobRepository()
        val viewModel = JobFormViewModel(SavedStateHandle(), repository)
        var savedCalled = false

        viewModel.updateInput { it.copy(title = "Replace gutters") }
        viewModel.save { savedCalled = true }

        assertTrue(savedCalled)
        assertTrue(viewModel.uiState.value.errors.isEmpty())
    }

    @Test
    fun `negative cost fails validation`() = runTest {
        val repository = FakeJobRepository()
        val viewModel = JobFormViewModel(SavedStateHandle(), repository)

        viewModel.updateInput { it.copy(title = "Job", quotedCost = -50.0) }
        viewModel.save {}

        assertTrue(viewModel.uiState.value.errors.any { it.contains("negative") })
    }

    @Test
    fun `editing an existing job pre-populates the form`() = runTest {
        val repository = FakeJobRepository()
        repository.jobsState.value = listOf(
            Job(
                id = 42,
                title = "Existing job",
                category = "HVAC",
                location = null,
                vendorName = null,
                vendorContact = null,
                status = com.homejobs.android.domain.model.JobStatus.SCHEDULED,
                quotedCost = 200.0,
                actualCost = null,
                predictedHours = null,
                actualHours = null,
                scheduledDate = null,
                completedDate = null,
                warrantyExpiry = null,
                paymentStatus = com.homejobs.android.domain.model.PaymentStatus.UNPAID,
                paymentMethod = null,
                createdAt = "2026-01-01T00:00:00Z",
                updatedAt = "2026-01-01T00:00:00Z",
            ),
        )

        val viewModel = JobFormViewModel(SavedStateHandle(mapOf("jobId" to "42")), repository)

        assertEquals("Existing job", viewModel.uiState.value.input.title)
        assertEquals("HVAC", viewModel.uiState.value.input.category)
        assertTrue(viewModel.uiState.value.isEditing)
    }
}
