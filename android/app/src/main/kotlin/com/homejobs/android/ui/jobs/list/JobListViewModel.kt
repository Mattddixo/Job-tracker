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
    private val isLoading = MutableStateFlow(true)
    private val errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<JobListUiState> = combine(
        filter.flatMapLatest { repository.observeJobs(it) },
        filter,
        selectedTab,
        isLoading,
        errorMessage,
    ) { jobs, currentFilter, tab, loading, error ->
        JobListUiState(
            jobs = jobs.filter { tab.matches(it.status) },
            filter = currentFilter,
            selectedTab = tab,
            isLoading = loading,
            errorMessage = error,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), JobListUiState())

    init {
        refresh()
    }

    fun updateFilter(newFilter: JobFilter) {
        filter.value = newFilter
    }

    fun selectTab(tab: JobListTab) {
        selectedTab.value = tab
    }

    fun refresh() {
        viewModelScope.launch {
            isLoading.value = true
            repository.refreshJobs()
                .onSuccess { errorMessage.value = null }
                .onFailure { errorMessage.value = it.message ?: "Failed to load jobs" }
            isLoading.value = false
        }
    }

    fun deleteJob(id: Long) {
        viewModelScope.launch {
            repository.deleteJob(id).onFailure { errorMessage.value = it.message ?: "Failed to delete job" }
        }
    }
}
