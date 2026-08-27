package com.homejobs.android.ui.jobs.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homejobs.android.domain.model.JobFilter
import com.homejobs.android.domain.repository.JobRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class JobListViewModel @Inject constructor(
    private val repository: JobRepository,
) : ViewModel() {

    private val filter = MutableStateFlow(JobFilter())
    private val selectedTab = MutableStateFlow(JobListTab.ACTIVE)

    val uiState: StateFlow<JobListUiState> = combine(
        filter.flatMapLatest { repository.observeJobs(it) },
        filter,
        selectedTab,
    ) { jobs, currentFilter, tab ->
        JobListUiState(
            jobs = jobs.filter { tab.matches(it.status) },
            filter = currentFilter,
            selectedTab = tab,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), JobListUiState())

    fun updateFilter(newFilter: JobFilter) {
        filter.value = newFilter
    }

    fun selectTab(tab: JobListTab) {
        selectedTab.value = tab
    }

    fun deleteJob(id: Long) {
        viewModelScope.launch { repository.deleteJob(id) }
    }
}
