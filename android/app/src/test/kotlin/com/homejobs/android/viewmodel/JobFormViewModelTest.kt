package com.homejobs.android.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.homejobs.android.MainDispatcherRule
import com.homejobs.android.data.parsing.QuotePdfParser
import com.homejobs.android.domain.model.Job
import com.homejobs.android.domain.model.ParsedQuote
import com.homejobs.android.fakes.FakeJobRepository
import com.homejobs.android.ui.jobs.form.JobFormViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class JobFormViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModel(
        savedStateHandle: SavedStateHandle = SavedStateHandle(),
        repository: FakeJobRepository = FakeJobRepository(),
        quotePdfParser: QuotePdfParser = mockk(relaxed = true),
    ) = JobFormViewModel(savedStateHandle, repository, quotePdfParser)

    @Test
    fun `blank title fails validation and does not call the repository`() = runTest {
        val viewModel = viewModel()
        var savedCalled = false

        viewModel.save { savedCalled = true }

        assertFalse(savedCalled)
        assertTrue(viewModel.uiState.value.errors.isNotEmpty())
    }

    @Test
    fun `valid input creates a job and invokes onSaved`() = runTest {
        val viewModel = viewModel()
        var savedCalled = false

        viewModel.updateInput { it.copy(title = "Replace gutters") }
        viewModel.save { savedCalled = true }

        assertTrue(savedCalled)
        assertTrue(viewModel.uiState.value.errors.isEmpty())
    }

    @Test
    fun `negative cost fails validation`() = runTest {
        val viewModel = viewModel()

        viewModel.updateInput { it.copy(title = "Job", quotedCost = -50.0) }
        viewModel.save {}

        assertTrue(viewModel.uiState.value.errors.any { it.contains("negative") })
    }

    @Test
    fun `importing a PDF fills only the fields it found and reports what happened`() = runTest {
        val parser = mockk<QuotePdfParser>()
        every { parser.parse(any()) } returns ParsedQuote(vendorName = "ABC Plumbing", quotedCost = 450.0)
        val viewModel = viewModel(quotePdfParser = parser)
        viewModel.updateInput { it.copy(title = "Job", vendorContact = "already set") }

        viewModel.importFromPdf(mockk(relaxed = true))

        val state = viewModel.uiState.value
        assertEquals("ABC Plumbing", state.input.vendorName)
        assertEquals(450.0, state.input.quotedCost)
        assertEquals("already set", state.input.vendorContact) // untouched: parser found nothing for it
        assertTrue(state.pdfImportMessage!!.contains("vendor"))
        assertTrue(state.pdfImportMessage!!.contains("quoted cost"))
        assertFalse(state.isImportingPdf)
    }

    @Test
    fun `importing a PDF with nothing recognizable leaves the form untouched`() = runTest {
        val parser = mockk<QuotePdfParser>()
        every { parser.parse(any()) } returns ParsedQuote()
        val viewModel = viewModel(quotePdfParser = parser)
        viewModel.updateInput { it.copy(title = "Job", vendorName = "Existing vendor") }

        viewModel.importFromPdf(mockk(relaxed = true))

        val state = viewModel.uiState.value
        assertEquals("Existing vendor", state.input.vendorName)
        assertNull(state.input.quotedCost)
        assertTrue(state.pdfImportMessage!!.contains("enter details manually"))
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
                paymentMethodId = null,
                createdAt = 0L,
                updatedAt = 0L,
            ),
        )

        val viewModel = viewModel(savedStateHandle = SavedStateHandle(mapOf("jobId" to "42")), repository = repository)

        assertEquals("Existing job", viewModel.uiState.value.input.title)
        assertEquals("HVAC", viewModel.uiState.value.input.category)
        assertTrue(viewModel.uiState.value.isEditing)
    }
}
