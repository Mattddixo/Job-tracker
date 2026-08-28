package com.homejobs.android.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface JobNoteDao {
    @Transaction
    @Query("SELECT * FROM job_notes WHERE jobId = :jobId ORDER BY timestamp DESC")
    fun observeNotesWithPhotos(jobId: Long): Flow<List<NoteWithPhotos>>

    @Insert
    suspend fun insert(note: JobNoteEntity): Long

    @Query("UPDATE job_notes SET body = :body WHERE id = :noteId")
    suspend fun updateBody(noteId: Long, body: String)

    @Query("DELETE FROM job_notes WHERE id = :noteId")
    suspend fun delete(noteId: Long)
}
