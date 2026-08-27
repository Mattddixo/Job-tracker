package com.homejobs.android.data.local.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface JobNoteDao {
    @Query("SELECT * FROM job_notes WHERE jobId = :jobId ORDER BY timestamp DESC")
    fun observeNotes(jobId: Long): Flow<List<JobNoteEntity>>

    @Upsert
    suspend fun upsert(note: JobNoteEntity)

    @Upsert
    suspend fun upsertAll(notes: List<JobNoteEntity>)

    @Query("DELETE FROM job_notes WHERE id = :noteId")
    suspend fun delete(noteId: Long)

    @Query("DELETE FROM job_notes WHERE jobId = :jobId AND id NOT IN (:keepIds)")
    suspend fun deleteNotIn(jobId: Long, keepIds: List<Long>)

    @Transaction
    suspend fun replaceForJob(jobId: Long, notes: List<JobNoteEntity>) {
        deleteNotIn(jobId, notes.map { it.id })
        upsertAll(notes)
    }
}
