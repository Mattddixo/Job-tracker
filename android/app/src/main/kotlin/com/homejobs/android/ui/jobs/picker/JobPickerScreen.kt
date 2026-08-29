package com.homejobs.android.ui.jobs.picker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.homejobs.android.ui.common.EmptyState
import com.homejobs.android.ui.common.fireLinkedCallback

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobPickerScreen(
    onBack: () -> Unit,
    viewModel: JobPickerViewModel = hiltViewModel(),
) {
    val jobs by viewModel.pickableJobs.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select a job to link") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        if (jobs.isEmpty()) {
            EmptyState("No jobs available to pick.", modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(jobs, key = { it.id }) { job ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            viewModel.linkTo(job) { linkedJob ->
                                fireLinkedCallback(context, jobJarId = viewModel.returnJobId, trackerJobId = linkedJob.id)
                                onBack()
                            }
                        },
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(job.title, style = MaterialTheme.typography.bodyLarge)
                            // Not excluded from the list — see JobPickerViewModel.pickableJobs —
                            // but flagged so picking it is an informed choice: it'll overwrite
                            // whatever this job was linked to before.
                            if (job.linkedJobJarId != null) {
                                Text(
                                    "Already linked to a different job — picking this will re-point it here",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
