package com.homejobs.android.viewmodel

import app.cash.turbine.test
import com.homejobs.android.MainDispatcherRule
import com.homejobs.android.domain.model.Job
import com.homejobs.android.domain.model.JobStatus
import com.homejobs.android.domain.model.PaymentMethod
import com.homejobs.android.domain.model.PaymentStatus
import com.homejobs.android.fakes.FakeJobRepository
import com.homejobs.android.ui.stats.StatsViewModel
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class StatsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun job(
        id: Long,
        actualCost: Double?,
        paymentStatus: PaymentStatus,
        paymentMethodId: Long?,
    ) = Job(
        id = id,
        title = "Job $id",
        category = null,
        location = null,
        vendorName = null,
        vendorContact = null,
        status = JobStatus.DONE,
        quotedCost = null,
        actualCost = actualCost,
        predictedHours = null,
        actualHours = null,
        scheduledDate = null,
        completedDate = null,
        warrantyExpiry = null,
        paymentStatus = paymentStatus,
        paymentMethodId = paymentMethodId,
        createdAt = 0L,
        updatedAt = 0L,
    )

    @Test
    fun `sums actual cost per method, split by payment status`() = runTest {
        val repository = FakeJobRepository()
        repository.paymentMethodsState.value = listOf(PaymentMethod(id = 1, name = "Visa", maxCredit = 2000.0))
        repository.jobsState.value = listOf(
            job(1, actualCost = 100.0, paymentStatus = PaymentStatus.PAID, paymentMethodId = 1),
            job(2, actualCost = 50.0, paymentStatus = PaymentStatus.PARTIAL, paymentMethodId = 1),
            job(3, actualCost = 25.0, paymentStatus = PaymentStatus.UNPAID, paymentMethodId = 1),
        )
        val viewModel = StatsViewModel(repository)

        viewModel.uiState.test {
            val state = awaitItem()
            val visaStat = state.methodStats.single { it.method?.name == "Visa" }
            assertEquals(100.0, visaStat.paidTotal, 0.0)
            assertEquals(50.0, visaStat.partialTotal, 0.0)
            assertEquals(25.0, visaStat.unpaidTotal, 0.0)
            assertEquals(3, visaStat.jobCount)
            assertEquals(100.0, state.totalPaid, 0.0)
            assertEquals(75.0, state.totalOwed, 0.0)
        }
    }

    @Test
    fun `a job with no actual cost counts toward jobCount but not any total`() = runTest {
        val repository = FakeJobRepository()
        repository.paymentMethodsState.value = listOf(PaymentMethod(id = 1, name = "Cash", maxCredit = null))
        repository.jobsState.value = listOf(
            job(1, actualCost = null, paymentStatus = PaymentStatus.UNPAID, paymentMethodId = 1),
        )
        val viewModel = StatsViewModel(repository)

        viewModel.uiState.test {
            val state = awaitItem()
            val cashStat = state.methodStats.single { it.method?.name == "Cash" }
            assertEquals(1, cashStat.jobCount)
            assertEquals(0.0, cashStat.paidTotal + cashStat.partialTotal + cashStat.unpaidTotal, 0.0)
        }
    }

    @Test
    fun `jobs with no payment method land in an Unassigned bucket`() = runTest {
        val repository = FakeJobRepository()
        repository.jobsState.value = listOf(
            job(1, actualCost = 40.0, paymentStatus = PaymentStatus.UNPAID, paymentMethodId = null),
        )
        val viewModel = StatsViewModel(repository)

        viewModel.uiState.test {
            val state = awaitItem()
            val unassigned = state.methodStats.single()
            assertNull(unassigned.method)
            assertEquals(40.0, unassigned.unpaidTotal, 0.0)
        }
    }

    @Test
    fun `the Unassigned bucket is omitted when every job has a method`() = runTest {
        val repository = FakeJobRepository()
        repository.paymentMethodsState.value = listOf(PaymentMethod(id = 1, name = "Visa", maxCredit = null))
        repository.jobsState.value = listOf(
            job(1, actualCost = 10.0, paymentStatus = PaymentStatus.PAID, paymentMethodId = 1),
        )
        val viewModel = StatsViewModel(repository)

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.methodStats.none { it.method == null })
        }
    }
}
