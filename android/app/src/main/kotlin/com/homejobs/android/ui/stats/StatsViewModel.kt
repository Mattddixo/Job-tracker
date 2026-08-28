package com.homejobs.android.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homejobs.android.domain.model.Job
import com.homejobs.android.domain.model.JobFilter
import com.homejobs.android.domain.model.PaymentMethod
import com.homejobs.android.domain.model.PaymentStatus
import com.homejobs.android.domain.repository.JobRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** [method] is null for the "Unassigned" bucket — jobs with no payment method picked yet. */
data class MethodStat(
    val method: PaymentMethod?,
    val paidTotal: Double,
    val partialTotal: Double,
    val unpaidTotal: Double,
    val jobCount: Int,
)

data class StatsUiState(
    val methodStats: List<MethodStat> = emptyList(),
    val totalPaid: Double = 0.0,
    val totalOwed: Double = 0.0,
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val repository: JobRepository,
) : ViewModel() {

    val uiState: StateFlow<StatsUiState> = combine(
        repository.observeJobs(JobFilter()),
        repository.observePaymentMethods(),
    ) { jobs, methods -> buildUiState(jobs, methods) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatsUiState())

    fun addPaymentMethod(name: String, maxCredit: Double?) {
        viewModelScope.launch { repository.addPaymentMethod(name, maxCredit) }
    }

    fun updatePaymentMethod(id: Long, name: String, maxCredit: Double?) {
        viewModelScope.launch { repository.updatePaymentMethod(id, name, maxCredit) }
    }

    fun deletePaymentMethod(id: Long) {
        viewModelScope.launch { repository.deletePaymentMethod(id) }
    }

    private fun buildUiState(jobs: List<Job>, methods: List<PaymentMethod>): StatsUiState {
        val jobsByMethodId = jobs.groupBy { it.paymentMethodId }
        val configured = methods.map { method -> methodStat(method, jobsByMethodId[method.id].orEmpty()) }
        val unassigned = methodStat(method = null, jobs = jobsByMethodId[null].orEmpty())
        val methodStats = configured + listOfNotNull(unassigned.takeIf { it.jobCount > 0 })

        return StatsUiState(
            methodStats = methodStats,
            totalPaid = methodStats.sumOf { it.paidTotal },
            totalOwed = methodStats.sumOf { it.partialTotal + it.unpaidTotal },
        )
    }

    /**
     * Only [Job.actualCost] counts toward a payment method's totals — a quote isn't money that's
     * actually gone out on that method yet. Jobs with no actual cost still count toward
     * [MethodStat.jobCount], just not toward any dollar total, until one is entered.
     */
    private fun methodStat(method: PaymentMethod?, jobs: List<Job>): MethodStat {
        var paid = 0.0
        var partial = 0.0
        var unpaid = 0.0
        for (job in jobs) {
            val amount = job.actualCost ?: continue
            when (job.paymentStatus) {
                PaymentStatus.PAID -> paid += amount
                PaymentStatus.PARTIAL -> partial += amount
                PaymentStatus.UNPAID -> unpaid += amount
            }
        }
        return MethodStat(method = method, paidTotal = paid, partialTotal = partial, unpaidTotal = unpaid, jobCount = jobs.size)
    }
}
