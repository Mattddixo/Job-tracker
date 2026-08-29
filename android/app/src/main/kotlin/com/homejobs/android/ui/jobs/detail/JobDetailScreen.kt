package com.homejobs.android.ui.jobs.detail

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.homejobs.android.data.local.photo.PhotoStorage
import com.homejobs.android.domain.model.Job
import com.homejobs.android.domain.model.JobNote
import com.homejobs.android.domain.model.PaymentMethod
import com.homejobs.android.domain.model.Photo
import com.homejobs.android.ui.common.ErrorState
import com.homejobs.android.ui.common.LoadingState
import com.homejobs.android.ui.common.openInJobJar
import com.homejobs.android.ui.common.PhotoViewerDialog
import com.homejobs.android.ui.common.UiState
import com.homejobs.android.ui.common.toDisplayDate
import com.homejobs.android.ui.common.toDisplayDateTime
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobDetailScreen(
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    onViewPhotos: (Long, Long?) -> Unit,
    scrollToNoteId: Long? = null,
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
                        IconButton(onClick = { onViewPhotos(job.id, null) }) {
                            Icon(Icons.Filled.PhotoLibrary, contentDescription = "View all photos")
                        }
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
                paymentMethods = uiState.paymentMethods,
                noteDraft = uiState.noteDraft,
                pendingPhotoPaths = uiState.pendingPhotoPaths,
                isSubmittingNote = uiState.isSubmittingNote,
                noteError = uiState.noteError,
                editingNoteId = uiState.editingNoteId,
                editingNoteDraft = uiState.editingNoteDraft,
                isSavingEditedNote = uiState.isSavingEditedNote,
                editNoteError = uiState.editNoteError,
                onNoteDraftChange = viewModel::updateNoteDraft,
                onPhotoAdded = viewModel::addPendingPhoto,
                onPendingPhotoRemoved = viewModel::removePendingPhoto,
                onSubmitNote = viewModel::submitNote,
                onDeleteNote = viewModel::deleteNote,
                onDeletePhoto = viewModel::deletePhoto,
                onStartEditNote = viewModel::startEditingNote,
                onEditDraftChange = viewModel::updateEditingNoteDraft,
                onCancelEditNote = viewModel::cancelEditingNote,
                onSaveEditNote = viewModel::saveEditedNote,
                onAddPhotoToNote = viewModel::addPhotoToNote,
                onViewPhotos = onViewPhotos,
                initialScrollToNoteId = scrollToNoteId,
                modifier = Modifier.padding(padding),
            )
        }
    }
}

private data class PhotoViewerRequest(val photos: List<Photo>, val initialIndex: Int)

/** Number of fixed `item {}` entries before the notes themselves, for scroll-to-note math. */
private const val NOTES_LIST_HEADER_COUNT = 3

@Composable
private fun JobDetailContent(
    job: Job,
    notes: List<JobNote>,
    paymentMethods: List<PaymentMethod>,
    noteDraft: String,
    pendingPhotoPaths: List<String>,
    isSubmittingNote: Boolean,
    noteError: String?,
    editingNoteId: Long?,
    editingNoteDraft: String,
    isSavingEditedNote: Boolean,
    editNoteError: String?,
    onNoteDraftChange: (String) -> Unit,
    onPhotoAdded: (String) -> Unit,
    onPendingPhotoRemoved: (String) -> Unit,
    onSubmitNote: () -> Unit,
    onDeleteNote: (Long) -> Unit,
    onDeletePhoto: (Long) -> Unit,
    onStartEditNote: (JobNote) -> Unit,
    onEditDraftChange: (String) -> Unit,
    onCancelEditNote: () -> Unit,
    onSaveEditNote: () -> Unit,
    onAddPhotoToNote: (Long, String) -> Unit,
    onViewPhotos: (Long, Long?) -> Unit,
    initialScrollToNoteId: Long?,
    modifier: Modifier = Modifier,
) {
    var viewerRequest by remember { mutableStateOf<PhotoViewerRequest?>(null) }
    var pendingScrollNoteId by remember { mutableStateOf(initialScrollToNoteId) }
    val listState = rememberLazyListState()

    LaunchedEffect(pendingScrollNoteId, notes) {
        val targetId = pendingScrollNoteId ?: return@LaunchedEffect
        val index = notes.indexOfFirst { it.id == targetId }
        if (index >= 0) {
            listState.animateScrollToItem(NOTES_LIST_HEADER_COUNT + index)
            pendingScrollNoteId = null
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { JobSummaryCard(job, paymentMethods) }
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
                isEditing = editingNoteId == note.id,
                editDraft = editingNoteDraft,
                isSaving = isSavingEditedNote,
                editError = if (editingNoteId == note.id) editNoteError else null,
                onDelete = { onDeleteNote(note.id) },
                onDeletePhoto = onDeletePhoto,
                onPhotoClick = { photo ->
                    viewerRequest = PhotoViewerRequest(
                        photos = note.photos,
                        initialIndex = note.photos.indexOf(photo),
                    )
                },
                onStartEdit = { onStartEditNote(note) },
                onEditDraftChange = onEditDraftChange,
                onCancelEdit = onCancelEditNote,
                onSaveEdit = onSaveEditNote,
                onAddPhoto = { path -> onAddPhotoToNote(note.id, path) },
            )
        }
    }

    viewerRequest?.let { request ->
        PhotoViewerDialog(
            photos = request.photos,
            initialIndex = request.initialIndex,
            onDismiss = { viewerRequest = null },
            onOpenGrid = { photo ->
                viewerRequest = null
                onViewPhotos(job.id, photo.id)
            },
            onGoToNote = { photo ->
                viewerRequest = null
                pendingScrollNoteId = photo.noteId
            },
        )
    }
}

@Composable
private fun JobSummaryCard(job: Job, paymentMethods: List<PaymentMethod>) {
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
            job.paymentMethodId
                ?.let { id -> paymentMethods.firstOrNull { it.id == id }?.name }
                ?.let { InfoRow("Payment method", it) }
            HorizontalDivider()
            JobJarLinkActions(job)
        }
    }
}

/**
 * Links this job to a Job Jar task (a separate, unrelated app for tracking chores) via implicit
 * `ACTION_VIEW` intents against its own custom URI scheme — the standard way for two local-only
 * Android apps on the same device to talk to each other without a shared backend. Once a link
 * exists it's the only thing shown here — "Send"/"Link" both disappear, so a job can never end
 * up linked to two different Job Jar tasks at once (see [Job.linkedJobJarId]).
 */
@Composable
private fun JobJarLinkActions(job: Job) {
    val context = LocalContext.current
    val linkedJobJarId = job.linkedJobJarId
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (linkedJobJarId == null) {
            OutlinedButton(
                onClick = { openInJobJar(context, sendToJobJarUri(job)) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Send to Job Jar")
            }
            OutlinedButton(
                onClick = { openInJobJar(context, Uri.parse("jobjar://pickjob?returnJobId=${job.id}")) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Link to existing Job Jar job")
            }
        } else {
            OutlinedButton(
                onClick = { openInJobJar(context, Uri.parse("jobjar://job/$linkedJobJarId")) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Open in Job Jar")
            }
        }
    }
}

private fun sendToJobJarUri(job: Job): Uri {
    val builder = Uri.parse("jobjar://newjob").buildUpon()
        .appendQueryParameter("title", job.title)
        .appendQueryParameter("sourceId", job.id.toString())
    job.category?.let { builder.appendQueryParameter("category", it) }
    return builder.build()
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
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
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
    isEditing: Boolean,
    editDraft: String,
    isSaving: Boolean,
    editError: String?,
    onDelete: () -> Unit,
    onDeletePhoto: (Long) -> Unit,
    onPhotoClick: (Photo) -> Unit,
    onStartEdit: () -> Unit,
    onEditDraftChange: (String) -> Unit,
    onCancelEdit: () -> Unit,
    onSaveEdit: () -> Unit,
    onAddPhoto: (String) -> Unit,
) {
    val context = LocalContext.current
    val photoStorage = remember { PhotoStorage(context.applicationContext) }
    var pendingCapturePath by remember { mutableStateOf<String?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val path = pendingCapturePath
        if (success && path != null) onAddPhoto(path)
        pendingCapturePath = null
    }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) onAddPhoto(photoStorage.copyToAppStorage(uri))
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(note.timestamp.toDisplayDateTime(), style = MaterialTheme.typography.labelSmall)
                    if (!isEditing && note.body.isNotBlank()) {
                        Text(note.body, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                if (!isEditing) {
                    IconButton(onClick = onStartEdit) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit note")
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete note")
                }
            }
            if (isEditing) {
                OutlinedTextField(
                    value = editDraft,
                    onValueChange = onEditDraftChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Note") },
                    enabled = !isSaving,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
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
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = onCancelEdit, enabled = !isSaving) { Text("Cancel") }
                    TextButton(onClick = onSaveEdit, enabled = !isSaving) { Text("Save") }
                }
                editError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
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
                                    .clickable { onPhotoClick(photo) },
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
