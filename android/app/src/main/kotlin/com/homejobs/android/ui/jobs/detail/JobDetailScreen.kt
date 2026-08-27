package com.homejobs.android.ui.jobs.detail

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.homejobs.android.data.local.photo.PhotoStorage
import com.homejobs.android.domain.model.Job
import com.homejobs.android.domain.model.JobNote
import com.homejobs.android.domain.model.Photo
import com.homejobs.android.ui.common.ErrorState
import com.homejobs.android.ui.common.LoadingState
import com.homejobs.android.ui.common.UiState
import com.homejobs.android.ui.common.toDisplayDate
import com.homejobs.android.ui.common.toDisplayDateTime
import java.io.File

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
            is UiState.Error -> ErrorState(jobState.message, modifier = Modifier.padding(padding))
            is UiState.Success -> JobDetailContent(
                job = jobState.data,
                notes = uiState.notes,
                noteDraft = uiState.noteDraft,
                pendingPhotoPaths = uiState.pendingPhotoPaths,
                isSubmittingNote = uiState.isSubmittingNote,
                noteError = uiState.noteError,
                onNoteDraftChange = viewModel::updateNoteDraft,
                onPhotoAdded = viewModel::addPendingPhoto,
                onPendingPhotoRemoved = viewModel::removePendingPhoto,
                onSubmitNote = viewModel::submitNote,
                onDeleteNote = viewModel::deleteNote,
                onDeletePhoto = viewModel::deletePhoto,
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
    pendingPhotoPaths: List<String>,
    isSubmittingNote: Boolean,
    noteError: String?,
    onNoteDraftChange: (String) -> Unit,
    onPhotoAdded: (String) -> Unit,
    onPendingPhotoRemoved: (String) -> Unit,
    onSubmitNote: () -> Unit,
    onDeleteNote: (Long) -> Unit,
    onDeletePhoto: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var viewingPhoto by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { JobSummaryCard(job) }
        item { Text("Timeline", style = MaterialTheme.typography.titleMedium) }
        item {
            AddNoteCard(
                draft = noteDraft,
                pendingPhotoPaths = pendingPhotoPaths,
                isSubmitting = isSubmittingNote,
                error = noteError,
                onDraftChange = onNoteDraftChange,
                onPhotoAdded = onPhotoAdded,
                onPendingPhotoRemoved = onPendingPhotoRemoved,
                onSubmit = onSubmitNote,
            )
        }
        if (notes.isEmpty()) {
            item { Text("No notes yet.", style = MaterialTheme.typography.bodyMedium) }
        }
        items(notes, key = { it.id }) { note ->
            NoteRow(
                note = note,
                onDelete = { onDeleteNote(note.id) },
                onDeletePhoto = onDeletePhoto,
                onPhotoClick = { path -> viewingPhoto = path },
            )
        }
    }

    viewingPhoto?.let { path ->
        PhotoViewerDialog(filePath = path, onDismiss = { viewingPhoto = null })
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
            job.scheduledDate?.let { InfoRow("Scheduled", it.toDisplayDate()) }
            job.completedDate?.let { InfoRow("Completed", it.toDisplayDate()) }
            job.warrantyExpiry?.let { InfoRow("Warranty until", it.toDisplayDate()) }
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
private fun AddNoteCard(
    draft: String,
    pendingPhotoPaths: List<String>,
    isSubmitting: Boolean,
    error: String?,
    onDraftChange: (String) -> Unit,
    onPhotoAdded: (String) -> Unit,
    onPendingPhotoRemoved: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    val context = LocalContext.current
    val photoStorage = remember { PhotoStorage(context.applicationContext) }
    var pendingCapturePath by remember { mutableStateOf<String?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val path = pendingCapturePath
        if (success && path != null) onPhotoAdded(path)
        pendingCapturePath = null
    }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) onPhotoAdded(photoStorage.copyToAppStorage(uri))
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = draft,
                onValueChange = onDraftChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Add a note") },
                enabled = !isSubmitting,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = {
                    val target = photoStorage.createCaptureTarget()
                    pendingCapturePath = target.filePath
                    cameraLauncher.launch(target.uri)
                }) {
                    Icon(Icons.Filled.PhotoCamera, contentDescription = "Take photo")
                }
                IconButton(onClick = {
                    galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }) {
                    Icon(Icons.Filled.PhotoLibrary, contentDescription = "Choose photo from gallery")
                }
                TextButton(
                    onClick = onSubmit,
                    enabled = !isSubmitting && (draft.isNotBlank() || pendingPhotoPaths.isNotEmpty()),
                ) {
                    Text("Add")
                }
            }

            if (pendingPhotoPaths.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(pendingPhotoPaths, key = { it }) { path ->
                        RemovableThumbnail(filePath = path, onRemove = { onPendingPhotoRemoved(path) })
                    }
                }
            }

            error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
        }
    }
}

@Composable
private fun RemovableThumbnail(filePath: String, onRemove: () -> Unit) {
    Box(modifier = Modifier.size(72.dp)) {
        AsyncImage(
            model = File(filePath),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        IconButton(
            onClick = onRemove,
            modifier = Modifier.align(Alignment.TopEnd).size(20.dp),
        ) {
            Icon(Icons.Filled.Close, contentDescription = "Remove photo", tint = Color.White)
        }
    }
}

@Composable
private fun NoteRow(
    note: JobNote,
    onDelete: () -> Unit,
    onDeletePhoto: (Long) -> Unit,
    onPhotoClick: (String) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(note.timestamp.toDisplayDateTime(), style = MaterialTheme.typography.labelSmall)
                    if (note.body.isNotBlank()) {
                        Text(note.body, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete note")
                }
            }
            if (note.photos.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(note.photos, key = { it.id }) { photo: Photo ->
                        Box(modifier = Modifier.size(80.dp)) {
                            AsyncImage(
                                model = File(photo.filePath),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onPhotoClick(photo.filePath) },
                            )
                            IconButton(
                                onClick = { onDeletePhoto(photo.id) },
                                modifier = Modifier.align(Alignment.TopEnd).size(20.dp),
                            ) {
                                Icon(Icons.Filled.Close, contentDescription = "Remove photo", tint = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PhotoViewerDialog(filePath: String, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Box(modifier = Modifier.wrapContentSize()) {
            AsyncImage(
                model = File(filePath),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDismiss() },
            )
        }
    }
}
