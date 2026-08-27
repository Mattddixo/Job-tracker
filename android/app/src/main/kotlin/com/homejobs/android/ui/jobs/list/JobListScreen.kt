package com.homejobs.android.ui.jobs.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.homejobs.android.domain.model.Job
import com.homejobs.android.domain.model.JobFilter
import com.homejobs.android.domain.model.SortDirection
import com.homejobs.android.ui.common.EmptyState
import com.homejobs.android.ui.common.ErrorState
import com.homejobs.android.ui.common.LoadingState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobListScreen(
    onJobClick: (Long) -> Unit,
    onAddJobClick: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: JobListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Home Jobs") },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddJobClick) {
                Icon(Icons.Filled.Add, contentDescription = "Add job")
            }
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = uiState.selectedTab.ordinal) {
                JobListTab.entries.forEach { tab ->
                    Tab(
                        selected = uiState.selectedTab == tab,
                        onClick = { viewModel.selectTab(tab) },
                        text = { Text(tab.label) },
                    )
                }
            }
            SortBar(filter = uiState.filter, onFilterChange = viewModel::updateFilter)
            when {
                uiState.isLoading && uiState.jobs.isEmpty() -> LoadingState()
                uiState.errorMessage != null && uiState.jobs.isEmpty() ->
                    ErrorState(message = uiState.errorMessage!!, onRetry = viewModel::refresh)
                uiState.jobs.isEmpty() -> EmptyState(emptyMessage(uiState.selectedTab))
                else -> JobList(jobs = uiState.jobs, onJobClick = onJobClick)
            }
        }
    }
}

private fun emptyMessage(tab: JobListTab): String = when (tab) {
    JobListTab.ACTIVE -> "No active jobs. Tap + to add one."
    JobListTab.COMPLETED -> "No completed jobs yet."
    JobListTab.ALL -> "No jobs yet. Tap + to add one."
}

@Composable
private fun JobList(jobs: List<Job>, onJobClick: (Long) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(jobs, key = { it.id }) { job ->
            JobRow(job = job, onClick = { onJobClick(job.id) })
        }
    }
}

@Composable
private fun JobRow(job: Job, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = job.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(text = job.status.name.replace('_', ' '), style = MaterialTheme.typography.labelMedium)
            }
            if (job.category != null || job.location != null) {
                Text(
                    text = listOfNotNull(job.category, job.location).joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            job.costVariance?.let { variance ->
                val label = if (variance <= 0) "Under quote by $%.2f".format(-variance) else "Over quote by $%.2f".format(variance)
                Text(text = label, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SortBar(filter: JobFilter, onFilterChange: (JobFilter) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = filter.sortDir == SortDirection.ASC,
            onClick = {
                onFilterChange(
                    filter.copy(sortDir = if (filter.sortDir == SortDirection.ASC) SortDirection.DESC else SortDirection.ASC),
                )
            },
            label = { Text(if (filter.sortDir == SortDirection.ASC) "Oldest first" else "Newest first") },
        )
    }
}
