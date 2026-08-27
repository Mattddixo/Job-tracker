package com.homejobs.android.ui.jobs.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.homejobs.android.domain.model.Job
import com.homejobs.android.domain.model.JobNote
import com.homejobs.android.ui.common.ErrorState
import com.homejobs.android.ui.common.LoadingState
import com.homejobs.android.ui.common.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobDetailScreen(
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    viewModel: JobDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Job details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    val job = (uiState.job as? UiState.Success)?.data
                    if (job != null) {
                        IconButton(onClick = { onEdit(job.id) }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit job")
                        }
                        IconButton(onClick = { viewModel.deleteJob(onBack) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete job")
                        }
                    }
                },
            )
        },
    ) { padding ->
        when (val jobState = uiState.job) {
            is UiState.Loading -> LoadingState(modifier = Modifier.padding(padding))
            is UiState.Empty -> ErrorState("Job not found.", modifier = Modifier.padding(padding))
            is UiState.Error -> ErrorState(jobState.message, onRetry = viewModel::refresh, modifier = Modifier.padding(padding))
            is UiState.Success -> JobDetailContent(
                job = jobState.data,
                notes = uiState.notes,
                noteDraft = uiState.noteDraft,
                isSubmittingNote = uiState.isSubmittingNote,
                noteError = uiState.noteError,
                onNoteDraftChange = viewModel::updateNoteDraft,
                onSubmitNote = viewModel::submitNote,
                onDeleteNote = viewModel::deleteNote,
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@Composable
private fun JobDetailContent(
    job: Job,
    notes: List<JobNote>,
    noteDraft: String,
    isSubmittingNote: Boolean,
    noteError: String?,
    onNoteDraftChange: (String) -> Unit,
    onSubmitNote: () -> Unit,
    onDeleteNote: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { JobSummaryCard(job) }
        item {
            Text("Timeline", style = MaterialTheme.typography.titleMedium)
        }
        item {
            AddNoteRow(
                draft = noteDraft,
                isSubmitting = isSubmittingNote,
                error = noteError,
                onDraftChange = onNoteDraftChange,
                onSubmit = onSubmitNote,
            )
        }
        if (notes.isEmpty()) {
            item { Text("No notes yet.", style = MaterialTheme.typography.bodyMedium) }
        }
        items(notes, key = { it.id }) { note ->
            NoteRow(note = note, onDelete = { onDeleteNote(note.id) })
        }
    }
}

@Composable
private fun JobSummaryCard(job: Job) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(job.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            InfoRow("Status", job.status.name.replace('_', ' '))
            job.category?.let { InfoRow("Category", it) }
            job.location?.let { InfoRow("Location", it) }
            job.vendorName?.let { InfoRow("Vendor", it) }
            job.vendorContact?.let { InfoRow("Contact", it) }
            HorizontalDivider()
            InfoRow("Quoted cost", job.quotedCost?.let { "$%.2f".format(it) } ?: "—")
            InfoRow("Actual cost", job.actualCost?.let { "$%.2f".format(it) } ?: "—")
            InfoRow("Cost variance", job.costVariance?.let { "$%.2f".format(it) } ?: "—")
            InfoRow("Predicted hours", job.predictedHours?.toString() ?: "—")
            InfoRow("Actual hours", job.actualHours?.toString() ?: "—")
            InfoRow("Time variance", job.timeVariance?.toString() ?: "—")
            HorizontalDivider()
            job.scheduledDate?.let { InfoRow("Scheduled", it) }
            job.completedDate?.let { InfoRow("Completed", it) }
            job.warrantyExpiry?.let { InfoRow("Warranty until", it) }
            InfoRow("Payment status", job.paymentStatus.name)
            job.paymentMethod?.let { InfoRow("Payment method", it) }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun AddNoteRow(
    draft: String,
    isSubmitting: Boolean,
    error: String?,
    onDraftChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    Column {
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            OutlinedTextField(
                value = draft,
                onValueChange = onDraftChange,
                modifier = Modifier.weight(1f),
                label = { Text("Add a note") },
                enabled = !isSubmitting,
            )
            TextButton(onClick = onSubmit, enabled = !isSubmitting && draft.isNotBlank()) {
                Text("Add")
            }
        }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
    }
}

@Composable
private fun NoteRow(note: JobNote, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(note.timestamp, style = MaterialTheme.typography.labelSmall)
                Text(note.body, style = MaterialTheme.typography.bodyMedium)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete note")
            }
        }
    }
}
