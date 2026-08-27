package com.homejobs.android.fakes

import com.homejobs.android.data.local.db.JobNoteDao
import com.homejobs.android.data.local.db.JobNoteEntity
import com.homejobs.android.data.local.db.NoteWithPhotos
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import java.util.concurrent.atomic.AtomicLong

class FakeJobNoteDao(private val photoDao: FakePhotoDao) : JobNoteDao {
    private val notesState = MutableStateFlow<List<JobNoteEntity>>(emptyList())
    private val idSeq = AtomicLong(1)

    init {
        photoDao.noteJobLookup = { noteId -> notesState.value.firstOrNull { it.id == noteId }?.jobId }
    }

    override fun observeNotesWithPhotos(jobId: Long) =
        combine(notesState, photoDao.photosState) { notes, photos ->
            notes.filter { it.jobId == jobId }
                .sortedByDescending { it.timestamp }
                .map { note -> NoteWithPhotos(note, photos.filter { it.noteId == note.id }) }
        }

    override suspend fun insert(note: JobNoteEntity): Long {
        val id = idSeq.getAndIncrement()
        notesState.value = notesState.value + note.copy(id = id)
        return id
    }

    override suspend fun delete(noteId: Long) {
        notesState.value = notesState.value.filterNot { it.id == noteId }
        // Simulate the FK cascade Room performs at the database level.
        photoDao.photosState.value = photoDao.photosState.value.filterNot { it.noteId == noteId }
    }
}
