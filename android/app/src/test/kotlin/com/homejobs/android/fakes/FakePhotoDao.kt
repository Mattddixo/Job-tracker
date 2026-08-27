package com.homejobs.android.fakes

import com.homejobs.android.data.local.db.PhotoDao
import com.homejobs.android.data.local.db.PhotoEntity
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.concurrent.atomic.AtomicLong

class FakePhotoDao : PhotoDao {
    val photosState = MutableStateFlow<List<PhotoEntity>>(emptyList())
    private val idSeq = AtomicLong(1)

    /** Set by FakeJobNoteDao so filePathsForJob can resolve note -> job without a circular reference. */
    var noteJobLookup: (Long) -> Long? = { null }

    override suspend fun insert(photo: PhotoEntity): Long {
        val id = idSeq.getAndIncrement()
        photosState.value = photosState.value + photo.copy(id = id)
        return id
    }

    override suspend fun getById(id: Long): PhotoEntity? = photosState.value.firstOrNull { it.id == id }

    override suspend fun delete(id: Long) {
        photosState.value = photosState.value.filterNot { it.id == id }
    }

    override suspend fun filePathsForNote(noteId: Long): List<String> =
        photosState.value.filter { it.noteId == noteId }.map { it.filePath }

    override suspend fun filePathsForJob(jobId: Long): List<String> =
        photosState.value.filter { noteJobLookup(it.noteId) == jobId }.map { it.filePath }
}
