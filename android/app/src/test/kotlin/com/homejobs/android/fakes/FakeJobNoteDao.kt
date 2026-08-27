package com.homejobs.android.fakes

import com.homejobs.android.data.local.db.JobNoteDao
import com.homejobs.android.data.local.db.JobNoteEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeJobNoteDao : JobNoteDao {
    private val state = MutableStateFlow<List<JobNoteEntity>>(emptyList())

    override fun observeNotes(jobId: Long) =
        state.map { notes -> notes.filter { it.jobId == jobId }.sortedByDescending { it.timestamp } }

    override suspend fun upsert(note: JobNoteEntity) {
        state.value = state.value.filterNot { it.id == note.id } + note
    }

    override suspend fun upsertAll(notes: List<JobNoteEntity>) {
        val ids = notes.map { it.id }.toSet()
        state.value = state.value.filterNot { it.id in ids } + notes
    }

    override suspend fun delete(noteId: Long) {
        state.value = state.value.filterNot { it.id == noteId }
    }

    override suspend fun deleteNotIn(jobId: Long, keepIds: List<Long>) {
        state.value = state.value.filterNot { it.jobId == jobId && it.id !in keepIds }
    }
}
